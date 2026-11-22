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
        // Clear previous topic immediately to prevent flash of previous topic
        if (_uiState.value.currentTopicDetail?.id != topicId) {
            _uiState.value = _uiState.value.copy(currentTopicDetail = null, isLoading = true)
        }
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

    fun clearTopicDetail() {
        _uiState.value = _uiState.value.copy(currentTopicDetail = null)
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
        toggleReaction(postId, "heart")
    }

    fun toggleReaction(postId: Long, reactionName: String) {
        viewModelScope.launch {
            val detail = _uiState.value.currentTopicDetail ?: return@launch
            val post = detail.postStream?.posts?.firstOrNull { it.id == postId } ?: return@launch

            val isCurrentlyReactedWithThis = post.currentUserReaction?.id == reactionName || (reactionName == "heart" && post.isLiked && post.currentUserReaction == null)
            val previousReactionId = post.currentUserReaction?.id ?: if (post.isLiked) "heart" else null

            // Optimistic UI update
            val updatedPosts = detail.postStream?.posts?.map { p ->
                if (p.id == postId) {
                    val reactionsList = p.reactions.map { it.copy() }.toMutableList()
                    val actionsList = p.actionsSummary.map { it.copy() }.toMutableList()

                    if (isCurrentlyReactedWithThis) {
                        // User is removing this reaction
                        val idx = reactionsList.indexOfFirst { it.id == reactionName }
                        if (idx >= 0) {
                            val newCount = reactionsList[idx].count - 1
                            if (newCount <= 0) {
                                reactionsList.removeAt(idx)
                            } else {
                                reactionsList[idx] = reactionsList[idx].copy(count = newCount)
                            }
                        }
                        if (reactionName == "heart") {
                            val likeIdx = actionsList.indexOfFirst { it.id == 2 }
                            if (likeIdx >= 0) {
                                actionsList[likeIdx] = actionsList[likeIdx].copy(count = maxOf(0, actionsList[likeIdx].count - 1), acted = false)
                            }
                        }
                        p.copy(
                            reactions = reactionsList,
                            actionsSummary = actionsList,
                            currentUserReaction = null
                        )
                    } else {
                        // User is switching from another reaction or adding a new one
                        if (!previousReactionId.isNullOrBlank()) {
                            val prevIdx = reactionsList.indexOfFirst { it.id == previousReactionId }
                            if (prevIdx >= 0) {
                                val prevCount = reactionsList[prevIdx].count - 1
                                if (prevCount <= 0) {
                                    reactionsList.removeAt(prevIdx)
                                } else {
                                    reactionsList[prevIdx] = reactionsList[prevIdx].copy(count = prevCount)
                                }
                            }
                            if (previousReactionId == "heart") {
                                val likeIdx = actionsList.indexOfFirst { it.id == 2 }
                                if (likeIdx >= 0) {
                                    actionsList[likeIdx] = actionsList[likeIdx].copy(count = maxOf(0, actionsList[likeIdx].count - 1), acted = false)
                                }
                            }
                        }

                        // Add new reaction
                        val newIdx = reactionsList.indexOfFirst { it.id == reactionName }
                        if (newIdx >= 0) {
                            reactionsList[newIdx] = reactionsList[newIdx].copy(count = reactionsList[newIdx].count + 1)
                        } else {
                            reactionsList.add(PostReaction(id = reactionName, type = "emoji", count = 1))
                        }
                        if (reactionName == "heart") {
                            val likeIdx = actionsList.indexOfFirst { it.id == 2 }
                            if (likeIdx >= 0) {
                                actionsList[likeIdx] = actionsList[likeIdx].copy(count = actionsList[likeIdx].count + 1, acted = true)
                            } else {
                                actionsList.add(PostActionSummary(id = 2, count = 1, acted = true))
                            }
                        }

                        p.copy(
                            reactions = reactionsList,
                            actionsSummary = actionsList,
                            currentUserReaction = PostCurrentUserReaction(id = reactionName)
                        )
                    }
                } else p
            } ?: emptyList()

            _uiState.value = _uiState.value.copy(
                currentTopicDetail = detail.copy(
                    postStream = detail.postStream?.copy(posts = updatedPosts)
                )
            )

            // Send to server
            repository.toggleReaction(postId, reactionName)
        }
    }

    fun votePoll(postId: Long, pollName: String, optionId: String) {
        viewModelScope.launch {
            val detail = _uiState.value.currentTopicDetail ?: return@launch
            val post = detail.postStream?.posts?.firstOrNull { it.id == postId } ?: return@launch
            val currentVotes = post.pollsVotes[pollName] ?: emptyList()
            val alreadyVotedThisOption = currentVotes.contains(optionId)

            val newVotesList = if (alreadyVotedThisOption) {
                currentVotes - optionId
            } else {
                listOf(optionId)
            }

            if (newVotesList.isEmpty()) {
                repository.unvotePoll(postId, pollName)
            } else {
                repository.votePoll(postId, pollName, newVotesList)
            }

            val updatedPosts = detail.postStream?.posts?.map { p ->
                if (p.id == postId) {
                    val updatedPolls = p.polls.map { poll ->
                        if (poll.name == pollName) {
                            val updatedOptions = poll.options.map { opt ->
                                val delta = if (opt.id == optionId) {
                                    if (alreadyVotedThisOption) -1 else 1
                                } else if (!alreadyVotedThisOption && currentVotes.contains(opt.id)) {
                                    -1
                                } else 0
                                opt.copy(votes = maxOf(0, opt.votes + delta))
                            }
                            val deltaVoters = if (alreadyVotedThisOption && newVotesList.isEmpty()) -1
                                              else if (!alreadyVotedThisOption && currentVotes.isEmpty()) 1
                                              else 0
                            poll.copy(
                                options = updatedOptions,
                                voters = maxOf(0, poll.voters + deltaVoters)
                            )
                        } else poll
                    }
                    val updatedPollsVotes = p.pollsVotes.toMutableMap()
                    if (newVotesList.isEmpty()) {
                        updatedPollsVotes.remove(pollName)
                    } else {
                        updatedPollsVotes[pollName] = newVotesList
                    }
                    p.copy(polls = updatedPolls, pollsVotes = updatedPollsVotes)
                } else p
            } ?: emptyList()

            _uiState.value = _uiState.value.copy(
                currentTopicDetail = detail.copy(
                    postStream = detail.postStream?.copy(posts = updatedPosts)
                )
            )
        }
    }

    fun toggleTopicVote(topicId: Long) {
        viewModelScope.launch {
            val detail = _uiState.value.currentTopicDetail ?: return@launch
            if (detail.id != topicId) return@launch
            val isVoted = detail.userVoted == true
            val currentCount = detail.voteCount ?: 0

            if (isVoted) {
                repository.unvoteTopic(topicId)
                _uiState.value = _uiState.value.copy(
                    currentTopicDetail = detail.copy(
                        userVoted = false,
                        voteCount = maxOf(0, currentCount - 1)
                    )
                )
            } else {
                repository.voteTopic(topicId)
                _uiState.value = _uiState.value.copy(
                    currentTopicDetail = detail.copy(
                        userVoted = true,
                        voteCount = currentCount + 1
                    )
                )
            }
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
                val streamResult = repository.uploadVideoStream(context, uri)
                _uiState.value = _uiState.value.copy(isLoading = false)
                streamResult.onSuccess { bbcode ->
                    onDone(bbcode, null)
                }.onFailure { err ->
                    onDone(null, err.localizedMessage ?: "Failed to upload video.")
                }
                return@launch
            }

            // Standard image upload without filenames
            val result = repository.uploadFile(context, uri)
            _uiState.value = _uiState.value.copy(isLoading = false)
            result.onSuccess { upload ->
                val targetUrl = upload.shortUrl?.takeIf { it.isNotBlank() } ?: upload.url
                // No file name in markdown tag
                val markdown = "\n![]($targetUrl)\n"
                onDone(markdown, null)
            }.onFailure { err ->
                onDone(null, err.localizedMessage ?: "Failed to upload image.")
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
