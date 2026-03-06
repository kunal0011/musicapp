package com.musicapp.android.repository

import com.musicapp.android.data.local.TrackDao
import com.musicapp.android.data.local.toEntity
import com.musicapp.android.data.local.toTrack
import com.musicapp.android.models.Playlist
import com.musicapp.android.models.Track
import com.musicapp.android.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackRepository @Inject constructor(
    private val apiService: ApiService,
    private val trackDao: TrackDao
) {
    suspend fun getAllTracks(): Result<List<Track>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAllTracks()
            val tracks = response.content
            trackDao.clearAll()
            trackDao.insertAll(tracks.map { it.toEntity() })
            Result.success(tracks)
        } catch (e: Exception) {
            val cached = trackDao.getAll().map { it.toTrack() }
            if (cached.isNotEmpty()) Result.success(cached)
            else Result.failure(e)
        }
    }

    suspend fun searchTracks(query: String): Result<List<Track>> = withContext(Dispatchers.IO) {
        try {
            Result.success(apiService.searchTracks(query))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRecentlyPlayed(): Result<List<Track>> = withContext(Dispatchers.IO) {
        try {
            Result.success(apiService.getRecentlyPlayed())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun recordPlay(trackId: Long) = withContext(Dispatchers.IO) {
        runCatching { apiService.recordPlay(trackId) }
    }

    suspend fun getPlaylists(): Result<List<Playlist>> = withContext(Dispatchers.IO) {
        try {
            Result.success(apiService.getPlaylists())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createPlaylist(name: String): Result<Playlist> = withContext(Dispatchers.IO) {
        try {
            Result.success(apiService.createPlaylist(mapOf("name" to name)))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addTrackToPlaylist(playlistId: Long, trackId: Long): Result<Playlist> = withContext(Dispatchers.IO) {
        try {
            Result.success(apiService.addTrackToPlaylist(playlistId, trackId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLikedTracks(): Result<List<Track>> = withContext(Dispatchers.IO) {
        try {
            Result.success(apiService.getLikedTracks())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun likeTrack(trackId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            apiService.likeTrack(trackId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unlikeTrack(trackId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            apiService.unlikeTrack(trackId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
