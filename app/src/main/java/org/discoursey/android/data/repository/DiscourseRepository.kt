package org.discoursey.android.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.discoursey.android.data.api.DiscourseClient
import org.discoursey.android.data.api.models.*
import org.discoursey.android.data.preferences.AppPreferences
import java.io.File
import java.io.FileOutputStream

class DiscourseRepository(
    private val client: DiscourseClient,
    private val prefs: AppPreferences
) {

    init {
        if (prefs.forumUrl.isNotBlank()) {
            client.updateConfig(
                baseUrl = prefs.forumUrl,
                username = prefs.username.ifBlank { null },
                key = prefs.passwordOrApiKey.ifBlank { null },
                isApiKey = prefs.isApiKeyLogin
            )
        }
    }

    suspend fun login(
        baseUrl: String,
        username: String = "",
        passwordOrKey: String = "",
        isApiKey: Boolean = false
    ): Result<SiteResponse> = withContext(Dispatchers.IO) {
        try {
            client.updateConfig(baseUrl, username, passwordOrKey, isApiKey)

            // Step 1: Obtain CSRF token
            try {
                val csrf = client.api.getCsrf()
                if (csrf.csrf.isNotBlank()) {
                    client.csrfToken = csrf.csrf
                }
            } catch (_: Exception) { }

            // Step 2: If standard username/email & password, perform session login
            if (!isApiKey && username.isNotBlank() && passwordOrKey.isNotBlank()) {
                val loginResponse = client.api.loginSession(
                    login = username,
                    password = passwordOrKey
                )

                if (!loginResponse.isSuccessful) {
                    val errorBody = loginResponse.errorBody()?.string()
                    val errorMessage = parseErrorMessage(errorBody, loginResponse.code())
                    return@withContext Result.failure(Exception(errorMessage))
                }

                val body = loginResponse.body()
                if (body != null) {
                    if (!body.error.isNullOrBlank()) {
                        return@withContext Result.failure(Exception(body.error))
                    }
                    if (!body.errors.isNullOrEmpty()) {
                        return@withContext Result.failure(Exception(body.errors.joinToString("\n")))
                    }
                    if (body.secondFactorRequired) {
                        return@withContext Result.failure(
                            Exception("Two-factor authentication is active on this account. Please use a Discourse User API Key.")
                        )
                    }
                }
            }

            // Step 3: Fetch site details (categories, title, etc.) with authenticated session
            val site = client.api.getSite()
            Result.success(site)
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: "Failed to connect to Discourse instance."
            if (msg.contains("403")) {
                Result.failure(Exception("HTTP 403 Forbidden: Access denied. Please verify your credentials or check if the forum requires login."))
            } else {
                Result.failure(e)
            }
        }
    }

    private fun parseErrorMessage(errorBody: String?, code: Int): String {
        if (!errorBody.isNullOrBlank()) {
            try {
                val json = com.google.gson.JsonParser.parseString(errorBody).asJsonObject
                if (json.has("error")) {
                    return json.get("error").asString
                }
                if (json.has("errors") && json.get("errors").isJsonArray) {
                    val arr = json.getAsJsonArray("errors")
                    val list = arr.map { it.asString }
                    if (list.isNotEmpty()) return list.joinToString("\n")
                }
                if (json.has("message")) {
                    return json.get("message").asString
                }
            } catch (_: Exception) { }
        }
        return when (code) {
            401 -> "Authentication failed: Invalid username or password."
            403 -> "HTTP 403 Forbidden: Access denied by forum server or login required."
            404 -> "HTTP 404: Discourse endpoint not found. Please verify the forum URL."
            422 -> "HTTP 422: Unprocessable request. Check parameters or credentials."
            else -> "HTTP $code: Server error while connecting."
        }
    }

    suspend fun testConnection(
        baseUrl: String,
        username: String? = null,
        apiKey: String? = null,
        isApiKey: Boolean = false
    ): Result<SiteResponse> = login(
        baseUrl = baseUrl,
        username = username ?: "",
        passwordOrKey = apiKey ?: "",
        isApiKey = isApiKey
    )

    fun logout() {
        client.clearSession()
    }

    suspend fun getSiteInfo(): SiteResponse = withContext(Dispatchers.IO) {
        try {
            client.api.getSite()
        } catch (_: Exception) {
            SiteResponse()
        }
    }

    suspend fun getLatestTopics(page: Int = 0): List<Topic> = withContext(Dispatchers.IO) {
        try {
            val response = client.api.getLatestTopics(page)
            val usersMap = response.users.associateBy { it.id }
            val topics = response.topicList?.topics ?: emptyList()
            topics.forEach { topic ->
                resolveLastPoster(topic, usersMap)
            }
            topics
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun checkForNewTopics(highestExistingId: Long): Boolean = withContext(Dispatchers.IO) {
        if (highestExistingId <= 0) return@withContext false
        try {
            val response = client.api.getLatestTopics(0)
            val topics = response.topicList?.topics ?: emptyList()
            topics.any { it.id > highestExistingId }
        } catch (_: Exception) {
            false
        }
    }

    suspend fun getTopTopics(period: String = "all", page: Int = 0): List<Topic> = withContext(Dispatchers.IO) {
        try {
            val response = client.api.getTopTopics(period, page)
            val usersMap = response.users.associateBy { it.id }
            val topics = response.topicList?.topics ?: emptyList()
            topics.forEach { topic ->
                resolveLastPoster(topic, usersMap)
            }
            topics
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun getCategoryTopics(slug: String, categoryId: Long, page: Int = 0): List<Topic> = withContext(Dispatchers.IO) {
        try {
            val response = client.api.getCategoryTopics(slug, categoryId, page)
            val usersMap = response.users.associateBy { it.id }
            val topics = response.topicList?.topics ?: emptyList()
            topics.forEach { topic ->
                resolveLastPoster(topic, usersMap)
            }
            topics
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun getTopicDetail(topicId: Long): TopicDetailResponse = withContext(Dispatchers.IO) {
        client.api.getTopic(topicId)
    }

    suspend fun searchTopics(query: String): List<Topic> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        try {
            val response = client.api.search(query)
            val usersMap = response.users.associateBy { it.id }
            val topics = response.topicList?.topics ?: emptyList()
            topics.forEach { resolveLastPoster(it, usersMap) }
            topics
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun createTopic(title: String, raw: String, categoryId: Long?): Result<CreatePostResponse> =
        withContext(Dispatchers.IO) {
            try {
                val req = CreatePostRequest(
                    title = title,
                    raw = raw,
                    categoryId = categoryId
                )
                val resp = client.api.createPost(req)
                Result.success(resp)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun createReply(topicId: Long, raw: String, replyToPostNumber: Int? = null): Result<CreatePostResponse> =
        withContext(Dispatchers.IO) {
            try {
                val req = CreatePostRequest(
                    topicId = topicId,
                    raw = raw,
                    replyToPostNumber = replyToPostNumber
                )
                val resp = client.api.createPost(req)
                Result.success(resp)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun toggleLike(postId: Long, currentLiked: Boolean): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            if (currentLiked) {
                client.api.deletePostAction(postId, typeId = 2)
                Result.success(false)
            } else {
                client.api.postAction(PostActionRequest(postId = postId, postActionTypeId = 2))
                Result.success(true)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleReaction(postId: Long, reactionName: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            if (reactionName == "heart") {
                val resp = client.api.toggleCustomReaction(postId, "heart")
                if (resp.isSuccessful) {
                    Result.success(true)
                } else {
                    // Fallback to core like
                    toggleLike(postId, false)
                }
            } else {
                val response = client.api.toggleCustomReaction(postId, reactionName)
                if (response.isSuccessful) {
                    Result.success(true)
                } else {
                    Result.failure(Exception("Reactions not supported on this forum."))
                }
            }
        } catch (_: Exception) {
            if (reactionName == "heart") {
                toggleLike(postId, false)
            } else {
                Result.failure(Exception("Reactions not enabled on this forum."))
            }
        }
    }

    suspend fun votePoll(postId: Long, pollName: String, options: List<String>): Boolean = withContext(Dispatchers.IO) {
        try {
            val resp = client.api.votePoll(postId, pollName, options)
            resp.isSuccessful
        } catch (_: Exception) {
            false
        }
    }

    suspend fun unvotePoll(postId: Long, pollName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val resp = client.api.unvotePoll(postId, pollName)
            resp.isSuccessful
        } catch (_: Exception) {
            false
        }
    }

    suspend fun voteTopic(topicId: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            val resp = client.api.voteTopic(topicId)
            resp.isSuccessful
        } catch (_: Exception) {
            false
        }
    }

    suspend fun unvoteTopic(topicId: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            val resp = client.api.unvoteTopic(topicId)
            resp.isSuccessful
        } catch (_: Exception) {
            false
        }
    }

    suspend fun getPrivateMessages(page: Int = 0): List<Topic> = withContext(Dispatchers.IO) {
        val user = prefs.username.ifBlank { return@withContext emptyList() }
        try {
            val response = client.api.getPrivateMessages(user, page)
            val usersMap = response.users.associateBy { it.id }
            val topics = response.topicList?.topics ?: emptyList()
            topics.forEach { resolveLastPoster(it, usersMap) }
            topics
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun getNotifications(): List<NotificationItem> = withContext(Dispatchers.IO) {
        try {
            val resp = client.api.getNotifications()
            resp.notifications
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun markAllNotificationsRead(): Boolean = withContext(Dispatchers.IO) {
        try {
            client.api.markNotificationsRead()
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun getUserProfile(username: String? = null): UserProfile? = withContext(Dispatchers.IO) {
        val targetUser = username ?: prefs.username.ifBlank { return@withContext null }
        try {
            val resp = client.api.getUserProfile(targetUser)
            resp.user
        } catch (_: Exception) {
            null
        }
    }

    suspend fun getUserBookmarks(username: String? = null): List<Topic> = withContext(Dispatchers.IO) {
        val targetUser = username ?: prefs.username.ifBlank { return@withContext emptyList() }
        try {
            val resp = client.api.getUserBookmarks(targetUser)
            val usersMap = resp.users.associateBy { it.id }
            val topics = resp.topicList?.topics ?: emptyList()
            topics.forEach { resolveLastPoster(it, usersMap) }
            topics
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun getUserReplies(username: String? = null): List<Topic> = withContext(Dispatchers.IO) {
        val targetUser = username ?: prefs.username.ifBlank { return@withContext emptyList() }
        try {
            val resp = client.api.getUserReplies(targetUser)
            val usersMap = resp.users.associateBy { it.id }
            val topics = resp.topicList?.topics ?: emptyList()
            topics.forEach { resolveLastPoster(it, usersMap) }
            topics
        } catch (_: Exception) {
            emptyList()
        }
    }

    // Discourse Image Album integration
    suspend fun getUserAlbums(): List<Album> = withContext(Dispatchers.IO) {
        try {
            val resp = client.api.getUserAlbums()
            resp.albums
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun getAlbumItems(albumId: Long): List<AlbumItem> = withContext(Dispatchers.IO) {
        try {
            val resp = client.api.getAlbumItems(albumId)
            resp.items
        } catch (_: Exception) {
            emptyList()
        }
    }

    // Standard Discourse File Upload (/uploads.json)
    suspend fun uploadFile(context: Context, uri: Uri): Result<UploadResponse> = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val fileName = getFileName(context, uri) ?: "attachment_${System.currentTimeMillis()}"
            val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"

            val tempFile = File(context.cacheDir, fileName)
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }

            val requestFile = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", fileName, requestFile)
            val typePart = "composer".toRequestBody("text/plain".toMediaTypeOrNull())
            val syncPart = "true".toRequestBody("text/plain".toMediaTypeOrNull())

            val uploadResponse = client.api.uploadFile(body, typePart, syncPart)
            tempFile.delete()
            Result.success(uploadResponse)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Video Upload: Supports discourse-video-stream plugin (Cloudflare/Bunny TUS) and Discourse internal video upload
    suspend fun uploadVideoStream(context: Context, uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        val contentResolver = context.contentResolver
        val fileName = getFileName(context, uri) ?: "video_${System.currentTimeMillis()}.mp4"
        val mimeType = contentResolver.getType(uri) ?: "video/mp4"

        val tempFile = File(context.cacheDir, "temp_stream_$fileName")
        try {
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
            val fileSize = tempFile.length()

            // 1. Try Discourse Video Stream plugin via Cloudflare TUS route (/video-stream/upload-url)
            val baseUri = client.currentBaseUrl.trimEnd('/')
            val metaString = "name " + android.util.Base64.encodeToString(fileName.toByteArray(), android.util.Base64.NO_WRAP) +
                    ",filetype " + android.util.Base64.encodeToString(mimeType.toByteArray(), android.util.Base64.NO_WRAP)

            val cfInitRequest = okhttp3.Request.Builder()
                .url("$baseUri/video-stream/upload-url")
                .post("".toRequestBody("application/json".toMediaTypeOrNull()))
                .header("Upload-Length", fileSize.toString())
                .header("Upload-Metadata", metaString)
                .header("Tus-Resumable", "1.0.0")
                .header("Accept", "application/json")
                .build()

            val cfResp = client.okHttpClient.newCall(cfInitRequest).execute()
            if (cfResp.code == 201 || cfResp.isSuccessful) {
                val location = cfResp.header("Location")
                val streamMediaId = cfResp.header("stream-media-id")
                cfResp.close()

                if (!location.isNullOrBlank()) {
                    val fileBody = tempFile.asRequestBody("application/offset+octet-stream".toMediaTypeOrNull())
                    val patchRequest = okhttp3.Request.Builder()
                        .url(location)
                        .patch(fileBody)
                        .header("Tus-Resumable", "1.0.0")
                        .header("Upload-Offset", "0")
                        .header("Content-Type", "application/offset+octet-stream")
                        .build()

                    val patchResp = client.rawHttpClient.newCall(patchRequest).execute()
                    patchResp.close()

                    var uid = streamMediaId
                    if (uid.isNullOrBlank()) {
                        uid = location.substringAfterLast('/').substringBefore('?')
                    }
                    tempFile.delete()
                    return@withContext Result.success("\n\n[video-stream id=\"$uid\"]\n[/video-stream]\n\n")
                }
            } else {
                cfResp.close()
            }

            // 2. Try Bunny video stream provider (/video-stream/prepare-upload)
            try {
                val prepareResp = client.api.prepareVideoUpload(fileName, fileSize, mimeType)
                val endpoint = prepareResp.endpoint ?: prepareResp.uploadUrl
                val vid = prepareResp.videoId ?: prepareResp.streamMediaId
                if (!vid.isNullOrBlank() && !endpoint.isNullOrBlank()) {
                    val fileBody = tempFile.asRequestBody("application/offset+octet-stream".toMediaTypeOrNull())
                    val patchBuilder = okhttp3.Request.Builder()
                        .url(endpoint)
                        .patch(fileBody)
                        .header("Tus-Resumable", "1.0.0")
                        .header("Upload-Offset", "0")
                        .header("Content-Type", "application/offset+octet-stream")

                    for ((k, v) in prepareResp.headers) {
                        patchBuilder.header(k, v)
                    }
                    val bunnyResp = client.rawHttpClient.newCall(patchBuilder.build()).execute()
                    bunnyResp.close()
                    tempFile.delete()
                    return@withContext Result.success("\n\n[video-stream id=\"$vid\"]\n[/video-stream]\n\n")
                }
            } catch (_: Exception) { }

            // 3. Fallback to standard Discourse internal video upload (/uploads.json)
            val uploadResult = uploadFile(context, uri)
            tempFile.delete()
            if (uploadResult.isSuccess) {
                val upload = uploadResult.getOrNull()!!
                val targetUrl = upload.shortUrl?.takeIf { it.isNotBlank() } ?: upload.url
                Result.success("\n<video controls src=\"$targetUrl\"></video>\n")
            } else {
                Result.failure(uploadResult.exceptionOrNull() ?: Exception("Video upload failed"))
            }
        } catch (e: Exception) {
            tempFile.delete()
            // Final fallback to standard upload
            val uploadResult = uploadFile(context, uri)
            if (uploadResult.isSuccess) {
                val upload = uploadResult.getOrNull()!!
                val targetUrl = upload.shortUrl?.takeIf { it.isNotBlank() } ?: upload.url
                Result.success("\n<video controls src=\"$targetUrl\"></video>\n")
            } else {
                Result.failure(e)
            }
        }
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        var name: String? = null
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (idx >= 0) name = cursor.getString(idx)
                }
            }
        }
        if (name == null) {
            name = uri.path?.let { p ->
                val cut = p.lastIndexOf('/')
                if (cut != -1) p.substring(cut + 1) else p
            }
        }
        return name
    }

    suspend fun updateProfile(name: String?, bio: String?, website: String?): Boolean = withContext(Dispatchers.IO) {
        val username = prefs.username.ifBlank { return@withContext false }
        try {
            client.api.updateProfile(username, name, bio, website)
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun resolveLastPoster(topic: Topic, usersMap: Map<Long, User>) {
        val lastPoster = topic.posters.lastOrNull()
        if (lastPoster != null) {
            topic.resolvedLastPosterUser = usersMap[lastPoster.userId]
        }
        if (topic.resolvedLastPosterUser == null && !topic.lastPosterUsername.isNullOrBlank()) {
            topic.resolvedLastPosterUser = User(
                username = topic.lastPosterUsername,
                name = topic.lastPosterUsername
            )
        }
    }
}
