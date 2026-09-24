package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DayOfWeek
import com.example.data.ExecutionStatus
import com.example.data.ProgressSnapshotEntity
import com.example.data.RoutineCategory
import com.example.data.RoutineItem
import com.example.data.RoutineRepository
import com.example.data.RoutineType
import com.example.data.StatsExporter
import com.example.gemini.ChatMessage
import com.example.gemini.GeminiChatService
import com.example.notifications.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.IsoFields
import java.util.Locale

data class ViewSettings(
    val selectedDay: DayOfWeek = DayOfWeek.MONDAY,
    val viewMode: ViewMode = ViewMode.DAY_TIMELINE,
    val filterCategory: RoutineCategory? = null,
    val filterType: RoutineType? = null,
    val isBottomSheetOpen: Boolean = false,
    val isExportSheetOpen: Boolean = false,
    val editingItem: RoutineItem? = null,
    val snackbarMessage: String? = null,
    val chatMessages: List<ChatMessage> = listOf(
        ChatMessage(
            isUser = false,
            text = "Ciao! Sono il tuo Focus Coach personale 🧠✨.\n\nSono qui per aiutarti con gentilezza e metodo a riprendere la concentrazione, superare la procrastinazione o organizzare i blocchi della tua routine settimanale in modo equilibrato.\n\nCome ti senti oggi? Di cosa hai bisogno per ritrovare il tuo ritmo?"
        )
    ),
    val isChatLoading: Boolean = false,
    val chatInputText: String = ""
)

