package com.musicapp.android.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicapp.android.models.Track
import com.musicapp.android.repository.TrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val trackRepository: TrackRepository
) : ViewModel() {

    private val _tracks = MutableStateFlow<List<Track>>(emptyList())
    val tracks: StateFlow<List<Track>> = _tracks.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        fetchTracks()
    }

    private fun fetchTracks() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            val result = trackRepository.getAllTracks()
            result.onSuccess { trackList ->
                _tracks.value = trackList
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Failed to fetch tracks"
            }
            
            _isLoading.value = false
        }
    }
    
    fun retry() {
        fetchTracks()
    }
}
