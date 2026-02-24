package com.musicapp.android.repository

import com.musicapp.android.models.Playlist
import com.musicapp.android.models.Track
import com.musicapp.android.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getAllTracks(): Result<List<Track>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAllTracks()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchTracks(query: String): Result<List<Track>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.searchTracks(query)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPlaylists(): Result<List<Playlist>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getPlaylists()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createPlaylist(name: String): Result<Playlist> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createPlaylist(Playlist(0, name, "", emptyList()))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addTrackToPlaylist(playlistId: Long, trackId: Long): Result<Playlist> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.addTrackToPlaylist(playlistId, trackId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
