package com.musicapp.android.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicapp.android.models.Playlist
import com.musicapp.android.models.Track
import com.musicapp.android.repository.TrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: TrackRepository
) : ViewModel() {

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    private val _likedTracks = MutableStateFlow<List<Track>>(emptyList())
    val likedTracks: StateFlow<List<Track>> = _likedTracks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init { loadAll() }

    fun loadAll() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val playlistsDeferred = async { repository.getPlaylists() }
            val likedDeferred = async { repository.getLikedTracks() }

            playlistsDeferred.await().onSuccess { _playlists.value = it }
                .onFailure { _errorMessage.value = it.message }

            likedDeferred.await().onSuccess { _likedTracks.value = it }

            _isLoading.value = false
        }
    }

    fun loadPlaylists() {
        viewModelScope.launch {
            repository.getPlaylists().onSuccess { _playlists.value = it }
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.createPlaylist(name)
                .onSuccess { loadPlaylists() }
                .onFailure { _errorMessage.value = it.message }
            _isLoading.value = false
        }
    }

    fun addTrackToPlaylist(playlistId: Long, trackId: Long) {
        viewModelScope.launch {
            repository.addTrackToPlaylist(playlistId, trackId)
                .onSuccess { loadPlaylists() }
                .onFailure { _errorMessage.value = it.message }
        }
    }
}
