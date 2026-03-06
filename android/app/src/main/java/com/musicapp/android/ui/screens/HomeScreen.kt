package com.musicapp.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
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
import coil.compose.AsyncImage
import com.musicapp.android.models.Track
import com.musicapp.android.ui.components.TrackItem
import com.musicapp.android.ui.theme.Brand
import com.musicapp.android.ui.theme.SurfaceElevated
import com.musicapp.android.viewmodels.HomeViewModel
import com.musicapp.android.viewmodels.PlayerViewModel
import java.util.Calendar

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel()
) {
    val tracks by homeViewModel.tracks.collectAsState()
    val recentlyPlayed by homeViewModel.recentlyPlayed.collectAsState()
    val isLoading by homeViewModel.isLoading.collectAsState()
    val errorMessage by homeViewModel.errorMessage.collectAsState()
    val currentTrackId by playerViewModel.currentTrackId.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Greeting
        item {
            Text(
                text = greeting(),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            )
        }

        // Quick-play grid — 2 columns, 6 items max (Spotify Home signature)
        if (recentlyPlayed.isNotEmpty()) {
            item {
                QuickPlayGrid(
                    tracks = recentlyPlayed.take(6),
                    allTracks = recentlyPlayed,
                    playerViewModel = playerViewModel
                )
                Spacer(Modifier.height(24.dp))
            }
        }

        // Recently Played carousel
        if (recentlyPlayed.size > 6) {
            item {
                SectionHeader("Recently Played")
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(recentlyPlayed.drop(6).take(10)) { index, track ->
                        SpotifyTrackCard(track = track, onClick = {
                            playerViewModel.playTracks(recentlyPlayed, index + 6)
                            playerViewModel.requestExpand()
                        })
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }

        // All tracks
        item { SectionHeader("All Songs") }

        when {
            isLoading -> item {
                Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Brand, strokeWidth = 2.dp)
                }
            }
            errorMessage != null -> item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(errorMessage ?: "", color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { homeViewModel.load() },
                        colors = ButtonDefaults.buttonColors(containerColor = Brand)
                    ) {
                        Text("Retry", color = Color.Black)
                    }
                }
            }
            else -> itemsIndexed(tracks) { index, track ->
                TrackItem(
                    track = track,
                    isCurrentlyPlaying = track.id == currentTrackId,
                    onClick = {
                        playerViewModel.playTracks(tracks, index)
                        playerViewModel.requestExpand()
                    }
                )
            }
        }
    }
}

/**
 * Spotify-style 2-column quick-play grid showing 6 recently played tracks.
 */
@Composable
private fun QuickPlayGrid(
    tracks: List<Track>,
    allTracks: List<Track>,
    playerViewModel: PlayerViewModel
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tracks.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { track ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = SurfaceElevated,
                        onClick = {
                            val idx = allTracks.indexOf(track)
                            if (idx >= 0) {
                                playerViewModel.playTracks(allTracks, idx)
                                playerViewModel.requestExpand()
                            }
                        }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = track.coverArtUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Text(
                                text = track.title,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }
                }
                // Fill remaining space if odd number
                if (row.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun SpotifyTrackCard(track: Track, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.Start
    ) {
        AsyncImage(
            model = track.coverArtUrl,
            contentDescription = track.title,
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceElevated),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = track.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = track.artist,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.6f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun greeting(): String {
    return when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 5..11 -> "Good morning"
        in 12..17 -> "Good afternoon"
        else -> "Good evening"
    }
}