class RoutineViewModel(
    private val repository: RoutineRepository
) : ViewModel() {

    private val initialDay = determineInitialDay()
    private val _settings = MutableStateFlow(ViewSettings(selectedDay = initialDay))
    private val geminiChatService = GeminiChatService()

    val uiState: StateFlow<RoutineUiState> = combine(
        repository.allItems,
        repository.allSnapshots,
        _settings
    ) { items, snapshots, settings ->
        RoutineUiState(
            selectedDay = settings.selectedDay,
            viewMode = settings.viewMode,
            filterCategory = settings.filterCategory,
            filterType = settings.filterType,
            allItems = items,
            snapshots = snapshots,
            isLoading = false,
            isBottomSheetOpen = settings.isBottomSheetOpen,
            isExportSheetOpen = settings.isExportSheetOpen,
            editingItem = settings.editingItem,
            messageSnackbar = settings.snackbarMessage,
            chatMessages = settings.chatMessages,
            isChatLoading = settings.isChatLoading,
            chatInputText = settings.chatInputText
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RoutineUiState(selectedDay = initialDay)
    )

    init {
        viewModelScope.launch {
            repository.loadStarterRoutineIfEmpty()
        }
    }

    private fun determineInitialDay(): DayOfWeek {
        return try {
            val dayValue = LocalDate.now().dayOfWeek.value // 1 = Monday, 7 = Sunday
            DayOfWeek.fromIndex(dayValue)
        } catch (e: Exception) {
            DayOfWeek.MONDAY
        }
    }

    fun selectDay(day: DayOfWeek) {
        _settings.update { it.copy(selectedDay = day) }
    }

    fun setViewMode(mode: ViewMode) {
        _settings.update { it.copy(viewMode = mode) }
    }

    fun setFilterCategory(category: RoutineCategory?) {
        _settings.update { it.copy(filterCategory = category) }
    }

    fun setFilterType(type: RoutineType?) {
        _settings.update { it.copy(filterType = type) }
    }

    fun openAddSheet(
        prefilledDay: DayOfWeek? = null,
        prefilledType: RoutineType = RoutineType.EVENT
    ) {
        val targetDay = prefilledDay ?: _settings.value.selectedDay
        val newItem = RoutineItem(
            title = "",
            type = prefilledType,
            dayOfWeek = targetDay.dayIndex,
            startHour = if (prefilledType == RoutineType.EVENT) 9 else null,
            startMinute = if (prefilledType == RoutineType.EVENT) 0 else null,
            endHour = if (prefilledType == RoutineType.EVENT) 10 else null,
            endMinute = if (prefilledType == RoutineType.EVENT) 0 else null
        )
        _settings.update {
            it.copy(
                isBottomSheetOpen = true,
                editingItem = newItem
            )
        }
    }

    fun openEditSheet(item: RoutineItem) {
        _settings.update {
            it.copy(
                isBottomSheetOpen = true,
                editingItem = item
            )
        }
    }

    fun closeBottomSheet() {
        _settings.update {
            it.copy(
                isBottomSheetOpen = false,
                editingItem = null
            )
        }
    }

    fun saveRoutineItem(
        item: RoutineItem,
        selectedDays: Set<Int>,
        isNewItem: Boolean
    ) {
        viewModelScope.launch {
            if (isNewItem) {
                if (selectedDays.size <= 1) {
                    val dayToSave = selectedDays.firstOrNull() ?: item.dayOfWeek
                    repository.insertItem(item.copy(dayOfWeek = dayToSave))
                } else {
                    repository.insertItemsForDays(selectedDays, item)
                }
                _settings.update { it.copy(snackbarMessage = "Elemento aggiunto alla routine!") }
            } else {
                repository.updateItem(item)
                _settings.update { it.copy(snackbarMessage = "Elemento aggiornato!") }
            }
            closeBottomSheet()
        }
    }

    fun toggleReminderCompleted(item: RoutineItem) {
        viewModelScope.launch {
            val newCompleted = !item.isCompleted
            repository.setReminderCompleted(item.id, newCompleted)
        }
    }

    fun setItemExecution(item: RoutineItem, status: ExecutionStatus, actualMinutes: Int?) {
        viewModelScope.launch {
            repository.setItemExecution(item.id, status, actualMinutes)
            val msg = when (status) {
                ExecutionStatus.COMPLETED -> "Segnato come Fatto ✅ (${actualMinutes?.let { RoutineItem.formatMinutes(it) } ?: "durata prevista"})"
                ExecutionStatus.MISSED -> "Segnato come Non Fatto ❌"
                ExecutionStatus.PENDING -> "Reimpostato su Non Assegnato"
            }
            _settings.update { it.copy(snackbarMessage = msg) }
        }
    }

    fun deleteItem(item: RoutineItem) {
        viewModelScope.launch {
            repository.deleteItem(item)
            _settings.update { it.copy(snackbarMessage = "Rimosso dalla routine") }
        }
    }

    fun resetWeeklyReminders() {
        viewModelScope.launch {
            repository.resetAllWeeklyReminders()
            _settings.update { it.copy(snackbarMessage = "Tutti i promemoria sono stati reimpostati per una nuova settimana!") }
        }
    }

    fun restoreStarterRoutine() {
        viewModelScope.launch {
            repository.loadStarterRoutineForce()
            _settings.update { it.copy(snackbarMessage = "Routine iniziale d'esempio ripristinata!") }
        }
    }

    fun clearAllRoutine() {
        viewModelScope.launch {
            repository.clearAll()
            _settings.update { it.copy(snackbarMessage = "Tutti gli elementi sono stati rimossi.") }
        }
    }

    fun clearSnackbar() {
        _settings.update { it.copy(snackbarMessage = null) }
    }

    fun triggerNotificationTest(context: Context, item: RoutineItem) {
        NotificationHelper.showRoutineNotification(context, item)
        _settings.update { it.copy(snackbarMessage = "Notifica inviata per ${item.title}") }
    }

    // --- Export & Progress Over Time ---

    fun openExportSheet() {
        _settings.update { it.copy(isExportSheetOpen = true) }
    }

    fun closeExportSheet() {
        _settings.update { it.copy(isExportSheetOpen = false) }
    }

    fun saveProgressSnapshot(customLabel: String? = null) {
        viewModelScope.launch {
            val items = uiState.value.allItems
            val totalPlanned = items.sumOf { it.plannedMinutes }
            val totalDone = items.sumOf { it.doneMinutes }
            val totalNotDone = items.sumOf { it.notDoneMinutes }
            val completedCount = items.count { it.executionStatus == ExecutionStatus.COMPLETED }
            val eventsCount = items.count { it.type == RoutineType.EVENT }
            val completedEvents = items.count { it.type == RoutineType.EVENT && it.executionStatus == ExecutionStatus.COMPLETED }
            val missedEvents = items.count { it.type == RoutineType.EVENT && it.executionStatus == ExecutionStatus.MISSED }
            val pendingEvents = items.count { it.type == RoutineType.EVENT && it.executionStatus == ExecutionStatus.PENDING }

            val completionPercent = if (items.isNotEmpty()) (completedCount * 100) / items.size else 0

            val now = LocalDate.now()
            val weekNum = now.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
            val monthName = now.month.getDisplayName(TextStyle.FULL, Locale.ITALIAN).replaceFirstChar { it.uppercase() }
            val periodLabel = customLabel ?: "Settimana $weekNum ($monthName ${now.year})"

            val summaryText = StatsExporter.generateSyntheticTextReport(items, periodLabel)
            val notes = when {
                completionPercent >= 80 -> "Eccellente prestazione! Oltre l'80% delle attività completate."
                completionPercent >= 60 -> "Costanza solida con buon ritmo di avanzamento."
                else -> "Settimana di riallineamento delle priorità."
            }

            val snapshot = ProgressSnapshotEntity(
                periodLabel = periodLabel,
                completionPercentage = completionPercent,
                totalEventsCount = eventsCount,
                completedEventsCount = completedEvents,
                missedEventsCount = missedEvents,
                pendingEventsCount = pendingEvents,
                totalPlannedMinutes = totalPlanned,
                totalDoneMinutes = totalDone,
                totalNotDoneMinutes = totalNotDone,
                efficiencyScore = completionPercent,
                efficiencyNotes = notes,
                syntheticSummaryText = summaryText
            )

            repository.saveSnapshot(snapshot)
            _settings.update { it.copy(snackbarMessage = "Istantanea '$periodLabel' salvata nel progresso storico! 📈") }
        }
    }

    fun deleteProgressSnapshot(id: Long) {
        viewModelScope.launch {
            repository.deleteSnapshot(id)
            _settings.update { it.copy(snackbarMessage = "Istantanea rimossa dal progresso storico.") }
        }
    }

    fun exportSyntheticReport(context: Context, periodLabel: String = "Settimana Corrente", includeReminders: Boolean = true) {
        val text = StatsExporter.generateSyntheticTextReport(uiState.value.allItems, periodLabel, includeReminders)
        StatsExporter.shareText(context, "Report Progresso Routine - $periodLabel", text)
    }

    fun exportCsv(context: Context) {
        val csv = StatsExporter.generateCsvData(uiState.value.allItems)
        val fileName = "week_routine_statistiche_${System.currentTimeMillis()}.csv"
        StatsExporter.shareCsvFile(context, fileName, csv)
    }

    fun copySyntheticReport(context: Context, periodLabel: String = "Settimana Corrente", includeReminders: Boolean = true) {
        val text = StatsExporter.generateSyntheticTextReport(uiState.value.allItems, periodLabel, includeReminders)
        StatsExporter.copyToClipboard(context, "Report Sintetico Routine", text)
    }

    // --- Gemini Focus Coach Chat Methods ---

    fun onChatInputChanged(text: String) {
        _settings.update { it.copy(chatInputText = text) }
    }

    fun sendChatMessage(customPrompt: String? = null) {
        val messageText = (customPrompt ?: _settings.value.chatInputText).trim()
        if (messageText.isBlank() || _settings.value.isChatLoading) return

        val userMsg = ChatMessage(isUser = true, text = messageText)
        val updatedHistory = _settings.value.chatMessages + userMsg

        _settings.update {
            it.copy(
                chatMessages = updatedHistory,
                chatInputText = "",
                isChatLoading = true
            )
        }

        viewModelScope.launch {
            val result = geminiChatService.sendMessage(
                history = updatedHistory,
                userMessage = messageText
            )
            result.onSuccess { reply ->
                val botMsg = ChatMessage(isUser = false, text = reply)
                _settings.update {
                    it.copy(
                        chatMessages = it.chatMessages + botMsg,
                        isChatLoading = false
                    )
                }
            }.onFailure { error ->
                val errorMsg = ChatMessage(
                    isUser = false,
                    text = "Mi dispiace, si è verificato un problema:\n${error.localizedMessage ?: "Errore imprevisto"}\n\nAssicurati che la chiave GEMINI_API_KEY sia impostata nei Secrets di AI Studio.",
                    isError = true
                )
                _settings.update {
                    it.copy(
                        chatMessages = it.chatMessages + errorMsg,
                        isChatLoading = false
                    )
                }
            }
        }
    }

    fun clearChatHistory() {
        val initialBotMsg = ChatMessage(
            isUser = false,
            text = "Conversazione azzerata. Quando vuoi, sono qui per aiutarti a ritrovare attenzione e concentrazione nella tua routine."
        )
        _settings.update {
            it.copy(chatMessages = listOf(initialBotMsg))
        }
    }
}
