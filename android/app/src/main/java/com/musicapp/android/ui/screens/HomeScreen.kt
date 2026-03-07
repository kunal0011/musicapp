package com.musicapp.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
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
import com.musicapp.android.ui.components.ShimmerTrackList
import com.musicapp.android.ui.components.SpotifyBackground
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

    val madeForYou = remember(tracks) { tracks.take(12) }

    SpotifyBackground {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 128.dp)
        ) {
            item {
                HomeHeader()
            }

            if (recentlyPlayed.isNotEmpty()) {
                item { SectionHeader(title = "Jump back in", subtitle = "Your recent favorites") }
                item {
                    QuickPlayGrid(
                        tracks = recentlyPlayed.take(6),
                        allTracks = recentlyPlayed,
                        playerViewModel = playerViewModel
                    )
                    Spacer(Modifier.height(20.dp))
                }
            }

            if (recentlyPlayed.size > 6) {
                item { SectionHeader(title = "Recently played") }
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
                    Spacer(Modifier.height(16.dp))
                }
            }

            if (madeForYou.isNotEmpty()) {
                item { SectionHeader(title = "Made for you", subtitle = "Fresh picks based on your plays") }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(madeForYou) { index, track ->
                            SpotifyTrackCard(track = track, onClick = {
                                playerViewModel.playTracks(madeForYou, index)
                                playerViewModel.requestExpand()
                            })
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }

            item { SectionHeader(title = "All songs", subtitle = "${tracks.size} tracks") }

            when {
                isLoading -> item {
                    ShimmerTrackList(count = 8)
                }

                errorMessage != null -> item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = errorMessage ?: "Something went wrong",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
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
}

@Composable
private fun HomeHeader() {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(
            text = greeting(),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Keep the music going",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

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
                            .height(64.dp),
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
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!track.coverArtUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = track.coverArtUrl,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color(0xFF1F1F1F)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.MusicNote,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            Text(
                                text = track.title,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
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
private fun SectionHeader(title: String, subtitle: String? = null) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SpotifyTrackCard(track: Track, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceElevated),
            contentAlignment = Alignment.Center
        ) {
            if (!track.coverArtUrl.isNullOrBlank()) {
                AsyncImage(
                    model = track.coverArtUrl,
                    contentDescription = track.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.MusicNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = track.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = track.artist,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
