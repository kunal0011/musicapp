package com.musicapp.android.viewmodels

import android.content.ComponentName
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.musicapp.android.models.Track
import com.musicapp.android.player.PlaybackService
import com.musicapp.android.repository.TrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class RepeatMode { OFF, ALL, ONE }

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: TrackRepository
) : ViewModel() {

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentTrackTitle = MutableStateFlow("No track selected")
    val currentTrackTitle: StateFlow<String> = _currentTrackTitle.asStateFlow()

    private val _currentArtist = MutableStateFlow("")
    val currentArtist: StateFlow<String> = _currentArtist.asStateFlow()

    private val _currentCoverUrl = MutableStateFlow("")
    val currentCoverUrl: StateFlow<String> = _currentCoverUrl.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    val currentTrackId = MutableStateFlow<Long?>(null)

    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _showPlayerSheet = MutableStateFlow(false)
    val showPlayerSheet: StateFlow<Boolean> = _showPlayerSheet.asStateFlow()

    private val _isCurrentTrackLiked = MutableStateFlow(false)
    val isCurrentTrackLiked: StateFlow<Boolean> = _isCurrentTrackLiked.asStateFlow()

    // Current queue for display in QueueScreen
    private val _queue = MutableStateFlow<List<Track>>(emptyList())
    val queue: StateFlow<List<Track>> = _queue.asStateFlow()

    // Sleep timer
    private val _sleepTimerRemainingMs = MutableStateFlow<Long?>(null)
    val sleepTimerRemainingMs: StateFlow<Long?> = _sleepTimerRemainingMs.asStateFlow()
    private var sleepTimerJob: Job? = null

    // Crossfade duration
    private val _crossfadeDurationMs = MutableStateFlow(3000L)
    val crossfadeDurationMs: StateFlow<Long> = _crossfadeDurationMs.asStateFlow()

    init { initController() }

    fun requestExpand() { _showPlayerSheet.value = true }
    fun dismissPlayerSheet() { _showPlayerSheet.value = false }

    private fun initController() {
        val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(context, token).buildAsync()

        viewModelScope.launch {
            mediaController = controllerFuture?.await()
            mediaController?.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _isPlaying.value = isPlaying
                    if (isPlaying) startProgressUpdate()
                }

                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_READY) {
                        _duration.value = mediaController?.duration?.coerceAtLeast(0L) ?: 0L
                    }
                }

                override fun onMediaItemTransition(item: MediaItem?, reason: Int) {
                    val trackId = item?.mediaId?.toLongOrNull()
                    currentTrackId.value = trackId
                    _duration.value = 0L
                    _currentPosition.value = 0L
                    item?.mediaMetadata?.let { meta ->
                        meta.title?.toString()?.takeIf { it.isNotBlank() }?.let { _currentTrackTitle.value = it }
                        meta.artist?.toString()?.takeIf { it.isNotBlank() }?.let { _currentArtist.value = it }
                        meta.artworkUri?.toString()?.takeIf { it.isNotBlank() }?.let { _currentCoverUrl.value = it }
                    }
                    // Update liked state for new track
                    if (trackId != null) loadLikedState(trackId)
                    // Record play
                    if (trackId != null) viewModelScope.launch { repository.recordPlay(trackId) }
                }

                override fun onMediaMetadataChanged(meta: androidx.media3.common.MediaMetadata) {
                    meta.title?.toString()?.takeIf { it.isNotBlank() }?.let { _currentTrackTitle.value = it }
                    meta.artist?.toString()?.takeIf { it.isNotBlank() }?.let { _currentArtist.value = it }
                    meta.artworkUri?.toString()?.takeIf { it.isNotBlank() }?.let { _currentCoverUrl.value = it }
                }

                override fun onShuffleModeEnabledChanged(enabled: Boolean) {
                    _isShuffleEnabled.value = enabled
                }
            })

            // Sync initial state after reconnect
            mediaController?.let { c ->
                _isPlaying.value = c.isPlaying
                if (c.isPlaying) startProgressUpdate()
                _duration.value = c.duration.coerceAtLeast(0L)
                _currentPosition.value = c.currentPosition.coerceAtLeast(0L)
                _isShuffleEnabled.value = c.shuffleModeEnabled
                c.currentMediaItem?.let { item ->
                    currentTrackId.value = item.mediaId.toLongOrNull()
                    item.mediaMetadata.let { meta ->
                        meta.title?.toString()?.takeIf { it.isNotBlank() }?.let { _currentTrackTitle.value = it }
                        meta.artist?.toString()?.takeIf { it.isNotBlank() }?.let { _currentArtist.value = it }
                        meta.artworkUri?.toString()?.takeIf { it.isNotBlank() }?.let { _currentCoverUrl.value = it }
                    }
                }
            }
        }
    }

    private fun loadLikedState(trackId: Long) {
        viewModelScope.launch {
            repository.getLikedTracks().onSuccess { liked ->
                _isCurrentTrackLiked.value = liked.any { it.id == trackId }
            }
        }
    }

    fun toggleLike() {
        val trackId = currentTrackId.value ?: return
        viewModelScope.launch {
            if (_isCurrentTrackLiked.value) {
                repository.unlikeTrack(trackId).onSuccess { _isCurrentTrackLiked.value = false }
            } else {
                repository.likeTrack(trackId).onSuccess { _isCurrentTrackLiked.value = true }
            }
        }
    }

    private fun startProgressUpdate() {
        viewModelScope.launch {
            while (_isPlaying.value) {
                _currentPosition.value = mediaController?.currentPosition ?: 0L
                delay(500L)
            }
        }
    }

    fun playTracks(tracks: List<Track>, startIndex: Int) {
        _queue.value = tracks
        tracks.getOrNull(startIndex)?.let {
            _currentTrackTitle.value = it.title
            _currentArtist.value = it.artist
            _currentCoverUrl.value = it.coverArtUrl ?: ""
            currentTrackId.value = it.id
            _isCurrentTrackLiked.value = it.liked
        }

        val items = tracks.map { track ->
            val streamUri = if (track.isDownloaded && track.localFilePath != null) {
                track.localFilePath
            } else {
                track.streamUrl
            }
            val meta = androidx.media3.common.MediaMetadata.Builder()
                .setTitle(track.title)
                .setArtist(track.artist)
                .apply { track.coverArtUrl?.let { setArtworkUri(android.net.Uri.parse(it)) } }
                .build()
            MediaItem.Builder()
                .setMediaId(track.id.toString())
                .setUri(streamUri)
                .setMediaMetadata(meta)
                .build()
        }

        mediaController?.setMediaItems(items, startIndex, 0L)
        mediaController?.prepare()
        mediaController?.play()
    }

    fun togglePlayPause() {
        if (mediaController?.isPlaying == true) mediaController?.pause()
        else mediaController?.play()
    }

    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
        _currentPosition.value = position
    }

    fun skipToNext() = mediaController?.seekToNext()
    fun skipToPrevious() = mediaController?.seekToPrevious()

    fun toggleShuffle() {
        mediaController?.let { it.shuffleModeEnabled = !it.shuffleModeEnabled }
    }

    fun toggleRepeat() {
        val next = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        _repeatMode.value = next
        mediaController?.repeatMode = when (next) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
        }
    }

    // --- Sleep Timer ---

    fun startSleepTimer(minutes: Int) {
        cancelSleepTimer()
        val totalMs = minutes * 60 * 1000L
        _sleepTimerRemainingMs.value = totalMs

        sleepTimerJob = viewModelScope.launch {
            var remaining = totalMs
            while (remaining > 0) {
                delay(1000L)
                remaining -= 1000L
                _sleepTimerRemainingMs.value = remaining
            }
            // Fade out volume
            mediaController?.let { controller ->
                for (i in 10 downTo 0) {
                    controller.volume = i / 10f
                    delay(200L)
                }
                controller.pause()
                controller.volume = 1f // Reset for next play
            }
            _sleepTimerRemainingMs.value = null
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        _sleepTimerRemainingMs.value = null
    }

    // --- Crossfade ---

    fun setCrossfadeDuration(ms: Long) {
        _crossfadeDurationMs.value = ms
        PlaybackService.crossfadeDurationMs = ms
    }

    // --- Queue reordering ---

    fun reorderQueue(from: Int, to: Int) {
        val mutable = _queue.value.toMutableList()
        val item = mutable.removeAt(from)
        mutable.add(to, item)
        _queue.value = mutable

        // Update ExoPlayer queue
        mediaController?.moveMediaItem(from, to)
    }

    override fun onCleared() {
        super.onCleared()
        sleepTimerJob?.cancel()
        controllerFuture?.let { MediaController.releaseFuture(it) }
    }
}
