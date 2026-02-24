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
import com.musicapp.android.player.PlaybackService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private var mediaControllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentTrackTitle = MutableStateFlow("No track selected")
    val currentTrackTitle: StateFlow<String> = _currentTrackTitle.asStateFlow()

    private val _currentArtist = MutableStateFlow("Unknown Artist")
    val currentArtist: StateFlow<String> = _currentArtist.asStateFlow()

    private val _currentCoverUrl = MutableStateFlow("")
    val currentCoverUrl: StateFlow<String> = _currentCoverUrl.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _currentTrackId = MutableStateFlow<Long?>(null)
    val currentTrackId: StateFlow<Long?> = _currentTrackId.asStateFlow()

    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()

    private val _showPlayerSheet = MutableStateFlow(false)
    val showPlayerSheet: StateFlow<Boolean> = _showPlayerSheet.asStateFlow()

    init {
        initializeController()
    }

    fun requestExpand() {
        _showPlayerSheet.value = true
    }

    fun dismissPlayerSheet() {
        _showPlayerSheet.value = false
    }
    
    private fun initializeController() {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, PlaybackService::class.java)
        )
        mediaControllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

        viewModelScope.launch {
            mediaController = mediaControllerFuture?.await()
            mediaController?.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _isPlaying.value = isPlaying
                    if (isPlaying) {
                        startProgressUpdate()
                    }
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        _duration.value = mediaController?.duration ?: 0L
                    }
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    _currentTrackId.value = mediaItem?.mediaId?.toLongOrNull()
                    _duration.value = mediaController?.duration ?: 0L
                    _currentPosition.value = 0L
                    
                    // Fallback in case MediaMetadata changed doesn't fire immediately
                    mediaItem?.mediaMetadata?.let { metadata ->
                        metadata.title?.toString()?.let { if (it.isNotBlank()) _currentTrackTitle.value = it }
                        metadata.artist?.toString()?.let { if (it.isNotBlank()) _currentArtist.value = it }
                        metadata.artworkUri?.toString()?.let { if (it.isNotBlank()) _currentCoverUrl.value = it }
                    }
                }

                override fun onMediaMetadataChanged(mediaMetadata: androidx.media3.common.MediaMetadata) {
                    super.onMediaMetadataChanged(mediaMetadata)
                    mediaMetadata.title?.toString()?.let { if (it.isNotBlank()) _currentTrackTitle.value = it }
                    mediaMetadata.artist?.toString()?.let { if (it.isNotBlank()) _currentArtist.value = it }
                    mediaMetadata.artworkUri?.toString()?.let { if (it.isNotBlank()) _currentCoverUrl.value = it }
                }

                override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                    _isShuffleEnabled.value = shuffleModeEnabled
                }
            })

            // Sync initial state with the active MediaController (fixes metadata disappearing on minimize)
            mediaController?.let { controller ->
                _isPlaying.value = controller.isPlaying
                if (controller.isPlaying) {
                    startProgressUpdate()
                }
                _duration.value = controller.duration.coerceAtLeast(0L)
                _currentPosition.value = controller.currentPosition.coerceAtLeast(0L)
                _isShuffleEnabled.value = controller.shuffleModeEnabled

                controller.currentMediaItem?.let { item ->
                    _currentTrackId.value = item.mediaId.toLongOrNull()
                    item.mediaMetadata.let { metadata ->
                        metadata.title?.toString()?.let { if (it.isNotBlank()) _currentTrackTitle.value = it }
                        metadata.artist?.toString()?.let { if (it.isNotBlank()) _currentArtist.value = it }
                        metadata.artworkUri?.toString()?.let { if (it.isNotBlank()) _currentCoverUrl.value = it }
                    }
                }
                
                controller.mediaMetadata.let { metadata ->
                    metadata.title?.toString()?.let { if (it.isNotBlank()) _currentTrackTitle.value = it }
                    metadata.artist?.toString()?.let { if (it.isNotBlank()) _currentArtist.value = it }
                    metadata.artworkUri?.toString()?.let { if (it.isNotBlank()) _currentCoverUrl.value = it }
                }
            }
        }
    }

    private fun startProgressUpdate() {
        viewModelScope.launch {
            while (_isPlaying.value) {
                mediaController?.let {
                    _currentPosition.value = it.currentPosition
                }
                kotlinx.coroutines.delay(1000L)
            }
        }
    }

    fun playTracks(tracks: List<com.musicapp.android.models.Track>, startIndex: Int) {
        val selectedTrack = tracks.getOrNull(startIndex)
        selectedTrack?.let {
            _currentTrackTitle.value = it.title
            _currentArtist.value = it.artist
            _currentCoverUrl.value = it.coverArtUrl ?: ""
            _currentTrackId.value = it.id
        }
        
        val mediaItems = tracks.map { track ->
            val metadataBuilder = androidx.media3.common.MediaMetadata.Builder()
                .setTitle(track.title)
                .setArtist(track.artist)
            
            track.coverArtUrl?.let {
                metadataBuilder.setArtworkUri(android.net.Uri.parse(it))
            }

            MediaItem.Builder()
                .setMediaId(track.id.toString())
                .setUri(track.streamUrl)
                .setMediaMetadata(metadataBuilder.build())
                .build()
        }
            
        mediaController?.let { controller ->
            controller.setMediaItems(mediaItems, startIndex, 0L)
            controller.prepare()
            controller.play()
        }
    }

    fun togglePlayPause() {
        if (mediaController?.isPlaying == true) {
            mediaController?.pause()
        } else {
            mediaController?.play()
        }
    }

    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
        _currentPosition.value = position
    }

    fun skipToNext() {
        mediaController?.seekToNext()
    }

    fun skipToPrevious() {
        mediaController?.seekToPrevious()
    }

    fun seekForward() {
        mediaController?.let {
            val newPosition = (it.currentPosition + 10000).coerceAtMost(it.duration)
            it.seekTo(newPosition)
            _currentPosition.value = newPosition
        }
    }

    fun seekBackward() {
        mediaController?.let {
            val newPosition = (it.currentPosition - 10000).coerceAtLeast(0)
            it.seekTo(newPosition)
            _currentPosition.value = newPosition
        }
    }

    fun seekToStart() {
        mediaController?.seekTo(0)
    }

    fun seekToEnd() {
        mediaController?.let {
            it.seekTo(it.duration)
        }
    }

    fun toggleShuffle() {
        mediaController?.let {
            it.shuffleModeEnabled = !it.shuffleModeEnabled
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaControllerFuture?.let { MediaController.releaseFuture(it) }
    }
}
