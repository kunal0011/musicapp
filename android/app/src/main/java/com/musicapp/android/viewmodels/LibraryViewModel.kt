package com.musicapp.android.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicapp.android.models.Playlist
import com.musicapp.android.repository.TrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadPlaylists()
    }

    fun loadPlaylists() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            repository.getPlaylists()
                .onSuccess { playlists ->
                    _playlists.value = playlists
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to load playlists"
                }

            _isLoading.value = false
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.createPlaylist(name)
                .onSuccess {
                    loadPlaylists()
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to create playlist"
                }
            _isLoading.value = false
        }
    }

    fun addTrackToPlaylist(playlistId: Long, trackId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.addTrackToPlaylist(playlistId, trackId)
                .onSuccess {
                    loadPlaylists() // Refresh playlists after adding track
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to add track to playlist"
                }
            _isLoading.value = false
        }
    }
}
