package org.discoursey.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.discoursey.android.data.preferences.AppPreferences
import org.discoursey.android.ui.screens.classic.ClassicMainScreen
import org.discoursey.android.ui.screens.composer.ComposerScreen
import org.discoursey.android.ui.screens.login.LoginScreen
import org.discoursey.android.ui.screens.modern.ModernMainScreen
import org.discoursey.android.ui.screens.photos.PhotoAlbumScreen
import org.discoursey.android.ui.screens.topic.TopicDetailScreen
import org.discoursey.android.ui.theme.DiscourseyTheme
import org.discoursey.android.ui.viewmodel.MainViewModel
import org.discoursey.android.ui.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        val app = application as DiscourseyApp
        ViewModelFactory(app.discourseRepository, app.appPreferences, app.cacheManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val uiState by viewModel.uiState.collectAsState()

            DiscourseyTheme(themeMode = uiState.themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()

                    val startDestination = if (uiState.isLoggedIn) "main" else "login"

                    NavHost(navController = navController, startDestination = startDestination) {
                        composable("login") {
                            LoginScreen(
                                viewModel = viewModel,
                                onLoginSuccess = {
                                    navController.navigate("main") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("main") {
                            if (uiState.uiMode == AppPreferences.MODE_CLASSIC) {
                                ClassicMainScreen(
                                    viewModel = viewModel,
                                    onTopicClick = { topicId ->
                                        navController.navigate("topic/$topicId")
                                    },
                                    onNewTopicClick = {
                                        navController.navigate("composer/new")
                                    },
                                    onLogout = {
                                        navController.navigate("login") {
                                            popUpTo("main") { inclusive = true }
                                        }
                                    },
                                    onOpenPhotoAlbum = {
                                        navController.navigate("albums")
                                    }
                                )
                            } else {
                                ModernMainScreen(
                                    viewModel = viewModel,
                                    onTopicClick = { topicId ->
                                        navController.navigate("topic/$topicId")
                                    },
                                    onNewTopicClick = {
                                        navController.navigate("composer/new")
                                    },
                                    onLogout = {
                                        navController.navigate("login") {
                                            popUpTo("main") { inclusive = true }
                                        }
                                    },
                                    onOpenPhotoAlbum = {
                                        navController.navigate("albums")
                                    }
                                )
                            }
                        }

                        composable(
                            route = "topic/{topicId}",
                            arguments = listOf(navArgument("topicId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val topicId = backStackEntry.arguments?.getLong("topicId") ?: 0L
                            TopicDetailScreen(
                                topicId = topicId,
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() },
                                onReplyClick = { id ->
                                    navController.navigate("composer/reply/$id")
                                }
                            )
                        }

                        composable("composer/new") {
                            ComposerScreen(
                                topicId = null,
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() },
                                onOpenPhotoAlbum = { navController.navigate("albums") }
                            )
                        }

                        composable(
                            route = "composer/reply/{topicId}",
                            arguments = listOf(navArgument("topicId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val topicId = backStackEntry.arguments?.getLong("topicId") ?: 0L
                            ComposerScreen(
                                topicId = topicId,
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() },
                                onOpenPhotoAlbum = { navController.navigate("albums") }
                            )
                        }

                        composable("albums") {
                            PhotoAlbumScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() },
                                onImageSelected = { imageUrl ->
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
