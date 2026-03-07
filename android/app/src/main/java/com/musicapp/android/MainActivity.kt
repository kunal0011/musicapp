package com.musicapp.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.*
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.musicapp.android.data.local.TrackDao
import com.musicapp.android.ui.components.MiniPlayer
import com.musicapp.android.ui.components.SpotifyBackground
import com.musicapp.android.ui.navigation.MusicAppNavigation
import com.musicapp.android.ui.navigation.Screen
import com.musicapp.android.ui.screens.LoginScreen
import com.musicapp.android.ui.screens.PlayerScreen
import com.musicapp.android.ui.screens.RegisterScreen
import com.musicapp.android.ui.theme.MusicAppTheme
import com.musicapp.android.ui.theme.SurfaceDark
import com.musicapp.android.utils.TokenManager
import com.musicapp.android.viewmodels.PlayerViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var tokenManager: TokenManager

    @Inject
    lateinit var trackDao: TrackDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MusicAppTheme {
                if (tokenManager.isLoggedIn()) {
                    MainApp(trackDao = trackDao)
                } else {
                    AuthFlow(onAuthSuccess = { recreate() })
                }
            }
        }
    }
}

@Composable
private fun AuthFlow(onAuthSuccess: () -> Unit) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = onAuthSuccess,
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = onAuthSuccess,
                onNavigateToLogin = { navController.popBackStack() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainApp(trackDao: TrackDao) {
    val navController = rememberNavController()
    val playerViewModel: PlayerViewModel = hiltViewModel()
    val currentTrackTitle by playerViewModel.currentTrackTitle.collectAsState()
    val showPlayerSheet by playerViewModel.showPlayerSheet.collectAsState()
    val hasTrack = currentTrackTitle != "No track selected"

    SpotifyBackground(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                Column {
                    // Mini Player above nav bar
                    AnimatedVisibility(
                        visible = hasTrack && !showPlayerSheet,
                        enter = slideInVertically { it } + fadeIn(),
                        exit = slideOutVertically { it } + fadeOut()
                    ) {
                        MiniPlayer(playerViewModel = playerViewModel)
                    }
                    // Bottom nav
                    SpotifyBottomNav(navController = navController)
                }
            }
        ) { innerPadding ->
            MusicAppNavigation(
                navController = navController,
                paddingValues = innerPadding,
                playerViewModel = playerViewModel,
                trackDao = trackDao
            )
        }

        // Full-screen player overlay — slides up from bottom
        AnimatedVisibility(
            visible = showPlayerSheet,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            PlayerScreen(onMinimize = { playerViewModel.dismissPlayerSheet() })
        }
    }
}

data class NavItem(val screen: Screen, val icon: ImageVector, val label: String)

@Composable
private fun SpotifyBottomNav(navController: NavController) {
    val items = listOf(
        NavItem(Screen.Home, Icons.Rounded.Home, "Home"),
        NavItem(Screen.Search, Icons.Rounded.Search, "Search"),
        NavItem(Screen.Library, Icons.Rounded.LibraryMusic, "Your Library"),
    )
    val libraryRoutes = setOf(
        Screen.Library.route,
        Screen.LikedSongs.route,
        Screen.Queue.route,
        Screen.Offline.route,
    )

    NavigationBar(
        containerColor = SurfaceDark.copy(alpha = 0.95f),
        contentColor = Color.White,
        tonalElevation = 0.dp
    ) {
        val entry by navController.currentBackStackEntryAsState()
        val current = entry?.destination?.route

        items.forEach { item ->
            val isSelected = when (item.screen) {
                Screen.Library -> {
                    current in libraryRoutes || current?.startsWith("playlist_detail") == true
                }

                else -> current == item.screen.route
            }

            NavigationBarItem(
                icon = {
                    Icon(
                        item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        item.label,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                selected = isSelected,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = Color.White,
                    unselectedIconColor = Color(0xFF727272),
                    unselectedTextColor = Color(0xFF727272),
                    indicatorColor = Color.Transparent
                ),
                onClick = {
                    navController.navigate(item.screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
