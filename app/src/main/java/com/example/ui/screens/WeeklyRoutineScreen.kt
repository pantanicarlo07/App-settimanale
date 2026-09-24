package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarViewDay
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.automirrored.filled.PlaylistAddCheck
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.DayOfWeek
import com.example.data.RoutineCategory
import com.example.data.RoutineType
import com.example.ui.RoutineViewModel
import com.example.ui.ViewMode
import com.example.ui.components.AddEditRoutineBottomSheet
import com.example.ui.components.DaySelectorBar
import com.example.ui.components.EventTimelineItem
import com.example.ui.components.ExecutionTimeDialog
import com.example.ui.components.ExportProgressBottomSheet
import com.example.ui.components.ReminderChecklistItem
import com.example.ui.components.SevenColumnWeekView
import com.example.ui.components.WeeklyOverviewGrid
import com.example.ui.components.WeeklyStatsCard
import com.example.ui.components.FocusCoachChatScreen
import com.example.data.ExecutionStatus
import com.example.data.RoutineItem
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.Psychology

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyRoutineScreen(
    viewModel: RoutineViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Notification permission launcher for Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Handle snackbar messages
    LaunchedEffect(uiState.messageSnackbar) {
        uiState.messageSnackbar?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    var showCategoryFilterMenu by remember { mutableStateOf(false) }
    var showColumnsLayout by remember { mutableStateOf(true) }
    var executionDialogItem by remember { mutableStateOf<RoutineItem?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Routine Settimanale",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (uiState.viewMode == ViewMode.FOCUS_COACH) "Focus Coach • Guida personale con Gemini"
                            else "La tua settimana perpetua • ${uiState.selectedDay.fullName}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    if (uiState.viewMode == ViewMode.STATS) {
                        IconButton(
                            onClick = { viewModel.openExportSheet() },
                            modifier = Modifier.testTag("topbar_btn_export_stats")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Esporta statistiche e progresso",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    if (uiState.viewMode != ViewMode.FOCUS_COACH) {
                        IconButton(
                            onClick = { showCategoryFilterMenu = !showCategoryFilterMenu },
                            modifier = Modifier.testTag("btn_filter_categories")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (uiState.filterCategory != null) {
                                        Badge(modifier = Modifier.size(8.dp))
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = "Filtra categorie",
                                    tint = if (uiState.filterCategory != null) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                NavigationBarItem(
                    selected = uiState.viewMode == ViewMode.DAY_TIMELINE,
                    onClick = { viewModel.setViewMode(ViewMode.DAY_TIMELINE) },
                    icon = { Icon(Icons.Default.CalendarViewDay, contentDescription = "Vista Giorno") },
                    label = { Text("Giorno") },
                    modifier = Modifier.testTag("nav_item_day")
                )
                NavigationBarItem(
                    selected = uiState.viewMode == ViewMode.WEEK_GRID,
                    onClick = { viewModel.setViewMode(ViewMode.WEEK_GRID) },
                    icon = { Icon(Icons.Default.CalendarViewWeek, contentDescription = "Vista Settimana") },
                    label = { Text("Settimana") },
                    modifier = Modifier.testTag("nav_item_week")
                )
                NavigationBarItem(
                    selected = uiState.viewMode == ViewMode.FOCUS_COACH,
                    onClick = { viewModel.setViewMode(ViewMode.FOCUS_COACH) },
                    icon = { Icon(Icons.Default.Psychology, contentDescription = "Focus Coach AI") },
                    label = { Text("Focus AI") },
                    modifier = Modifier.testTag("nav_item_focus_coach")
                )
                NavigationBarItem(
                    selected = uiState.viewMode == ViewMode.STATS,
                    onClick = { viewModel.setViewMode(ViewMode.STATS) },
                    icon = { Icon(Icons.Default.PieChart, contentDescription = "Riepilogo Routine") },
                    label = { Text("Riepilogo") },
                    modifier = Modifier.testTag("nav_item_stats")
                )
            }
        },
        floatingActionButton = {
            if (uiState.viewMode != ViewMode.STATS && uiState.viewMode != ViewMode.FOCUS_COACH) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.openAddSheet(prefilledDay = uiState.selectedDay) },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Aggiungi") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("fab_add_routine")
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Optional Filter Category Bar
            AnimatedVisibility(visible = showCategoryFilterMenu) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Filtra per Categoria",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (uiState.filterCategory != null) {
                                androidx.compose.material3.TextButton(
                                    onClick = { viewModel.setFilterCategory(null) }
                                ) {
                                    Text("Azzera filtro", fontSize = 11.sp)
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = uiState.filterCategory == null,
                                onClick = { viewModel.setFilterCategory(null) },
                                label = { Text("Tutte", fontSize = 11.sp) }
                            )
                            RoutineCategory.entries.take(4).forEach { cat ->
                                FilterChip(
                                    selected = uiState.filterCategory == cat,
                                    onClick = {
                                        viewModel.setFilterCategory(if (uiState.filterCategory == cat) null else cat)
                                    },
                                    label = { Text(cat.label, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }
            }

            when (uiState.viewMode) {
                ViewMode.DAY_TIMELINE -> {
                    // Day View
                    Spacer(modifier = Modifier.height(6.dp))

                    // 7-day cyclical switcher strip
                    DaySelectorBar(
                        selectedDay = uiState.selectedDay,
                        onDaySelected = { viewModel.selectDay(it) },
                        getEventsCount = { viewModel.uiState.value.getEventsCountForDay(it) },
                        getRemindersCount = { viewModel.uiState.value.getRemindersCountForDay(it) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Sub-filter tabs (Tutti, Eventi, Promemoria)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = uiState.filterType == null,
                            onClick = { viewModel.setFilterType(null) },
                            label = { Text("Tutti (${uiState.dayItems.size})", fontSize = 12.sp) }
                        )
                        FilterChip(
                            selected = uiState.filterType == RoutineType.EVENT,
                            onClick = {
                                viewModel.setFilterType(
                                    if (uiState.filterType == RoutineType.EVENT) null else RoutineType.EVENT
                                )
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(13.dp))
                            },
                            label = { Text("Eventi (${uiState.dayEvents.size})", fontSize = 12.sp) }
                        )
                        FilterChip(
                            selected = uiState.filterType == RoutineType.REMINDER,
                            onClick = {
                                viewModel.setFilterType(
                                    if (uiState.filterType == RoutineType.REMINDER) null else RoutineType.REMINDER
                                )
                            },
                            leadingIcon = {
                                Icon(Icons.AutoMirrored.Filled.PlaylistAddCheck, contentDescription = null, modifier = Modifier.size(13.dp))
                            },
                            label = { Text("Promemoria (${uiState.dayReminders.size})", fontSize = 12.sp) }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Day Content List
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 88.dp)
                    ) {
                        // Section: Eventi
                        if (uiState.filterType != RoutineType.REMINDER) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "EVENTI A ORARIO (${uiState.dayEvents.size})",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        letterSpacing = 0.5.sp
                                    )
                                    androidx.compose.material3.TextButton(
                                        onClick = {
                                            viewModel.openAddSheet(
                                                prefilledDay = uiState.selectedDay,
                                                prefilledType = RoutineType.EVENT
                                            )
                                        }
                                    ) {
                                        Text("+ Evento", fontSize = 12.sp)
                                    }
                                }
                            }

                            if (uiState.dayEvents.isEmpty()) {
                                item {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Nessun evento a orario programmato per ${uiState.selectedDay.fullName}.",
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(14.dp)
                                        )
                                    }
                                }
                            } else {
                                items(uiState.dayEvents, key = { it.id }) { item ->
                                    EventTimelineItem(
                                        item = item,
                                        onEdit = { viewModel.openEditSheet(item) },
                                        onDelete = { viewModel.deleteItem(item) },
                                        onNotifyTest = { viewModel.triggerNotificationTest(context, item) },
                                        onOpenExecutionDialog = { executionDialogItem = item },
                                        onQuickSetStatus = { status, mins -> viewModel.setItemExecution(item, status, mins) }
                                    )
                                }
                            }
                        }

                        // Section: Promemoria
                        if (uiState.filterType != RoutineType.EVENT) {
                            item {
                                Spacer(modifier = Modifier.height(14.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "PROMEMORIA & CHECKLIST (${uiState.dayReminders.size})",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.secondary,
                                        letterSpacing = 0.5.sp
                                    )
                                    androidx.compose.material3.TextButton(
                                        onClick = {
                                            viewModel.openAddSheet(
                                                prefilledDay = uiState.selectedDay,
                                                prefilledType = RoutineType.REMINDER
                                            )
                                        }
                                    ) {
                                        Text("+ Promemoria", fontSize = 12.sp)
                                    }
                                }
                            }

                            if (uiState.dayReminders.isEmpty()) {
                                item {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Nessun promemoria o task per ${uiState.selectedDay.fullName}.",
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(14.dp)
                                        )
                                    }
                                }
                            } else {
                                items(uiState.dayReminders, key = { it.id }) { item ->
                                    ReminderChecklistItem(
                                        item = item,
                                        onToggleCompleted = { viewModel.toggleReminderCompleted(item) },
                                        onEdit = { viewModel.openEditSheet(item) },
                                        onDelete = { viewModel.deleteItem(item) },
                                        onSetStatus = { status ->
                                            viewModel.setItemExecution(
                                                item,
                                                status,
                                                if (status == ExecutionStatus.COMPLETED) item.plannedMinutes else 0
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                ViewMode.WEEK_GRID -> {
                    // Full 7-day cyclical week view (Lun-Dom)
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = showColumnsLayout,
                            onClick = { showColumnsLayout = true },
                            leadingIcon = {
                                Icon(Icons.Default.ViewColumn, contentDescription = null, modifier = Modifier.size(14.dp))
                            },
                            label = { Text("7 Colonne", fontSize = 11.sp) },
                            modifier = Modifier.testTag("chip_layout_7_columns")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        FilterChip(
                            selected = !showColumnsLayout,
                            onClick = { showColumnsLayout = false },
                            leadingIcon = {
                                Icon(Icons.Default.ViewAgenda, contentDescription = null, modifier = Modifier.size(14.dp))
                            },
                            label = { Text("Lista Giorni", fontSize = 11.sp) },
                            modifier = Modifier.testTag("chip_layout_vertical_list")
                        )
                    }

                    if (showColumnsLayout) {
                        SevenColumnWeekView(
                            allItems = uiState.allItems,
                            onAddEventForDay = { day ->
                                viewModel.openAddSheet(prefilledDay = day, prefilledType = RoutineType.EVENT)
                            },
                            onEditItem = { item ->
                                viewModel.openEditSheet(item)
                            },
                            onDeleteItem = { item ->
                                viewModel.deleteItem(item)
                            },
                            onToggleReminder = { reminder ->
                                viewModel.toggleReminderCompleted(reminder)
                            },
                            onDayHeaderClick = { day ->
                                viewModel.selectDay(day)
                                viewModel.setViewMode(ViewMode.DAY_TIMELINE)
                            },
                            onTestNotify = { item ->
                                viewModel.triggerNotificationTest(context, item)
                            },
                            onSetItemStatus = { item, status, mins ->
                                viewModel.setItemExecution(item, status, mins)
                            },
                            onOpenExecutionDialog = { item ->
                                executionDialogItem = item
                            }
                        )
                    } else {
                        WeeklyOverviewGrid(
                            allItems = uiState.allItems,
                            onSelectDay = { day ->
                                viewModel.selectDay(day)
                                viewModel.setViewMode(ViewMode.DAY_TIMELINE)
                            },
                            onAddForDay = { day ->
                                viewModel.openAddSheet(prefilledDay = day)
                            },
                            onToggleReminder = { rem ->
                                viewModel.toggleReminderCompleted(rem)
                            }
                        )
                    }
                }

                ViewMode.FOCUS_COACH -> {
                    // Gemini Focus Coach Chat Screen
                    Spacer(modifier = Modifier.height(4.dp))
                    FocusCoachChatScreen(
                        uiState = uiState,
                        onInputChange = { viewModel.onChatInputChanged(it) },
                        onSendMessage = { prompt -> viewModel.sendChatMessage(prompt) },
                        onClearChat = { viewModel.clearChatHistory() }
                    )
                }

                ViewMode.STATS -> {
                    // Statistics and weekly reset
                    Spacer(modifier = Modifier.height(8.dp))
                    WeeklyStatsCard(
                        uiState = uiState,
                        onSetItemStatus = { item, status, mins ->
                            viewModel.setItemExecution(item, status, mins)
                        },
                        onOpenTimeDialog = { item ->
                            executionDialogItem = item
                        },
                        onResetWeeklyReminders = { viewModel.resetWeeklyReminders() },
                        onRestoreStarterRoutine = { viewModel.restoreStarterRoutine() },
                        onClearAll = { viewModel.clearAllRoutine() }
                    )
                }
            }
        }
    }

    // Modal Bottom Sheet for Add/Edit
    if (uiState.isBottomSheetOpen) {
        AddEditRoutineBottomSheet(
            initialItem = uiState.editingItem,
            onDismiss = { viewModel.closeBottomSheet() },
            onSave = { item, days, isNew ->
                viewModel.saveRoutineItem(item, days, isNew)
            }
        )
    }

    // Execution Time & Result Dialog (Fatto, Non Fatto con X, e regolazione tempo svolto)
    executionDialogItem?.let { item ->
        ExecutionTimeDialog(
            item = item,
            onDismiss = { executionDialogItem = null },
            onConfirm = { status, actualMinutes ->
                viewModel.setItemExecution(item, status, actualMinutes)
                executionDialogItem = null
            }
        )
    }
}
