package com.musicapp.android.network

import com.musicapp.android.models.Playlist
import com.musicapp.android.models.Track
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("api/tracks")
    suspend fun getAllTracks(): List<Track>

    @GET("api/tracks/search")
    suspend fun searchTracks(@Query("query") query: String): List<Track>

    @GET("api/playlists")
    suspend fun getPlaylists(): List<Playlist>

    @POST("api/playlists")
    suspend fun createPlaylist(@retrofit2.http.Body playlist: Playlist): Playlist

    @POST("api/playlists/{playlistId}/tracks/{trackId}")
    suspend fun addTrackToPlaylist(
        @Path("playlistId") playlistId: Long,
        @Path("trackId") trackId: Long
    ): Playlist
}
