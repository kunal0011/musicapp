package com.musicapp.android.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.musicapp.android.data.local.TrackDao
import com.musicapp.android.ui.screens.*
import com.musicapp.android.viewmodels.PlayerViewModel

@Composable
fun MusicAppNavigation(
    navController: NavHostController,
    paddingValues: PaddingValues,
    playerViewModel: PlayerViewModel,
    trackDao: TrackDao? = null
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
        composable(Screen.LikedSongs.route) {
            LikedSongsScreen(playerViewModel = playerViewModel)
        }
        composable(Screen.Queue.route) {
            QueueScreen(playerViewModel = playerViewModel)
        }
        composable(Screen.Equalizer.route) {
            EqualizerScreen()
        }
        composable(Screen.Offline.route) {
            if (trackDao != null) {
                OfflineScreen(trackDao = trackDao, playerViewModel = playerViewModel)
            }
        }
        composable(
            route = Screen.PlaylistDetail.route,
            arguments = listOf(navArgument("playlistId") { type = NavType.LongType })
        ) { back ->
            val playlistId = back.arguments?.getLong("playlistId") ?: return@composable
            PlaylistDetailScreen(
                navController = navController,
                playlistId = playlistId,
                playerViewModel = playerViewModel
            )
        }
    }
}
