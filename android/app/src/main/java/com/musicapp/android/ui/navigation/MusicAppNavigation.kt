package com.musicapp.android.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.musicapp.android.ui.screens.HomeScreen
import com.musicapp.android.ui.screens.LibraryScreen
import com.musicapp.android.ui.screens.PlayerScreen
import com.musicapp.android.ui.screens.SearchScreen
import com.musicapp.android.ui.screens.PlaylistDetailScreen

import com.musicapp.android.viewmodels.PlayerViewModel

@Composable
fun MusicAppNavigation(
    navController: NavHostController,
    paddingValues: PaddingValues,
    playerViewModel: PlayerViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = Modifier.padding(paddingValues)
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController, playerViewModel = playerViewModel)
        }
        composable(Screen.Search.route) {
            SearchScreen(navController = navController, playerViewModel = playerViewModel)
        }
        composable(Screen.Library.route) {
            LibraryScreen(navController = navController)
        }
        composable(
            route = Screen.PlaylistDetail.route,
            arguments = listOf(navArgument("playlistId") { type = NavType.LongType })
        ) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getLong("playlistId") ?: return@composable
            PlaylistDetailScreen(
                navController = navController, 
                playlistId = playlistId,
                playerViewModel = playerViewModel
            )
        }
    }
}
