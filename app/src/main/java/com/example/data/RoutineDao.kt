package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {
    @Query("SELECT * FROM routine_items ORDER BY dayOfWeek ASC, (startHour * 60 + startMinute) ASC, id ASC")
    fun getAllItems(): Flow<List<RoutineItem>>

    @Query("SELECT * FROM routine_items WHERE dayOfWeek = :dayOfWeek ORDER BY (startHour * 60 + startMinute) ASC, id ASC")
    fun getItemsForDay(dayOfWeek: Int): Flow<List<RoutineItem>>

    @Query("SELECT * FROM routine_items WHERE id = :id")
    suspend fun getItemById(id: Long): RoutineItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: RoutineItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<RoutineItem>)

    @Update
    suspend fun updateItem(item: RoutineItem)

    @Delete
    suspend fun deleteItem(item: RoutineItem)

    @Query("DELETE FROM routine_items WHERE id = :id")
    suspend fun deleteItemById(id: Long)

    @Query("UPDATE routine_items SET isCompleted = :completed, executionStatus = CASE WHEN :completed = 1 THEN 'COMPLETED' ELSE 'PENDING' END WHERE id = :id")
    suspend fun updateCompletion(id: Long, completed: Boolean)

    @Query("UPDATE routine_items SET executionStatus = :status, actualMinutesSpent = :actualMinutes, isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateExecution(id: Long, status: ExecutionStatus, actualMinutes: Int?, isCompleted: Boolean)

    @Query("UPDATE routine_items SET isCompleted = 0, executionStatus = 'PENDING', actualMinutesSpent = NULL")
    suspend fun resetWeeklyReminders()

    @Query("DELETE FROM routine_items")
    suspend fun clearAll()
}
