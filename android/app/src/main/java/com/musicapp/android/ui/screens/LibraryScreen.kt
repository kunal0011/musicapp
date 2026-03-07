package com.musicapp.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.musicapp.android.models.Playlist
import com.musicapp.android.ui.components.SpotifyBackground
import com.musicapp.android.ui.navigation.Screen
import com.musicapp.android.ui.theme.Brand
import com.musicapp.android.ui.theme.SurfaceElevated
import com.musicapp.android.viewmodels.LibraryViewModel

private enum class LibraryFilter(val label: String) {
    Playlists("Playlists"),
    Collections("Collections")
}

@Composable
fun LibraryScreen(
    navController: NavController,
    libraryViewModel: LibraryViewModel = hiltViewModel()
) {
    val playlists by libraryViewModel.playlists.collectAsState()
    val likedTracks by libraryViewModel.likedTracks.collectAsState()
    val isLoading by libraryViewModel.isLoading.collectAsState()
    val errorMessage by libraryViewModel.errorMessage.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(LibraryFilter.Playlists) }

    SpotifyBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Your Library",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "${playlists.size} playlists • ${likedTracks.size} liked songs",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { showCreateDialog = true }) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Create playlist",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(LibraryFilter.values().toList()) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = {
                            Text(
                                filter.label,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selectedFilter == filter) Color.Black else MaterialTheme.colorScheme.onSurface
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = SurfaceElevated,
                            selectedContainerColor = Brand,
                            selectedLabelColor = Color.Black
                        ),
                        shape = RoundedCornerShape(20.dp),
                        border = null
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            LibraryQuickActions(
                onLikedClick = { navController.navigate(Screen.LikedSongs.route) },
                onOfflineClick = { navController.navigate(Screen.Offline.route) },
                onQueueClick = { navController.navigate(Screen.Queue.route) }
            )

            Spacer(Modifier.height(8.dp))

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    isLoading && playlists.isEmpty() -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = Brand,
                            strokeWidth = 2.dp
                        )
                    }

                    errorMessage != null -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = errorMessage ?: "Failed to load library",
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.height(10.dp))
                            Button(
                                onClick = { libraryViewModel.loadAll() },
                                colors = ButtonDefaults.buttonColors(containerColor = Brand)
                            ) {
                                Text("Retry", color = Color.Black)
                            }
                        }
                    }

                    selectedFilter == LibraryFilter.Playlists && playlists.isEmpty() -> {
                        EmptyPlaylistsState(onCreateClick = { showCreateDialog = true })
                    }

                    selectedFilter == LibraryFilter.Collections -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 120.dp)
                        ) {
                            item {
                                CollectionRow(
                                    icon = Icons.Default.Favorite,
                                    title = "Liked Songs",
                                    subtitle = "${likedTracks.size} songs",
                                    onClick = { navController.navigate(Screen.LikedSongs.route) }
                                )
                            }
                            item {
                                CollectionRow(
                                    icon = Icons.Default.Download,
                                    title = "Downloaded",
                                    subtitle = "Available offline",
                                    onClick = { navController.navigate(Screen.Offline.route) }
                                )
                            }
                            item {
                                CollectionRow(
                                    icon = Icons.AutoMirrored.Filled.QueueMusic,
                                    title = "Queue",
                                    subtitle = "Up next and now playing",
                                    onClick = { navController.navigate(Screen.Queue.route) }
                                )
                            }
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 120.dp)
                        ) {
                            items(playlists) { playlist ->
                                SpotifyPlaylistItem(
                                    playlist = playlist,
                                    onClick = { navController.navigate(Screen.PlaylistDetail.createRoute(playlist.id)) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Create dialog
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            containerColor = Color(0xFF282828),
            title = { Text("New Playlist", color = Color.White) },
            text = {
                TextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    placeholder = { Text("Playlist name", color = Color.White.copy(alpha = 0.5f)) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF404040),
                        unfocusedContainerColor = Color(0xFF404040),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Brand,
                        focusedIndicatorColor = Brand,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            libraryViewModel.createPlaylist(newPlaylistName)
                            newPlaylistName = ""
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Brand)
                ) {
                    Text("Create", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}

@Composable
private fun EmptyPlaylistsState(onCreateClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.LibraryMusic,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "No playlists yet",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            "Create your first playlist to save favorite tracks",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )
        Button(
            onClick = onCreateClick,
            colors = ButtonDefaults.buttonColors(containerColor = Brand)
        ) {
            Text("Create Playlist", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun LibraryQuickActions(
    onLikedClick: () -> Unit,
    onOfflineClick: () -> Unit,
    onQueueClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuickActionTile(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Favorite,
            label = "Liked",
            onClick = onLikedClick
        )
        QuickActionTile(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Download,
            label = "Offline",
            onClick = onOfflineClick
        )
        QuickActionTile(
            modifier = Modifier.weight(1f),
            icon = Icons.AutoMirrored.Filled.QueueMusic,
            label = "Queue",
            onClick = onQueueClick
        )
    }
}

@Composable
private fun QuickActionTile(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = SurfaceElevated,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun CollectionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceElevated),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SpotifyPlaylistItem(playlist: Playlist, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = playlist.coverUrl,
            contentDescription = "Playlist Cover",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(SurfaceElevated)
        )

        Spacer(Modifier.width(12.dp))

        Column {
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Playlist • ${playlist.tracks.size} songs",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
