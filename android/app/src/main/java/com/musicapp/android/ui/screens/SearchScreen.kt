package com.musicapp.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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
    val currentTrackId by playerViewModel.currentTrackId.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        // Header
        Text(
            "Search",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        // Search bar — rounded dark style
        TextField(
            value = query,
            onValueChange = { searchViewModel.onSearchQueryChanged(it) },
            placeholder = {
                Text(
                    "What do you want to listen to?",
                    color = Color.Black.copy(alpha = 0.6f)
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Rounded.Search,
                    contentDescription = null,
                    tint = Color.Black
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(8.dp)),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                cursorColor = Color.Black,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            singleLine = true
        )

        Spacer(Modifier.height(16.dp))

        // Content
        when {
            query.isNotBlank() && isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Brand, strokeWidth = 2.dp)
                }
            }
            query.isNotBlank() && results.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "No results for \"$query\"",
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
            query.isNotBlank() -> {
                LazyColumn(contentPadding = PaddingValues(bottom = 120.dp)) {
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
                // Browse categories grid (Spotify pattern)
                Text(
                    "Browse All",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(categoryNames.zip(categoryColors)) { (name, color) ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(color)
                                .clickable { searchViewModel.onSearchQueryChanged(name) }
                                .padding(12.dp)
                        ) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
