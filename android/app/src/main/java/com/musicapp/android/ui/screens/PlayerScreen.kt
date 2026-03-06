package com.musicapp.android.ui.screens

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.musicapp.android.models.Playlist
import com.musicapp.android.ui.theme.SurfaceBlack
import com.musicapp.android.ui.theme.Brand
import com.musicapp.android.viewmodels.LibraryViewModel
import com.musicapp.android.viewmodels.PlayerViewModel
import com.musicapp.android.viewmodels.RepeatMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    onMinimize: () -> Unit,
    playerViewModel: PlayerViewModel = hiltViewModel(),
    libraryViewModel: LibraryViewModel = hiltViewModel()
) {
    val isPlaying by playerViewModel.isPlaying.collectAsState()
    val title by playerViewModel.currentTrackTitle.collectAsState()
    val artist by playerViewModel.currentArtist.collectAsState()
    val coverUrl by playerViewModel.currentCoverUrl.collectAsState()
    val isShuffleEnabled by playerViewModel.isShuffleEnabled.collectAsState()
    val repeatMode by playerViewModel.repeatMode.collectAsState()
    val currentPosition by playerViewModel.currentPosition.collectAsState()
    val duration by playerViewModel.duration.collectAsState()
    val isLiked by playerViewModel.isCurrentTrackLiked.collectAsState()
    val sleepTimerRemaining by playerViewModel.sleepTimerRemainingMs.collectAsState()

    var showPlaylistSheet by remember { mutableStateOf(false) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var dominantColor by remember { mutableStateOf(Color(0xFF282828)) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Extract dominant color from album art
    LaunchedEffect(coverUrl) {
        if (coverUrl.isNotEmpty()) {
            scope.launch {
                val bitmap = loadBitmap(context, coverUrl)
                bitmap?.let {
                    val palette = withContext(Dispatchers.Default) { Palette.from(it).generate() }
                    val vibrant = palette.getDarkVibrantColor(palette.getDarkMutedColor(0xFF282828.toInt()))
                    dominantColor = Color(vibrant)
                }
            }
        }
    }

    val animatedBg by animateColorAsState(
        targetValue = dominantColor,
        animationSpec = tween(800),
        label = "bg"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(animatedBg, SurfaceBlack),
                    startY = 0f,
                    endY = 1400f
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount > 80) onMinimize()
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // Drag handle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White.copy(alpha = 0.3f))
                )
            }

            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onMinimize) {
                    Icon(
                        Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Minimize",
                        modifier = Modifier.size(32.dp),
                        tint = Color.White
                    )
                }
                Text(
                    "NOW PLAYING",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    letterSpacing = 2.sp
                )
                IconButton(onClick = { /* more options */ }) {
                    Icon(
                        Icons.Rounded.MoreVert,
                        contentDescription = "More",
                        modifier = Modifier.size(24.dp),
                        tint = Color.White
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Album Art — large and centered
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF282828))
            ) {
                if (coverUrl.isNotEmpty()) {
                    AsyncImage(
                        model = coverUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // Track info + like + playlist
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = artist,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row {
                    IconButton(onClick = { playerViewModel.toggleLike() }) {
                        Icon(
                            imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = if (isLiked) "Unlike" else "Like",
                            tint = if (isLiked) Brand else Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Seek bar — Spotify style (thin, green)
            Slider(
                value = if (duration > 0) currentPosition.toFloat() else 0f,
                valueRange = 0f..(if (duration > 0) duration.toFloat() else 1f),
                onValueChange = { playerViewModel.seekTo(it.toLong()) },
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White,
                    inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 0.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    formatMs(currentPosition),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
                Text(
                    formatMs(duration),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            Spacer(Modifier.height(12.dp))

            // Main playback controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle
                IconButton(onClick = { playerViewModel.toggleShuffle() }) {
                    Icon(
                        Icons.Rounded.Shuffle,
                        contentDescription = "Shuffle",
                        modifier = Modifier.size(24.dp),
                        tint = if (isShuffleEnabled) Brand else Color.White.copy(alpha = 0.7f)
                    )
                }
                // Previous
                IconButton(
                    onClick = { playerViewModel.skipToPrevious() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Rounded.SkipPrevious,
                        contentDescription = "Previous",
                        modifier = Modifier.size(36.dp),
                        tint = Color.White
                    )
                }
                // Play/Pause — large circle
                IconButton(
                    onClick = { playerViewModel.togglePlayPause() },
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.Black,
                        modifier = Modifier.size(36.dp)
                    )
                }
                // Next
                IconButton(
                    onClick = { playerViewModel.skipToNext() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Rounded.SkipNext,
                        contentDescription = "Next",
                        modifier = Modifier.size(36.dp),
                        tint = Color.White
                    )
                }
                // Repeat
                IconButton(onClick = { playerViewModel.toggleRepeat() }) {
                    val (icon, tint) = when (repeatMode) {
                        RepeatMode.OFF -> Pair(Icons.Rounded.Repeat, Color.White.copy(alpha = 0.7f))
                        RepeatMode.ALL -> Pair(Icons.Rounded.Repeat, Brand)
                        RepeatMode.ONE -> Pair(Icons.Rounded.RepeatOne, Brand)
                    }
                    Icon(icon, contentDescription = "Repeat", modifier = Modifier.size(24.dp), tint = tint)
                }
            }

            Spacer(Modifier.height(16.dp))

            // +10 / -10 second skip row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // -10 seconds
                OutlinedButton(
                    onClick = {
                        val newPos = (currentPosition - 10000).coerceAtLeast(0)
                        playerViewModel.seekTo(newPos)
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        brush = Brush.linearGradient(listOf(Color.White.copy(alpha = 0.3f), Color.White.copy(alpha = 0.3f)))
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Icon(
                        Icons.Rounded.Replay10,
                        contentDescription = "Rewind 10s",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("-10s", style = MaterialTheme.typography.labelMedium)
                }

                Spacer(Modifier.width(32.dp))

                // +10 seconds
                OutlinedButton(
                    onClick = {
                        val newPos = (currentPosition + 10000).coerceAtMost(duration)
                        playerViewModel.seekTo(newPos)
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        brush = Brush.linearGradient(listOf(Color.White.copy(alpha = 0.3f), Color.White.copy(alpha = 0.3f)))
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Icon(
                        Icons.Rounded.Forward10,
                        contentDescription = "Forward 10s",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("+10s", style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(Modifier.weight(1f))

            // Bottom utility row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { showSleepTimerDialog = true }) {
                    Icon(
                        Icons.Filled.Timer,
                        contentDescription = "Sleep Timer",
                        modifier = Modifier.size(22.dp),
                        tint = if (sleepTimerRemaining != null) Brand else Color.White.copy(alpha = 0.5f)
                    )
                }
                IconButton(onClick = { showPlaylistSheet = true }) {
                    Icon(
                        Icons.Rounded.PlaylistAdd,
                        contentDescription = "Add to Playlist",
                        modifier = Modifier.size(22.dp),
                        tint = Color.White.copy(alpha = 0.5f)
                    )
                }
                IconButton(onClick = { /* share */ }) {
                    Icon(
                        Icons.Rounded.Share,
                        contentDescription = "Share",
                        modifier = Modifier.size(22.dp),
                        tint = Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }

    // Add to playlist sheet
    if (showPlaylistSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPlaylistSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = Color(0xFF282828)
        ) {
            PlaylistSelectionSheet(
                libraryViewModel = libraryViewModel,
                onSelected = { playlist ->
                    playerViewModel.currentTrackId.value?.let { trackId ->
                        libraryViewModel.addTrackToPlaylist(playlist.id, trackId)
                    }
                    showPlaylistSheet = false
                }
            )
        }
    }

    // Sleep timer dialog
    if (showSleepTimerDialog) {
        AlertDialog(
            onDismissRequest = { showSleepTimerDialog = false },
            containerColor = Color(0xFF282828),
            titleContentColor = Color.White,
            textContentColor = Color.White,
            title = { Text("Sleep Timer") },
            text = {
                Column {
                    if (sleepTimerRemaining != null) {
                        Text(
                            "Timer active: ${formatMs(sleepTimerRemaining ?: 0)} remaining",
                            color = Brand,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(16.dp))
                        TextButton(onClick = {
                            playerViewModel.cancelSleepTimer()
                            showSleepTimerDialog = false
                        }) {
                            Text("Cancel Timer", color = Brand)
                        }
                    } else {
                        listOf(15, 30, 45, 60).forEach { minutes ->
                            TextButton(
                                onClick = {
                                    playerViewModel.startSleepTimer(minutes)
                                    showSleepTimerDialog = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("$minutes minutes", color = Color.White)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSleepTimerDialog = false }) {
                    Text("Close", color = Brand)
                }
            }
        )
    }
}

@Composable
private fun PlaylistSelectionSheet(
    libraryViewModel: LibraryViewModel,
    onSelected: (Playlist) -> Unit
) {
    val playlists by libraryViewModel.playlists.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            "Save to Playlist",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(Modifier.height(16.dp))
        if (playlists.isEmpty()) {
            Text(
                "No playlists available",
                color = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.padding(16.dp)
            )
        } else {
            LazyColumn {
                items(playlists) { playlist ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelected(playlist) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.PlaylistPlay,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            playlist.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}

private suspend fun loadBitmap(context: android.content.Context, url: String): Bitmap? {
    return try {
        val loader = ImageLoader(context)
        val request = ImageRequest.Builder(context).data(url).allowHardware(false).build()
        val result = loader.execute(request)
        (result as? SuccessResult)?.let { (it.drawable as? BitmapDrawable)?.bitmap }
    } catch (e: Exception) { null }
}

private fun formatMs(ms: Long): String {
    if (ms <= 0) return "0:00"
    val totalSec = ms / 1000
    return String.format(Locale.getDefault(), "%d:%02d", totalSec / 60, totalSec % 60)
}
