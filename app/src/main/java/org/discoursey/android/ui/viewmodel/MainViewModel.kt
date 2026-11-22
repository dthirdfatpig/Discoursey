package org.discoursey.android.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.discoursey.android.data.api.models.*
import org.discoursey.android.data.preferences.AppPreferences
import org.discoursey.android.data.preferences.CacheManager
import org.discoursey.android.data.repository.DiscourseRepository

data class MainUiState(
    val isLoggedIn: Boolean = false,
    val forumUrl: String = "",
    val username: String = "",
    val uiMode: String = AppPreferences.MODE_MODERN,
    val themeMode: String = AppPreferences.THEME_MODERN_DARK,
    val fontSize: String = AppPreferences.FONT_NORMAL,
    val nameDisplayPreference: String = AppPreferences.DISPLAY_AUTO,
    val forumEnablesNames: Boolean = true,
    val bottomBarSlots: List<String> = listOf("home", "search", "messages", "profile", "menu"),
    val homeDestination: String = "latest",
    val topics: List<Topic> = emptyList(),
    val hasNewTopics: Boolean = false,
    val isRefreshing: Boolean = false,
    val categories: List<Category> = emptyList(),
    val privateMessages: List<Topic> = emptyList(),
    val notifications: List<NotificationItem> = emptyList(),
    val unreadNotificationCount: Int = 0,
    val currentTopicDetail: TopicDetailResponse? = null,
    val userProfile: UserProfile? = null,
    val userBookmarks: List<Topic> = emptyList(),
    val userReplies: List<Topic> = emptyList(),
    val userAlbums: List<Album> = emptyList(),
    val currentAlbumItems: List<AlbumItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val cacheSizeFormatted: String = "0 MB"
)

