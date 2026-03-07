package com.musicapp.android.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.musicapp.android.models.Playlist
import com.musicapp.android.ui.components.PlaylistCoverMosaic
import com.musicapp.android.ui.components.SpotifyBackground
import com.musicapp.android.ui.components.TrackItem
import com.musicapp.android.ui.theme.Brand
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
    val currentTrackId by playerViewModel.currentTrackId.collectAsState()

    SpotifyBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            when {
                playlist == null -> {
                    Text(
                        "Playlist not found",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                playlist.tracks.isEmpty() -> {
                    Text(
                        "This playlist is empty",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                else -> {
                    LazyColumn(contentPadding = PaddingValues(bottom = 120.dp)) {
                        item {
                            PlaylistHeader(
                                playlist = playlist,
                                onBack = { navController.popBackStack() },
                                onPlay = {
                                    playerViewModel.playTracks(playlist.tracks, 0)
                                    playerViewModel.requestExpand()
                                },
                                onShuffle = {
                                    playerViewModel.playTracks(playlist.tracks.shuffled(), 0)
                                    playerViewModel.requestExpand()
                                }
                            )
                        }

                        itemsIndexed(playlist.tracks) { index, track ->
                            TrackItem(
                                track = track,
                                isCurrentlyPlaying = track.id == currentTrackId,
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
}

@Composable
private fun PlaylistHeader(
    playlist: Playlist,
    onBack: () -> Unit,
    onPlay: () -> Unit,
    onShuffle: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(Modifier.height(8.dp))

        PlaylistCoverMosaic(
            coverUrls = playlist.tracks.mapNotNull { it.coverArtUrl }.take(4),
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.CenterHorizontally),
            cornerRadius = 10
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = playlist.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Playlist • ${playlist.tracks.size} songs",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onShuffle,
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
            ) {
                Icon(
                    imageVector = Icons.Rounded.Shuffle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.width(6.dp))
                Text("Shuffle", color = MaterialTheme.colorScheme.onSurface)
            }

            Button(
                onClick = onPlay,
                colors = ButtonDefaults.buttonColors(containerColor = Brand),
                shape = RoundedCornerShape(24.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    tint = Color.Black
                )
                Spacer(Modifier.width(6.dp))
                Text("Play", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}
