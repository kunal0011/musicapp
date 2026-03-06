package com.musicapp.android.network

import com.musicapp.android.models.Album
import com.musicapp.android.models.Artist
import com.musicapp.android.models.AuthResponse
import com.musicapp.android.models.LoginRequest
import com.musicapp.android.models.LyricsResponse
import com.musicapp.android.models.PagedResponse
import com.musicapp.android.models.Playlist
import com.musicapp.android.models.RegisterRequest
import com.musicapp.android.models.Track
import com.musicapp.android.models.UserProfile
import retrofit2.http.*

interface ApiService {

    // Auth
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    // Tracks
    @GET("api/v1/tracks")
    suspend fun getAllTracks(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50
    ): PagedResponse<Track>

    @GET("api/v1/tracks/cursor")
    suspend fun getTracksCursor(
        @Query("cursor") cursor: Long? = null,
        @Query("size") size: Int = 20
    ): List<Track>

    @GET("api/v1/tracks/search")
    suspend fun searchTracks(@Query("q") query: String): List<Track>

    @GET("api/v1/tracks/{id}")
    suspend fun getTrackById(@Path("id") id: Long): Track

    @GET("api/v1/tracks/{id}/stream")
    suspend fun getStreamUrl(@Path("id") id: Long): Map<String, String>

    @POST("api/v1/tracks/{id}/play")
    suspend fun recordPlay(@Path("id") id: Long)

    @GET("api/v1/tracks/recently-played")
    suspend fun getRecentlyPlayed(): List<Track>

    // Playlists
    @GET("api/v1/playlists")
    suspend fun getPlaylists(): List<Playlist>

    @POST("api/v1/playlists")
    suspend fun createPlaylist(@Body body: Map<String, String>): Playlist

    @POST("api/v1/playlists/{playlistId}/tracks/{trackId}")
    suspend fun addTrackToPlaylist(
        @Path("playlistId") playlistId: Long,
        @Path("trackId") trackId: Long
    ): Playlist

    @DELETE("api/v1/playlists/{playlistId}/tracks/{trackId}")
    suspend fun removeTrackFromPlaylist(
        @Path("playlistId") playlistId: Long,
        @Path("trackId") trackId: Long
    ): Playlist

    // Liked songs
    @GET("api/v1/me/liked")
    suspend fun getLikedTracks(): List<Track>

    @POST("api/v1/me/liked/{trackId}")
    suspend fun likeTrack(@Path("trackId") trackId: Long)

    @DELETE("api/v1/me/liked/{trackId}")
    suspend fun unlikeTrack(@Path("trackId") trackId: Long)

    // User profile
    @GET("api/v1/me/profile")
    suspend fun getProfile(): UserProfile

    @PUT("api/v1/me/profile")
    suspend fun updateProfile(@Body body: Map<String, String>): UserProfile

    // Recommendations
    @GET("api/v1/recommendations")
    suspend fun getRecommendations(@Query("limit") limit: Int = 20): List<Track>

    // Artists
    @GET("api/v1/artists")
    suspend fun getArtists(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): PagedResponse<Artist>

    @GET("api/v1/artists/{id}")
    suspend fun getArtistById(@Path("id") id: Long): Artist

    @GET("api/v1/artists/{id}/tracks")
    suspend fun getArtistTracks(@Path("id") id: Long): List<Track>

    @POST("api/v1/artists/{id}/follow")
    suspend fun followArtist(@Path("id") id: Long)

    @DELETE("api/v1/artists/{id}/follow")
    suspend fun unfollowArtist(@Path("id") id: Long)

    // Albums
    @GET("api/v1/albums")
    suspend fun getAlbums(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): PagedResponse<Album>

    @GET("api/v1/albums/{id}")
    suspend fun getAlbumById(@Path("id") id: Long): Album

    @GET("api/v1/albums/{id}/tracks")
    suspend fun getAlbumTracks(@Path("id") id: Long): List<Track>

    // Lyrics
    @GET("api/v1/tracks/{id}/lyrics")
    suspend fun getLyrics(@Path("id") id: Long): LyricsResponse
}
