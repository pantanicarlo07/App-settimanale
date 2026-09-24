package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "progress_snapshots")
data class ProgressSnapshotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val periodLabel: String,
    val completionPercentage: Int, // 0 to 100
    val totalEventsCount: Int,
    val completedEventsCount: Int,
    val missedEventsCount: Int,
    val pendingEventsCount: Int,
    val totalPlannedMinutes: Int,
    val totalDoneMinutes: Int,
    val totalNotDoneMinutes: Int,
    val efficiencyScore: Int = completionPercentage,
    val efficiencyNotes: String = "",
    val syntheticSummaryText: String = ""
) {
    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.ITALIAN)
            return sdf.format(Date(timestamp))
        }

    val formattedDoneDuration: String
        get() = RoutineItem.formatMinutes(totalDoneMinutes)

    val formattedPlannedDuration: String
        get() = RoutineItem.formatMinutes(totalPlannedMinutes)

    val formattedNotDoneDuration: String
        get() = RoutineItem.formatMinutes(totalNotDoneMinutes)
}
