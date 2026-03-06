package com.musicapp.android.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicapp.android.models.Track
import com.musicapp.android.repository.TrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: TrackRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _query.asStateFlow()

    private val _results = MutableStateFlow<List<Track>>(emptyList())
    val searchResults: StateFlow<List<Track>> = _results.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _error.asStateFlow()

    init {
        loadAll()

        _query
            .debounce(400L)
            .distinctUntilChanged()
            .onEach { q -> if (q.isBlank()) loadAll() else search(q) }
            .launchIn(viewModelScope)
    }

    private fun loadAll() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getAllTracks()
                .onSuccess { _results.value = it }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun onSearchQueryChanged(query: String) { _query.value = query }

    private fun search(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.searchTracks(query)
                .onSuccess { _results.value = it }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }
}
