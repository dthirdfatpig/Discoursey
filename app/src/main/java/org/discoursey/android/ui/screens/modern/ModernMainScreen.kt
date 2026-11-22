package org.discoursey.android.ui.screens.modern

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import org.discoursey.android.ui.screens.messages.MessagesAndNotificationsScreen
import org.discoursey.android.ui.screens.profile.ProfileScreen
import org.discoursey.android.ui.screens.search.SearchScreen
import org.discoursey.android.ui.screens.settings.SettingsScreen
import org.discoursey.android.ui.theme.ModernBackground
import org.discoursey.android.ui.viewmodel.MainViewModel

@Composable
fun ModernMainScreen(
    viewModel: MainViewModel,
    onTopicClick: (Long) -> Unit,
    onNewTopicClick: () -> Unit,
    onLogout: () -> Unit,
    onOpenPhotoAlbum: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentTab by remember { mutableStateOf("home") }

    Scaffold(
        bottomBar = {
            ModernBottomBar(
                slots = uiState.bottomBarSlots,
                currentTab = currentTab,
                onTabSelected = { currentTab = it },
                unreadMessagesCount = uiState.unreadNotificationCount
            )
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(ModernBackground)
        ) {
            when (currentTab) {
                "home", "latest", "categories" -> {
                    ModernTopicListScreen(
                        topics = uiState.topics,
                        baseUrl = uiState.forumUrl,
                        isRefreshing = uiState.isRefreshing,
                        hasNewTopics = uiState.hasNewTopics,
                        nameDisplayPreference = uiState.nameDisplayPreference,
                        forumEnablesNames = uiState.forumEnablesNames,
                        onRefresh = { viewModel.refreshTopics(isSwipeToRefresh = true) },
                        onTopicClick = onTopicClick,
                        onNewTopicClick = onNewTopicClick,
                        onBackClick = null // No dead back arrow on root home
                    )
                }
                "search" -> {
                    SearchScreen(
                        viewModel = viewModel,
                        onTopicClick = onTopicClick,
                        onBack = { currentTab = "home" }
                    )
                }
                "messages" -> {
                    MessagesAndNotificationsScreen(
                        viewModel = viewModel,
                        onTopicClick = onTopicClick,
                        onBack = { currentTab = "home" }
                    )
                }
                "profile", "bookmarks" -> {
                    ProfileScreen(
                        viewModel = viewModel,
                        onBack = { currentTab = "home" },
                        onTopicClick = onTopicClick
                    )
                }
                "menu" -> {
                    SettingsScreen(
                        viewModel = viewModel,
                        onBack = { currentTab = "home" },
                        onLogout = onLogout
                    )
                }
                else -> {
                    ModernTopicListScreen(
                        topics = uiState.topics,
                        baseUrl = uiState.forumUrl,
                        isRefreshing = uiState.isRefreshing,
                        hasNewTopics = uiState.hasNewTopics,
                        nameDisplayPreference = uiState.nameDisplayPreference,
                        forumEnablesNames = uiState.forumEnablesNames,
                        onRefresh = { viewModel.refreshTopics(isSwipeToRefresh = true) },
                        onTopicClick = onTopicClick,
                        onNewTopicClick = onNewTopicClick,
                        onBackClick = { currentTab = "home" }
                    )
                }
            }
        }
    }
}
