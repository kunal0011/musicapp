package com.musicapp.android.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {

    @Query("SELECT * FROM tracks ORDER BY title ASC")
    fun observeAll(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks ORDER BY title ASC")
    suspend fun getAll(): List<TrackEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tracks: List<TrackEntity>)

    @Query("DELETE FROM tracks")
    suspend fun clearAll()

    // --- Offline playback support ---

    @Query("SELECT * FROM tracks WHERE isDownloaded = 1 ORDER BY title ASC")
    fun observeDownloaded(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE isDownloaded = 1 ORDER BY title ASC")
    suspend fun getDownloadedTracks(): List<TrackEntity>

    @Query("UPDATE tracks SET localFilePath = :filePath, isDownloaded = 1 WHERE id = :trackId")
    suspend fun markDownloaded(trackId: Long, filePath: String)

    @Query("UPDATE tracks SET localFilePath = NULL, isDownloaded = 0 WHERE id = :trackId")
    suspend fun removeDownload(trackId: Long)

    @Query("SELECT * FROM tracks WHERE id = :trackId")
    suspend fun getTrackById(trackId: Long): TrackEntity?
}
