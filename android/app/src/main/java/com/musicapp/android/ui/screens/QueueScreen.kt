package com.musicapp.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.musicapp.android.models.Track
import com.musicapp.android.viewmodels.PlayerViewModel

@Composable
fun QueueScreen(
    playerViewModel: PlayerViewModel = hiltViewModel()
) {
    val queue by playerViewModel.queue.collectAsState()
    val currentTrackId by playerViewModel.currentTrackId.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.QueueMusic, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
            Text("Queue", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }

        if (queue.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Queue is empty", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            val upcoming = if (currentTrackId != null) {
                val idx = queue.indexOfFirst { it.id == currentTrackId }
                if (idx >= 0) queue.drop(idx + 1) else queue
            } else queue

            LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
                // Now playing
                currentTrackId?.let { id ->
                    queue.find { it.id == id }?.let { current ->
                        item {
                            Text(
                                "Now Playing",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                            QueueTrackRow(track = current, isCurrent = true, onClick = {}, showDragHandle = false)
                        }
                    }
                }

                if (upcoming.isNotEmpty()) {
                    item {
                        Text(
                            "Next Up",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    itemsIndexed(upcoming, key = { _, track -> track.id }) { index, track ->
                        QueueTrackRow(
                            track = track,
                            isCurrent = false,
                            onClick = {
                                val idx = queue.indexOf(track)
                                if (idx >= 0) playerViewModel.playTracks(queue, idx)
                            },
                            showDragHandle = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QueueTrackRow(
    track: Track,
    isCurrent: Boolean,
    onClick: () -> Unit,
    showDragHandle: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (!isCurrent) Modifier.clickable(onClick = onClick) else Modifier)
            .background(if (isCurrent) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else androidx.compose.ui.graphics.Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showDragHandle) {
            Icon(
                Icons.Rounded.DragHandle,
                contentDescription = "Reorder",
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 8.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        AsyncImage(
            model = track.coverArtUrl,
            contentDescription = null,
            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(6.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
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
}
