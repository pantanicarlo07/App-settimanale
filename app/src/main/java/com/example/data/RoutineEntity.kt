package com.example.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Locale

enum class RoutineType(val label: String) {
    EVENT("Evento a orario"),
    REMINDER("Promemoria / To-Do")
}

enum class ExecutionStatus(val label: String) {
    PENDING("Non assegnato"),
    COMPLETED("Fatto"),
    MISSED("Non fatto")
}

enum class DayOfWeek(val dayIndex: Int, val shortName: String, val fullName: String) {
    MONDAY(1, "Lun", "Lunedì"),
    TUESDAY(2, "Mar", "Martedì"),
    WEDNESDAY(3, "Mer", "Mercoledì"),
    THURSDAY(4, "Gio", "Giovedì"),
    FRIDAY(5, "Ven", "Venerdì"),
    SATURDAY(6, "Sab", "Sabato"),
    SUNDAY(7, "Dom", "Domenica");

    companion object {
        fun fromIndex(index: Int): DayOfWeek {
            return entries.firstOrNull { it.dayIndex == index } ?: MONDAY
        }
    }
}

enum class RoutineCategory(
    val label: String,
    val defaultColorHex: String
) {
    WORK("Lavoro", "#3B82F6"),
    STUDY("Studio", "#6366F1"),
    FITNESS("Sport & Fitness", "#10B981"),
    WELLNESS("Salute & Relax", "#06B6D4"),
    HOME("Casa & Famiglia", "#F59E0B"),
    HOBBY("Hobby & Svago", "#8B5CF6"),
    PERSONAL("Personale", "#EC4899");

    fun getIcon(): ImageVector {
        return when (this) {
            WORK -> Icons.Default.Laptop
            STUDY -> Icons.Default.Book
            FITNESS -> Icons.Default.FitnessCenter
            WELLNESS -> Icons.Default.SelfImprovement
            HOME -> Icons.Default.Home
            HOBBY -> Icons.Default.Palette
            PERSONAL -> Icons.Default.Star
        }
    }
}

enum class RoutinePriority(val label: String, val colorHex: String) {
    LOW("Bassa", "#10B981"),
    MEDIUM("Media", "#F59E0B"),
    HIGH("Alta", "#EF4444")
}

@Entity(tableName = "routine_items")
data class RoutineItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val notes: String = "",
    val type: RoutineType,
    val dayOfWeek: Int, // 1 to 7 (Monday to Sunday)
    val startHour: Int? = null,
    val startMinute: Int? = null,
    val endHour: Int? = null,
    val endMinute: Int? = null,
    val isCompleted: Boolean = false,
    val priority: RoutinePriority = RoutinePriority.MEDIUM,
    val category: RoutineCategory = RoutineCategory.PERSONAL,
    val colorHex: String = "#4F46E5",
    val reminderEnabled: Boolean = false,
    val executionStatus: ExecutionStatus = if (isCompleted) ExecutionStatus.COMPLETED else ExecutionStatus.PENDING,
    val actualMinutesSpent: Int? = null // Minuti effettivi svolti (se diverso dalla durata pianificata)
) {
    val dayEnum: DayOfWeek
        get() = DayOfWeek.fromIndex(dayOfWeek)

    val formattedTime: String
        get() {
            if (startHour == null || startMinute == null) return ""
            val start = String.format(Locale.getDefault(), "%02d:%02d", startHour, startMinute)
            return if (endHour != null && endMinute != null) {
                val end = String.format(Locale.getDefault(), "%02d:%02d", endHour, endMinute)
                "$start - $end"
            } else {
                start
            }
        }

    val sortMinutes: Int
        get() {
            return (startHour ?: 99) * 60 + (startMinute ?: 0)
        }

    /**
     * Durata pianificata in minuti (calcolata dall'orario di inizio e fine, oppure default standard).
     */
    val plannedMinutes: Int
        get() {
            if (startHour != null && startMinute != null && endHour != null && endMinute != null) {
                val diff = (endHour * 60 + endMinute) - (startHour * 60 + startMinute)
                return if (diff > 0) diff else 60
            }
            return if (type == RoutineType.EVENT) 60 else 15
        }

    /**
     * Minuti effettivamente svolti (tempo FATTO).
     */
    val doneMinutes: Int
        get() {
            return when (executionStatus) {
                ExecutionStatus.COMPLETED -> actualMinutesSpent ?: plannedMinutes
                ExecutionStatus.MISSED -> actualMinutesSpent ?: 0
                ExecutionStatus.PENDING -> 0
            }
        }

    /**
     * Minuti NON svolti / persi / mancanti (tempo NON FATTO).
     */
    val notDoneMinutes: Int
        get() {
            return when (executionStatus) {
                ExecutionStatus.MISSED -> (plannedMinutes - (actualMinutesSpent ?: 0)).coerceAtLeast(0)
                ExecutionStatus.COMPLETED -> (plannedMinutes - doneMinutes).coerceAtLeast(0)
                ExecutionStatus.PENDING -> 0 // In attesa di assegnazione
            }
        }

    val formattedPlannedDuration: String
        get() = formatMinutes(plannedMinutes)

    val formattedDoneDuration: String
        get() = formatMinutes(doneMinutes)

    val formattedNotDoneDuration: String
        get() = formatMinutes(notDoneMinutes)

    companion object {
        fun formatMinutes(minutes: Int): String {
            val h = minutes / 60
            val m = minutes % 60
            return when {
                h > 0 && m > 0 -> "${h}h ${m}m"
                h > 0 -> "${h}h"
                else -> "${m}m"
            }
        }
    }
}