class MainViewModel(
    private val repository: DiscourseRepository,
    private val prefs: AppPreferences,
    private val cacheManager: CacheManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        MainUiState(
            isLoggedIn = prefs.isLoggedIn,
            forumUrl = prefs.forumUrl,
            username = prefs.username,
            uiMode = prefs.uiMode,
            themeMode = prefs.themeMode,
            fontSize = prefs.fontSize,
            nameDisplayPreference = prefs.nameDisplayPreference,
            bottomBarSlots = prefs.bottomBarSlots,
            homeDestination = prefs.homeDestination,
            cacheSizeFormatted = cacheManager.getFormattedCacheSize()
        )
    )
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var highestKnownTopicId: Long = 0L

    init {
        if (prefs.isLoggedIn || prefs.forumUrl.isNotBlank()) {
            refreshAll()
        }
    }

    fun login(
        url: String,
        user: String,
        passOrKey: String,
        isApiKey: Boolean = false,
        onComplete: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = repository.login(url, user.trim(), passOrKey.trim(), isApiKey)
            result.onSuccess { site ->
                prefs.forumUrl = url
                prefs.username = user
                prefs.passwordOrApiKey = passOrKey
                prefs.isApiKeyLogin = isApiKey
                prefs.isLoggedIn = true
                _uiState.value = _uiState.value.copy(
                    isLoggedIn = true,
                    forumUrl = url,
                    username = user,
                    categories = site.categories,
                    forumEnablesNames = site.enableNames,
                    isLoading = false
                )
                refreshAll()
                onComplete(true, null)
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = err.localizedMessage ?: "Failed to connect to Discourse instance."
                )
                onComplete(false, err.localizedMessage)
            }
        }
    }

    fun useDemoInstance(onComplete: () -> Unit) {
        login("https://meta.discourse.org", "demo_user", "", isApiKey = false) { success, _ ->
            if (success) onComplete()
        }
    }

    fun refreshAll() {
        refreshTopics()
        refreshCategories()
        refreshPrivateMessages()
        refreshNotifications()
        refreshUserProfile()
        refreshAlbums()
        updateCacheSize()
    }

    fun refreshTopics(isSwipeToRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isSwipeToRefresh) {
                _uiState.value = _uiState.value.copy(isRefreshing = true)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = true)
            }

            val list = repository.getLatestTopics()
            highestKnownTopicId = list.maxOfOrNull { it.id } ?: highestKnownTopicId

            _uiState.value = _uiState.value.copy(
                topics = list,
                hasNewTopics = false,
                isLoading = false,
                isRefreshing = false
            )
        }
    }

    fun checkForNewTopics() {
        if (highestKnownTopicId <= 0L) return
        viewModelScope.launch {
            val hasNew = repository.checkForNewTopics(highestKnownTopicId)
            if (hasNew) {
                _uiState.value = _uiState.value.copy(hasNewTopics = true)
            }
        }
    }

    fun refreshCategories() {
        viewModelScope.launch {
            val site = repository.getSiteInfo()
            _uiState.value = _uiState.value.copy(
                categories = site.categories,
                forumEnablesNames = site.enableNames
            )
        }
    }

    fun refreshPrivateMessages() {
        viewModelScope.launch {
            val pms = repository.getPrivateMessages()
            _uiState.value = _uiState.value.copy(privateMessages = pms)
        }
    }

    fun refreshNotifications() {
        viewModelScope.launch {
            val notifs = repository.getNotifications()
            val unreadCount = notifs.count { !it.read }
            _uiState.value = _uiState.value.copy(
                notifications = notifs,
                unreadNotificationCount = unreadCount
            )
        }
    }

    fun markNotificationsAsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsRead()
            val updated = _uiState.value.notifications.map { it.copy(read = true) }
            _uiState.value = _uiState.value.copy(
                notifications = updated,
                unreadNotificationCount = 0
            )
        }
    }

    fun loadTopicDetail(topicId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val detail = repository.getTopicDetail(topicId)
                _uiState.value = _uiState.value.copy(
                    currentTopicDetail = detail,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage
                )
            }
        }
    }

    fun createTopic(title: String, raw: String, categoryId: Long?, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.createTopic(title, raw, categoryId)
            _uiState.value = _uiState.value.copy(isLoading = false)
            if (result.isSuccess) {
                refreshTopics()
                onDone(true)
            } else {
                onDone(false)
            }
        }
    }

    fun replyToTopic(topicId: Long, raw: String, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.createReply(topicId, raw)
            _uiState.value = _uiState.value.copy(isLoading = false)
            if (result.isSuccess) {
                loadTopicDetail(topicId)
                onDone(true)
            } else {
                onDone(false)
            }
        }
    }

    fun toggleLike(postId: Long, currentLiked: Boolean) {
        viewModelScope.launch {
            repository.toggleLike(postId, currentLiked)
            val detail = _uiState.value.currentTopicDetail ?: return@launch
            val updatedPosts = detail.postStream?.posts?.map { post ->
                if (post.id == postId) {
                    val newCount = if (currentLiked) maxOf(0, post.likeCount - 1) else post.likeCount + 1
                    val newActions = post.actionsSummary.toMutableList()
                    val idx = newActions.indexOfFirst { it.id == 2 }
                    if (idx >= 0) {
                        newActions[idx] = newActions[idx].copy(count = newCount, acted = !currentLiked)
                    } else {
                        newActions.add(PostActionSummary(2, count = newCount, acted = !currentLiked))
                    }
                    post.copy(actionsSummary = newActions)
                } else post
            } ?: emptyList()
            _uiState.value = _uiState.value.copy(
                currentTopicDetail = detail.copy(
                    postStream = detail.postStream?.copy(posts = updatedPosts)
                )
            )
        }
    }

    fun toggleReaction(postId: Long, reactionName: String) {
        viewModelScope.launch {
            repository.toggleReaction(postId, reactionName)
            val detail = _uiState.value.currentTopicDetail ?: return@launch
            val updatedPosts = detail.postStream?.posts?.map { post ->
                if (post.id == postId) {
                    val reactionsList = post.reactions.toMutableList()
                    val existing = reactionsList.indexOfFirst { it.id == reactionName }
                    if (existing >= 0) {
                        reactionsList[existing] = reactionsList[existing].copy(count = reactionsList[existing].count + 1)
                    } else {
                        reactionsList.add(PostReaction(id = reactionName, type = "emoji", count = 1))
                    }
                    post.copy(
                        reactions = reactionsList,
                        currentUserReaction = PostCurrentUserReaction(id = reactionName)
                    )
                } else post
            } ?: emptyList()
            _uiState.value = _uiState.value.copy(
                currentTopicDetail = detail.copy(
                    postStream = detail.postStream?.copy(posts = updatedPosts)
                )
            )
        }
    }

    fun uploadAttachment(
        context: Context,
        uri: Uri,
        isVideo: Boolean,
        onDone: (markdown: String?, error: String?) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            if (isVideo) {
                // Try Discourse Video Stream plugin first
                val fileName = "video_${System.currentTimeMillis()}.mp4"
                val prepareResult = repository.prepareVideoStream(fileName, 1024L * 1024L, "video/mp4")
                if (prepareResult.isSuccess && !prepareResult.getOrNull()?.videoId.isNullOrBlank()) {
                    val videoId = prepareResult.getOrNull()!!.videoId!!
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onDone("\n[video-stream id=\"$videoId\"]\n", null)
                    return@launch
                }
            }

            // Standard upload
            val result = repository.uploadFile(context, uri)
            _uiState.value = _uiState.value.copy(isLoading = false)
            result.onSuccess { upload ->
                val markdown = if (isVideo) {
                    "\n![video](${upload.url})\n"
                } else {
                    "\n![${upload.originalFilename.ifBlank { "image" }}](${upload.url})\n"
                }
                onDone(markdown, null)
            }.onFailure { err ->
                onDone(null, err.localizedMessage ?: "Failed to upload file.")
            }
        }
    }

    fun refreshUserProfile() {
        viewModelScope.launch {
            val profile = repository.getUserProfile()
            val bookmarks = repository.getUserBookmarks()
            val replies = repository.getUserReplies()
            _uiState.value = _uiState.value.copy(
                userProfile = profile,
                userBookmarks = bookmarks,
                userReplies = replies
            )
        }
    }

    fun refreshAlbums() {
        viewModelScope.launch {
            val albums = repository.getUserAlbums()
            _uiState.value = _uiState.value.copy(userAlbums = albums)
        }
    }

    fun loadAlbumItems(albumId: Long) {
        viewModelScope.launch {
            val items = repository.getAlbumItems(albumId)
            _uiState.value = _uiState.value.copy(currentAlbumItems = items)
        }
    }

    fun setUiMode(mode: String) {
        prefs.uiMode = mode
        _uiState.value = _uiState.value.copy(uiMode = mode)
    }

    fun setThemeMode(theme: String) {
        prefs.themeMode = theme
        _uiState.value = _uiState.value.copy(themeMode = theme)
    }

    fun setFontSize(size: String) {
        prefs.fontSize = size
        _uiState.value = _uiState.value.copy(fontSize = size)
    }

    fun setNameDisplayPreference(pref: String) {
        prefs.nameDisplayPreference = pref
        _uiState.value = _uiState.value.copy(nameDisplayPreference = pref)
    }

    fun updateBottomSlots(slots: List<String>) {
        prefs.bottomBarSlots = slots
        _uiState.value = _uiState.value.copy(bottomBarSlots = slots)
    }

    fun setHomeDestination(dest: String) {
        prefs.homeDestination = dest
        _uiState.value = _uiState.value.copy(homeDestination = dest)
    }

    fun clearCache(): Boolean {
        val success = cacheManager.clearAllCache()
        updateCacheSize()
        return success
    }

    fun updateCacheSize() {
        _uiState.value = _uiState.value.copy(cacheSizeFormatted = cacheManager.getFormattedCacheSize())
    }

    fun logout() {
        prefs.clearAuth()
        repository.logout()
        _uiState.value = _uiState.value.copy(
            isLoggedIn = false,
            username = "",
            topics = emptyList(),
            privateMessages = emptyList(),
            notifications = emptyList()
        )
    }
}
