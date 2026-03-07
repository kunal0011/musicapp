package com.musicapp.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.musicapp.android.ui.components.SpotifyBackground
import com.musicapp.android.ui.components.TrackItem
import com.musicapp.android.ui.theme.Brand
import com.musicapp.android.viewmodels.PlayerViewModel
import com.musicapp.android.viewmodels.SearchViewModel

// Spotify-style browse category colors
private val categoryColors = listOf(
    Color(0xFFE13300), Color(0xFF1E3264), Color(0xFFE8115B),
    Color(0xFF148A08), Color(0xFF503750), Color(0xFF477D95),
    Color(0xFFB02897), Color(0xFF7358FF), Color(0xFFE91429),
    Color(0xFF27856A), Color(0xFFAF2896), Color(0xFF1DB954),
)

private val categoryNames = listOf(
    "Pop", "Hip-Hop", "Rock", "Indie", "R&B", "Jazz",
    "Electronic", "Classical", "Metal", "Lo-Fi", "Latin", "Trending"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    searchViewModel: SearchViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel()
) {
    val query by searchViewModel.searchQuery.collectAsState()
    val results by searchViewModel.searchResults.collectAsState()
    val isLoading by searchViewModel.isLoading.collectAsState()
    val errorMessage by searchViewModel.errorMessage.collectAsState()
    val currentTrackId by playerViewModel.currentTrackId.collectAsState()

    SpotifyBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            Text(
                text = "Search",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )

            OutlinedTextField(
                value = query,
                onValueChange = { searchViewModel.onSearchQueryChanged(it) },
                placeholder = {
                    Text(
                        "Songs, artists, playlists",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (query.isNotBlank()) {
                        IconButton(onClick = { searchViewModel.onSearchQueryChanged("") }) {
                            Icon(
                                imageVector = Icons.Rounded.Clear,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    cursorColor = Color.Black
                )
            )

            Spacer(Modifier.height(16.dp))

            when {
                query.isNotBlank() && isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Brand, strokeWidth = 2.dp)
                    }
                }

                query.isNotBlank() && errorMessage != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = errorMessage ?: "Search failed",
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = { searchViewModel.onSearchQueryChanged(query) },
                                colors = ButtonDefaults.buttonColors(containerColor = Brand)
                            ) {
                                Text("Retry", color = Color.Black)
                            }
                        }
                    }
                }

                query.isNotBlank() && results.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "No results for \"$query\"",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                query.isNotBlank() -> {
                    LazyColumn(contentPadding = PaddingValues(bottom = 120.dp)) {
                        item {
                            Text(
                                text = "Top results",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }

                        itemsIndexed(results) { index, track ->
                            TrackItem(
                                track = track,
                                isCurrentlyPlaying = track.id == currentTrackId,
                                onClick = {
                                    playerViewModel.playTracks(results, index)
                                    playerViewModel.requestExpand()
                                }
                            )
                        }
                    }
                }

                else -> {
                    Text(
                        "Browse all",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(categoryNames.zip(categoryColors)) { _, (name, color) ->
                            BrowseCategoryCard(
                                name = name,
                                color = color,
                                onClick = { searchViewModel.onSearchQueryChanged(name) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BrowseCategoryCard(
    name: String,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(108.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(color)
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.align(Alignment.TopStart)
        )
        Icon(
            imageVector = Icons.Rounded.MusicNote,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.35f),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(44.dp)
                .graphicsLayer { rotationZ = -20f }
        )
    }
}
