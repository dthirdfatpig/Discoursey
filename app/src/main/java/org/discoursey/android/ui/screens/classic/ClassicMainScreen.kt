package org.discoursey.android.ui.screens.classic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import org.discoursey.android.ui.screens.messages.MessagesAndNotificationsScreen
import org.discoursey.android.ui.screens.photos.PhotoAlbumScreen
import org.discoursey.android.ui.screens.profile.ProfileScreen
import org.discoursey.android.ui.screens.search.SearchScreen
import org.discoursey.android.ui.screens.settings.SettingsScreen
import org.discoursey.android.ui.theme.ClassicBackground
import org.discoursey.android.ui.viewmodel.MainViewModel

@Composable
fun ClassicMainScreen(
    viewModel: MainViewModel,
    onTopicClick: (Long) -> Unit,
    onNewTopicClick: () -> Unit,
    onLogout: () -> Unit,
    onOpenPhotoAlbum: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentView by remember { mutableStateOf("topics") } // "topics", "menu", "messages", "search", "settings", "photos", "profile"

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ClassicBackground)
    ) {
        when (currentView) {
            "topics" -> {
                ClassicTopicListScreen(
                    topics = uiState.topics,
                    isRefreshing = uiState.isRefreshing,
                    hasNewTopics = uiState.hasNewTopics,
                    nameDisplayPreference = uiState.nameDisplayPreference,
                    forumEnablesNames = uiState.forumEnablesNames,
                    onRefresh = { viewModel.refreshTopics(isSwipeToRefresh = true) },
                    onTopicClick = onTopicClick,
                    onHomeClick = { currentView = "menu" },
                    onRefreshClick = { viewModel.refreshTopics() },
                    onOverflowClick = { currentView = "settings" }
                )
            }
            "menu" -> {
                ClassicMenuScreen(
                    userProfile = uiState.userProfile,
                    currentTab = currentView,
                    onTabSelect = { currentView = it },
                    onNavigate = { destination ->
                        when (destination) {
                            "topics" -> currentView = "topics"
                            "messages" -> currentView = "messages"
                            "search" -> currentView = "search"
                            "photos" -> currentView = "photos"
                            "full_profile" -> currentView = "profile"
                            "settings" -> currentView = "settings"
                            "bookmarks", "replies", "notifications" -> currentView = "messages"
                            else -> currentView = destination
                        }
                    },
                    onRefresh = { viewModel.refreshAll() },
                    onToggleNightMode = {
                        val next = if (uiState.themeMode == "CLASSIC_DARK") "LIGHT" else "CLASSIC_DARK"
                        viewModel.setThemeMode(next)
                    }
                )
            }
            "messages", "notifications" -> {
                MessagesAndNotificationsScreen(
                    viewModel = viewModel,
                    onTopicClick = onTopicClick,
                    onBack = { currentView = "topics" }
                )
            }
            "photos" -> {
                PhotoAlbumScreen(
                    viewModel = viewModel,
                    onBack = { currentView = "menu" }
                )
            }
            "search" -> {
                SearchScreen(
                    viewModel = viewModel,
                    onTopicClick = onTopicClick,
                    onBack = { currentView = "topics" }
                )
            }
            "profile" -> {
                ProfileScreen(
                    viewModel = viewModel,
                    onBack = { currentView = "menu" },
                    onTopicClick = onTopicClick
                )
            }
            "settings" -> {
                SettingsScreen(
                    viewModel = viewModel,
                    onBack = { currentView = "topics" },
                    onLogout = onLogout
                )
            }
        }
    }
}
