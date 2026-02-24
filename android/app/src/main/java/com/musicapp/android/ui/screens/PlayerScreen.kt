package com.musicapp.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.musicapp.android.viewmodels.PlayerViewModel
import com.musicapp.android.viewmodels.LibraryViewModel
import com.musicapp.android.models.Playlist
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    onMinimize: () -> Unit,
    playerViewModel: PlayerViewModel = hiltViewModel(),
    libraryViewModel: LibraryViewModel = hiltViewModel()
) {
    var showPlaylistDialog by remember { mutableStateOf(false) }
    val isPlaying by playerViewModel.isPlaying.collectAsState()
    val currentTrackTitle by playerViewModel.currentTrackTitle.collectAsState()
    val currentArtist by playerViewModel.currentArtist.collectAsState()
    val currentCoverUrl by playerViewModel.currentCoverUrl.collectAsState()
    val isShuffleEnabled by playerViewModel.isShuffleEnabled.collectAsState()
    val currentPosition by playerViewModel.currentPosition.collectAsState()
    val duration by playerViewModel.duration.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onMinimize) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown, 
                    contentDescription = "Minimize", 
                    modifier = Modifier.size(36.dp)
                )
            }
            Text(
                text = "NOW PLAYING", 
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { showPlaylistDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Add, 
                    contentDescription = "Add to Playlist", 
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Album Art
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (currentCoverUrl.isNotEmpty()) {
                AsyncImage(
                    model = currentCoverUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Track Info
        Text(
            text = currentTrackTitle,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = currentArtist,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Seek Bar
        Slider(
            value = if (duration > 0) currentPosition.toFloat() else 0f,
            valueRange = 0f..(if (duration > 0) duration.toFloat() else 100f),
            onValueChange = { playerViewModel.seekTo(it.toLong()) },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(formatTime(currentPosition), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(formatTime(duration), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Playback Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { playerViewModel.toggleShuffle() }) {
                Icon(
                    painter = painterResource(android.R.drawable.ic_menu_sort_by_size),
                    contentDescription = "Shuffle",
                    modifier = Modifier.size(28.dp),
                    tint = if(isShuffleEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
            IconButton(onClick = { playerViewModel.skipToPrevious() }) {
                Icon(
                    painter = painterResource(android.R.drawable.ic_media_previous),
                    contentDescription = "Previous",
                    modifier = Modifier.size(48.dp)
                )
            }
            
            IconButton(
                onClick = { playerViewModel.togglePlayPause() },
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                Icon(
                    painter = if (isPlaying) painterResource(android.R.drawable.ic_media_pause) else painterResource(android.R.drawable.ic_media_play),
                    contentDescription = "Play/Pause",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(40.dp)
                )
            }

            IconButton(onClick = { playerViewModel.skipToNext() }) {
                Icon(
                    painter = painterResource(android.R.drawable.ic_media_next),
                    contentDescription = "Next",
                    modifier = Modifier.size(48.dp)
                )
            }
            
            IconButton(onClick = { }) {
                Icon(
                    painter = painterResource(android.R.drawable.ic_menu_revert),
                    contentDescription = "Repeat",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }

    if (showPlaylistDialog) {
        ModalBottomSheet(
            onDismissRequest = { showPlaylistDialog = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            PlaylistSelection(
                libraryViewModel = libraryViewModel,
                onPlaylistSelected = { playlist ->
                    playerViewModel.currentTrackId.value?.let { trackId ->
                        libraryViewModel.addTrackToPlaylist(playlist.id, trackId)
                    }
                    showPlaylistDialog = false
                }
            )
        }
    }
}

@Composable
fun PlaylistSelection(
    libraryViewModel: LibraryViewModel,
    onPlaylistSelected: (Playlist) -> Unit
) {
    val playlists by libraryViewModel.playlists.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Save to Playlist", 
            style = MaterialTheme.typography.titleLarge, 
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (playlists.isEmpty()) {
            Text("No playlists available", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            LazyColumn {
                items(playlists) { playlist ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPlaylistSelected(playlist) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(playlist.name, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

private fun formatTime(ms: Long): String {
    if (ms <= 0) return "0:00"
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
}
