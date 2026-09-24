package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class RoutineRepository(
    private val dao: RoutineDao,
    private val snapshotDao: ProgressSnapshotDao? = null
) {
    val allItems: Flow<List<RoutineItem>> = dao.getAllItems()
    val allSnapshots: Flow<List<ProgressSnapshotEntity>> = snapshotDao?.getAllSnapshots() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    fun getItemsForDay(dayOfWeek: Int): Flow<List<RoutineItem>> = dao.getItemsForDay(dayOfWeek)

    suspend fun insertItem(item: RoutineItem): Long = dao.insertItem(item)

    suspend fun insertItemsForDays(
        days: Set<Int>,
        baseItem: RoutineItem
    ) {
        val items = days.map { day ->
            baseItem.copy(id = 0, dayOfWeek = day)
        }
        dao.insertItems(items)
    }

    suspend fun updateItem(item: RoutineItem) = dao.updateItem(item)

    suspend fun deleteItem(item: RoutineItem) = dao.deleteItem(item)

    suspend fun deleteById(id: Long) = dao.deleteItemById(id)

    suspend fun setReminderCompleted(id: Long, completed: Boolean) {
        dao.updateCompletion(id, completed)
    }

    suspend fun setItemExecution(id: Long, status: ExecutionStatus, actualMinutes: Int?) {
        val isCompleted = status == ExecutionStatus.COMPLETED
        dao.updateExecution(id, status, actualMinutes, isCompleted)
    }

    suspend fun resetAllWeeklyReminders() {
        dao.resetWeeklyReminders()
    }

    suspend fun clearAll() {
        dao.clearAll()
    }

    suspend fun saveSnapshot(snapshot: ProgressSnapshotEntity): Long {
        return snapshotDao?.insertSnapshot(snapshot) ?: -1L
    }

    suspend fun deleteSnapshot(id: Long) {
        snapshotDao?.deleteSnapshot(id)
    }

    suspend fun clearAllSnapshots() {
        snapshotDao?.clearAllSnapshots()
    }

    suspend fun loadStarterRoutineIfEmpty() {
        val current = dao.getAllItems().first()
        if (current.isEmpty()) {
            dao.insertItems(SampleRoutineData.getStarterWeeklyRoutine())
        }

        snapshotDao?.let { sDao ->
            val existingSnapshots = sDao.getAllSnapshots().first()
            if (existingSnapshots.isEmpty()) {
                val now = System.currentTimeMillis()
                val oneWeekMs = 7L * 24 * 60 * 60 * 1000
                val twoWeeksMs = 14L * 24 * 60 * 60 * 1000

                val sampleSnapshots = listOf(
                    ProgressSnapshotEntity(
                        timestamp = now - twoWeeksMs,
                        periodLabel = "Settimana 37 (8 - 14 Set)",
                        completionPercentage = 68,
                        totalEventsCount = 14,
                        completedEventsCount = 9,
                        missedEventsCount = 3,
                        pendingEventsCount = 2,
                        totalPlannedMinutes = 1260, // 21h
                        totalDoneMinutes = 855,     // 14h 15m
                        totalNotDoneMinutes = 405,  // 6h 45m
                        efficiencyScore = 68,
                        efficiencyNotes = "Buona ripartenza di settembre. Focus sul consolidamento delle abitudini mattutine.",
                        syntheticSummaryText = "Report Settimana 37:\n• 9/14 eventi svolti (68%)\n• 14h 15m svolte su 21h\n• Focus positivo su Lavoro e Benessere"
                    ),
                    ProgressSnapshotEntity(
                        timestamp = now - oneWeekMs,
                        periodLabel = "Settimana 38 (15 - 21 Set)",
                        completionPercentage = 79,
                        totalEventsCount = 15,
                        completedEventsCount = 12,
                        missedEventsCount = 2,
                        pendingEventsCount = 1,
                        totalPlannedMinutes = 1380, // 23h
                        totalDoneMinutes = 1090,    // 18h 10m
                        totalNotDoneMinutes = 290,  // 4h 50m
                        efficiencyScore = 79,
                        efficiencyNotes = "+11% rispetto alla settimana precedente! Ottima costanza nello studio e negli allenamenti.",
                        syntheticSummaryText = "Report Settimana 38:\n• 12/15 eventi svolti (79%)\n• 18h 10m svolte su 23h\n• Notevole progresso rispetto a Settimana 37 (+11%)"
                    )
                )
                sDao.insertSnapshots(sampleSnapshots)
            }
        }
    }

    suspend fun loadStarterRoutineForce() {
        dao.clearAll()
        dao.insertItems(SampleRoutineData.getStarterWeeklyRoutine())
    }
}
