package com.musicapp.android.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicapp.android.models.Track
import com.musicapp.android.repository.TrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val trackRepository: TrackRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Track>>(emptyList())
    val searchResults: StateFlow<List<Track>> = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        fetchAllTracks()

        _searchQuery
            .debounce(500L) // Wait for user to stop typing
            .distinctUntilChanged()
            .onEach { query ->
                if (query.isBlank()) {
                    fetchAllTracks()
                } else {
                    performSearch(query)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun fetchAllTracks() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = trackRepository.getAllTracks()
            result.onSuccess { tracks ->
                _searchResults.value = tracks
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Failed to load tracks"
            }

            _isLoading.value = false
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = trackRepository.searchTracks(query)
            result.onSuccess { tracks ->
                _searchResults.value = tracks
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Failed to search tracks"
            }

            _isLoading.value = false
        }
    }
}
