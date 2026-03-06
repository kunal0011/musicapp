package com.musicapp.android.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.musicapp.android.models.Track
import com.musicapp.android.ui.theme.Brand

@Composable
fun TrackItem(
    track: Track,
    onClick: () -> Unit,
    isCurrentlyPlaying: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Cover art — 48dp (Spotify uses smaller thumbnails)
        AsyncImage(
            model = track.coverArtUrl,
            contentDescription = "Cover for ${track.title}",
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(4.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            // Title — green if currently playing
            Text(
                text = track.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isCurrentlyPlaying) FontWeight.Bold else FontWeight.Normal,
                color = if (isCurrentlyPlaying) Brand else Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Playing indicator
                if (isCurrentlyPlaying) {
                    Icon(
                        Icons.Rounded.GraphicEq,
                        contentDescription = "Playing",
                        tint = Brand,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                }
                Text(
                    text = track.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Three-dot menu (Spotify pattern)
        IconButton(
            onClick = { /* show options */ },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Rounded.MoreVert,
                contentDescription = "More options",
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
