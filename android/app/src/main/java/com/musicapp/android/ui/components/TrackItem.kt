package com.musicapp.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.musicapp.android.models.Track
import com.musicapp.android.ui.theme.Brand
import com.musicapp.android.ui.theme.SurfaceElevated
import com.musicapp.android.ui.theme.TextSecondary

@Composable
fun TrackItem(
    track: Track,
    onClick: () -> Unit,
    isCurrentlyPlaying: Boolean = false,
    onMoreClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (!track.coverArtUrl.isNullOrBlank()) {
                AsyncImage(
                    model = track.coverArtUrl,
                    contentDescription = "Cover for ${track.title}",
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SurfaceElevated),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MusicNote,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isCurrentlyPlaying) FontWeight.Bold else FontWeight.Normal,
                color = if (isCurrentlyPlaying) Brand else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                    text = buildString {
                        append(track.artist)
                        if (track.album.isNotBlank()) {
                            append(" • ")
                            append(track.album)
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        IconButton(
            onClick = { onMoreClick?.invoke() },
            enabled = onMoreClick != null,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                Icons.Rounded.MoreVert,
                contentDescription = "More options",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
