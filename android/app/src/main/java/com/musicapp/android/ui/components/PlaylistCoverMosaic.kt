package com.musicapp.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

/**
 * Auto-generated mosaic of up to 4 track cover art images in a 2×2 grid.
 * Falls back to single cover or gradient placeholder.
 */
@Composable
fun PlaylistCoverMosaic(
    coverUrls: List<String>,
    modifier: Modifier = Modifier,
    cornerRadius: Int = 8
) {
    val shape = RoundedCornerShape(cornerRadius.dp)

    when {
        coverUrls.isEmpty() -> {
            // Gradient placeholder
            Box(
                modifier = modifier
                    .clip(shape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF667eea),
                                Color(0xFF764ba2)
                            )
                        )
                    )
            )
        }
        coverUrls.size == 1 -> {
            // Single cover
            AsyncImage(
                model = coverUrls[0],
                contentDescription = "Playlist cover",
                modifier = modifier.clip(shape),
                contentScale = ContentScale.Crop
            )
        }
        coverUrls.size < 4 -> {
            // Use first cover
            AsyncImage(
                model = coverUrls[0],
                contentDescription = "Playlist cover",
                modifier = modifier.clip(shape),
                contentScale = ContentScale.Crop
            )
        }
        else -> {
            // 2×2 grid mosaic
            Box(modifier = modifier.clip(shape)) {
                Column {
                    Row(Modifier.weight(1f)) {
                        AsyncImage(
                            model = coverUrls[0],
                            contentDescription = null,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            contentScale = ContentScale.Crop
                        )
                        AsyncImage(
                            model = coverUrls[1],
                            contentDescription = null,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Row(Modifier.weight(1f)) {
                        AsyncImage(
                            model = coverUrls[2],
                            contentDescription = null,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            contentScale = ContentScale.Crop
                        )
                        AsyncImage(
                            model = coverUrls[3],
                            contentDescription = null,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}
