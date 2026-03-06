package com.musicapp.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.musicapp.android.ui.components.TrackItem
import com.musicapp.android.viewmodels.LibraryViewModel
import com.musicapp.android.viewmodels.PlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    navController: NavController,
    playlistId: Long,
    libraryViewModel: LibraryViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel()
) {
    val playlists by libraryViewModel.playlists.collectAsState()
    val playlist = playlists.find { it.id == playlistId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(playlist?.name ?: "Playlist") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                playlist == null -> Text("Playlist not found", modifier = Modifier.align(Alignment.Center))
                playlist.tracks.isEmpty() -> Text("This playlist is empty", modifier = Modifier.align(Alignment.Center))
                else -> LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
                    itemsIndexed(playlist.tracks) { index, track ->
                        TrackItem(
                            track = track,
                            onClick = {
                                playerViewModel.playTracks(playlist.tracks, index)
                                playerViewModel.requestExpand()
                            }
                        )
                    }
                }
            }
        }
    }
}
