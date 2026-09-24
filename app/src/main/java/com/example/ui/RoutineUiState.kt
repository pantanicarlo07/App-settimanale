package com.example.ui

import com.example.data.DayOfWeek
import com.example.data.ProgressSnapshotEntity
import com.example.data.RoutineCategory
import com.example.data.RoutineItem
import com.example.data.RoutineType
import com.example.gemini.ChatMessage

enum class ViewMode(val title: String) {
    DAY_TIMELINE("Giorno"),
    WEEK_GRID("Settimana"),
    FOCUS_COACH("Focus Coach"),
    STATS("Riepilogo")
}

data class RoutineUiState(
    val selectedDay: DayOfWeek = DayOfWeek.MONDAY,
    val viewMode: ViewMode = ViewMode.DAY_TIMELINE,
    val filterCategory: RoutineCategory? = null,
    val filterType: RoutineType? = null,
    val allItems: List<RoutineItem> = emptyList(),
    val snapshots: List<ProgressSnapshotEntity> = emptyList(),
    val isLoading: Boolean = true,
    val isBottomSheetOpen: Boolean = false,
    val isExportSheetOpen: Boolean = false,
    val editingItem: RoutineItem? = null,
    val messageSnackbar: String? = null,
    val chatMessages: List<ChatMessage> = emptyList(),
    val isChatLoading: Boolean = false,
    val chatInputText: String = ""
) {
    val dayItems: List<RoutineItem>
        get() = allItems
            .filter { it.dayOfWeek == selectedDay.dayIndex }
            .filter { filterCategory == null || it.category == filterCategory }
            .filter { filterType == null || it.type == filterType }

    val dayEvents: List<RoutineItem>
        get() = dayItems
            .filter { it.type == RoutineType.EVENT }
            .sortedBy { it.sortMinutes }

    val dayReminders: List<RoutineItem>
        get() = dayItems
            .filter { it.type == RoutineType.REMINDER }
            .sortedWith(
                compareBy<RoutineItem> { it.isCompleted }
                    .thenByDescending { it.priority.ordinal }
                    .thenBy { it.sortMinutes }
            )

    val totalEventsCount: Int
        get() = allItems.count { it.type == RoutineType.EVENT }

    val totalRemindersCount: Int
        get() = allItems.count { it.type == RoutineType.REMINDER }

    val completedRemindersCount: Int
        get() = allItems.count { it.type == RoutineType.REMINDER && it.isCompleted }

    val weeklyCompletionRate: Float
        get() = if (totalRemindersCount == 0) 0f else (completedRemindersCount.toFloat() / totalRemindersCount.toFloat())

    fun getItemsForDay(day: DayOfWeek): List<RoutineItem> {
        return allItems.filter { it.dayOfWeek == day.dayIndex }
    }

    fun getEventsCountForDay(day: DayOfWeek): Int {
        return allItems.count { it.dayOfWeek == day.dayIndex && it.type == RoutineType.EVENT }
    }

    fun getRemindersCountForDay(day: DayOfWeek): Int {
        return allItems.count { it.dayOfWeek == day.dayIndex && it.type == RoutineType.REMINDER }
    }
}
