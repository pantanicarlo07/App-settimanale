package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class Converters {
    @TypeConverter
    fun fromRoutineType(value: RoutineType): String = value.name

    @TypeConverter
    fun toRoutineType(value: String): RoutineType = try {
        RoutineType.valueOf(value)
    } catch (e: Exception) {
        RoutineType.EVENT
    }

    @TypeConverter
    fun fromRoutineCategory(value: RoutineCategory): String = value.name

    @TypeConverter
    fun toRoutineCategory(value: String): RoutineCategory = try {
        RoutineCategory.valueOf(value)
    } catch (e: Exception) {
        RoutineCategory.PERSONAL
    }

    @TypeConverter
    fun fromRoutinePriority(value: RoutinePriority): String = value.name

    @TypeConverter
    fun toRoutinePriority(value: String): RoutinePriority = try {
        RoutinePriority.valueOf(value)
    } catch (e: Exception) {
        RoutinePriority.MEDIUM
    }

    @TypeConverter
    fun fromExecutionStatus(value: ExecutionStatus): String = value.name

    @TypeConverter
    fun toExecutionStatus(value: String): ExecutionStatus = try {
        ExecutionStatus.valueOf(value)
    } catch (e: Exception) {
        ExecutionStatus.PENDING
    }
}

@Database(entities = [RoutineItem::class, ProgressSnapshotEntity::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun routineDao(): RoutineDao
    abstract fun progressSnapshotDao(): ProgressSnapshotDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "week_routine_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
