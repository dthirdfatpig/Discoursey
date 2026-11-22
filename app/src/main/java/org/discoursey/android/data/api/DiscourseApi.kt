package org.discoursey.android.data.api

import okhttp3.ResponseBody
import org.discoursey.android.data.api.models.*
import retrofit2.Response
import retrofit2.http.*

interface DiscourseApi {

    @GET("session/csrf.json")
    suspend fun getCsrf(): CsrfResponse

    @FormUrlEncoded
    @POST("session")
    suspend fun loginSession(
        @Field("login") login: String,
        @Field("password") password: String,
        @Field("second_factor_token") secondFactorToken: String? = null
    ): Response<SessionLoginResponse>

    @GET("site.json")
    suspend fun getSite(): SiteResponse

    @GET("latest.json")
    suspend fun getLatestTopics(
        @Query("page") page: Int = 0
    ): TopicListResponse

    @GET("top.json")
    suspend fun getTopTopics(
        @Query("period") period: String = "all",
        @Query("page") page: Int = 0
    ): TopicListResponse

    @GET("c/{slug}/{id}.json")
    suspend fun getCategoryTopics(
        @Path("slug") slug: String,
        @Path("id") id: Long,
        @Query("page") page: Int = 0
    ): TopicListResponse

    @GET("t/{id}.json")
    suspend fun getTopic(
        @Path("id") id: Long
    ): TopicDetailResponse

    @GET("search.json")
    suspend fun search(
        @Query("q") query: String
    ): TopicListResponse

    @Multipart
    @POST("uploads.json")
    suspend fun uploadFile(
        @Part file: okhttp3.MultipartBody.Part,
        @Part("type") type: okhttp3.RequestBody,
        @Part("synchronous") synchronous: okhttp3.RequestBody
    ): UploadResponse

    @FormUrlEncoded
    @POST("video-stream/prepare-upload")
    suspend fun prepareVideoUpload(
        @Field("file_name") fileName: String,
        @Field("file_size") fileSize: Long,
        @Field("file_type") fileType: String
    ): PrepareVideoUploadResponse

    @PUT("discourse-reactions/posts/{postId}/custom-reactions/{reaction}/toggle.json")
    suspend fun toggleCustomReaction(
        @Path("postId") postId: Long,
        @Path("reaction") reaction: String
    ): Response<ResponseBody>

    @POST("posts.json")
    suspend fun createPost(
        @Body request: CreatePostRequest
    ): CreatePostResponse

    @POST("post_actions")
    suspend fun postAction(
        @Body request: PostActionRequest
    ): Response<ResponseBody>

    @DELETE("post_actions/{id}")
    suspend fun deletePostAction(
        @Path("id") postId: Long,
        @Query("post_action_type_id") typeId: Int = 2
    ): Response<ResponseBody>

    @GET("topics/private-messages/{username}.json")
    suspend fun getPrivateMessages(
        @Path("username") username: String,
        @Query("page") page: Int = 0
    ): TopicListResponse

    @GET("notifications.json")
    suspend fun getNotifications(): NotificationResponse

    @PUT("notifications/mark-read")
    suspend fun markNotificationsRead(): Response<ResponseBody>

    @GET("u/{username}.json")
    suspend fun getUserProfile(
        @Path("username") username: String
    ): UserProfileResponse

    @GET("u/{username}/bookmarks.json")
    suspend fun getUserBookmarks(
        @Path("username") username: String
    ): TopicListResponse

    @GET("u/{username}/activity/replies.json")
    suspend fun getUserReplies(
        @Path("username") username: String
    ): TopicListResponse

    // Image Album Plugin integration
    @GET("image-albums/albums")
    suspend fun getUserAlbums(): AlbumListResponse

    @GET("image-albums/albums/{albumId}/items")
    suspend fun getAlbumItems(
        @Path("albumId") albumId: Long
    ): AlbumItemListResponse

    @PUT("u/{username}.json")
    suspend fun updateProfile(
        @Path("username") username: String,
        @Query("name") name: String? = null,
        @Query("bio_raw") bio: String? = null,
        @Query("website") website: String? = null
    ): Response<ResponseBody>
}
