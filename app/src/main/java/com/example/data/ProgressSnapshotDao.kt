package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressSnapshotDao {
    @Query("SELECT * FROM progress_snapshots ORDER BY timestamp DESC, id DESC")
    fun getAllSnapshots(): Flow<List<ProgressSnapshotEntity>>

    @Query("SELECT * FROM progress_snapshots ORDER BY timestamp ASC, id ASC")
    fun getAllSnapshotsChronological(): Flow<List<ProgressSnapshotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: ProgressSnapshotEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshots(snapshots: List<ProgressSnapshotEntity>)

    @Query("DELETE FROM progress_snapshots WHERE id = :id")
    suspend fun deleteSnapshot(id: Long)

    @Query("DELETE FROM progress_snapshots")
    suspend fun clearAllSnapshots()
}
