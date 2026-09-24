package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DayOfWeek
import com.example.data.ExecutionStatus
import com.example.data.RoutineCategory
import com.example.data.RoutineItem
import com.example.data.RoutineType
import com.example.ui.RoutineUiState

enum class StatusFilter(val label: String) {
    ALL("Tutti"),
    COMPLETED("Fatti ✅"),
    MISSED("Non Fatti ❌"),
    PENDING("Non Assegnati ⏳")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WeeklyStatsCard(
    uiState: RoutineUiState,
    onSetItemStatus: (RoutineItem, ExecutionStatus, Int?) -> Unit,
    onOpenTimeDialog: (RoutineItem) -> Unit,
    onOpenExportSheet: () -> Unit,
    onResetWeeklyReminders: () -> Unit,
    onRestoreStarterRoutine: () -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    var statusFilter by remember { mutableStateOf(StatusFilter.ALL) }
    var selectedDayFilter by remember { mutableStateOf<DayOfWeek?>(null) }
    var treatUnassignedAsMissed by remember { mutableStateOf(false) }

    // Computations across the entire week
    val allItems = uiState.allItems

    val totalPlannedMinutes = allItems.sumOf { it.plannedMinutes }
    val totalDoneMinutes = allItems.sumOf { it.doneMinutes }
    val rawNotDoneMinutes = allItems.sumOf { it.notDoneMinutes }
    val rawPendingMinutes = (totalPlannedMinutes - totalDoneMinutes - rawNotDoneMinutes).coerceAtLeast(0)

    val totalNotDoneMinutes = if (treatUnassignedAsMissed) rawNotDoneMinutes + rawPendingMinutes else rawNotDoneMinutes
    val totalPendingMinutes = if (treatUnassignedAsMissed) 0 else rawPendingMinutes

    val timeCompletionRatio = if (totalPlannedMinutes > 0) {
        totalDoneMinutes.toFloat() / totalPlannedMinutes.toFloat()
    } else 0f

    val completedItemsCount = allItems.count { it.executionStatus == ExecutionStatus.COMPLETED }
    val rawMissedCount = allItems.count { it.executionStatus == ExecutionStatus.MISSED }
    val rawPendingCount = allItems.count { it.executionStatus == ExecutionStatus.PENDING }

    val missedItemsCount = if (treatUnassignedAsMissed) rawMissedCount + rawPendingCount else rawMissedCount
    val pendingItemsCount = if (treatUnassignedAsMissed) 0 else rawPendingCount

    // Filtered items list
    val filteredList = allItems.filter { item ->
        val matchesDay = selectedDayFilter == null || item.dayOfWeek == selectedDayFilter?.dayIndex
        val matchesStatus = when (statusFilter) {
            StatusFilter.ALL -> true
            StatusFilter.COMPLETED -> item.executionStatus == ExecutionStatus.COMPLETED
            StatusFilter.MISSED -> {
                if (treatUnassignedAsMissed) {
                    item.executionStatus == ExecutionStatus.MISSED || item.executionStatus == ExecutionStatus.PENDING
                } else {
                    item.executionStatus == ExecutionStatus.MISSED
                }
            }
            StatusFilter.PENDING -> {
                if (treatUnassignedAsMissed) false
                else item.executionStatus == ExecutionStatus.PENDING
            }
        }
        matchesDay && matchesStatus
    }.sortedWith(
        compareBy<RoutineItem> { it.dayOfWeek }
            .thenBy { it.sortMinutes }
    )

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // --- 1. HERO CARD: ORE FATTE VS NON FATTE ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("weekly_time_stats_hero_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Bilancio Orario Settimanale",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Monitoraggio preciso del tempo svolto vs tempo perso",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = onOpenExportSheet,
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("btn_open_export_sheet_hero")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("Esporta", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 3 KPI Cards: Fatto, Non Fatto, In Attesa
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Fatto KPI
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "FATTO ✅",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF047857)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = RoutineItem.formatMinutes(totalDoneMinutes),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF047857)
                                )
                                Text(
                                    text = "${(timeCompletionRatio * 100).toInt()}% tempo",
                                    fontSize = 11.sp,
                                    color = Color(0xFF047857).copy(alpha = 0.8f)
                                )
                            }
                        }

                        // Non Fatto KPI
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFEF4444).copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "NON FATTO ❌",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFDC2626)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = RoutineItem.formatMinutes(totalNotDoneMinutes),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFDC2626)
                                )
                                val notDoneRatio = if (totalPlannedMinutes > 0) (totalNotDoneMinutes.toFloat() / totalPlannedMinutes.toFloat()) else 0f
                                Text(
                                    text = "${(notDoneRatio * 100).toInt()}% perso",
                                    fontSize = 11.sp,
                                    color = Color(0xFFDC2626).copy(alpha = 0.8f)
                                )
                            }
                        }

                        // Pianificato / In Attesa KPI
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "IN ATTESA ⏳",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = RoutineItem.formatMinutes(totalPendingMinutes),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "su ${RoutineItem.formatMinutes(totalPlannedMinutes)}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Multi-Segment Visual Time Bar
                    Text(
                        text = "Ripartizione del tempo pianificato:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        if (totalPlannedMinutes > 0) {
                            if (totalDoneMinutes > 0) {
                                Box(
                                    modifier = Modifier
                                        .weight(totalDoneMinutes.toFloat())
                                        .fillMaxWidth()
                                        .background(Color(0xFF10B981))
                                )
                            }
                            if (totalNotDoneMinutes > 0) {
                                Box(
                                    modifier = Modifier
                                        .weight(totalNotDoneMinutes.toFloat())
                                        .fillMaxWidth()
                                        .background(Color(0xFFEF4444))
                                )
                            }
                            if (totalPendingMinutes > 0) {
                                Box(
                                    modifier = Modifier
                                        .weight(totalPendingMinutes.toFloat())
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "🟢 Fatto: ${RoutineItem.formatMinutes(totalDoneMinutes)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF047857)
                        )
                        Text(
                            text = "🔴 Non Fatto: ${RoutineItem.formatMinutes(totalNotDoneMinutes)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFDC2626)
                        )
                        Text(
                            text = "⚪ In Sospeso: ${RoutineItem.formatMinutes(totalPendingMinutes)}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Activity Count Badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text(
                            text = "Totale attività: ${allItems.size}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "✅ Fatte: $completedItemsCount",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF047857)
                        )
                        Text(
                            text = "❌ Non fatte: $missedItemsCount",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFDC2626)
                        )
                        Text(
                            text = "⏳ Da fare: $pendingItemsCount",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // "Non assegnato = Non fatto" Toggle Switch & Bulk Action
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Regola: \"Non assegnato = Non fatto\"",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (treatUnassignedAsMissed)
                                            "Tutti gli impegni senza spunta sono conteggiati come ❌ NON FATTI"
                                        else
                                            "Gli impegni senza spunta rimangono come ⏳ In Attesa",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = treatUnassignedAsMissed,
                                    onCheckedChange = { treatUnassignedAsMissed = it },
                                    modifier = Modifier.testTag("switch_treat_unassigned_as_missed")
                                )
                            }

                            if (rawPendingCount > 0) {
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = {
                                        allItems.filter { it.executionStatus == ExecutionStatus.PENDING }.forEach { item ->
                                            onSetItemStatus(item, ExecutionStatus.MISSED, 0)
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("btn_bulk_mark_unassigned_as_missed"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626))
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Segna tutti i $rawPendingCount non assegnati come Non Fatti (X)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 1.5 PROMO CARD: PROGRESSO NEL TEMPO & REPORT SINTETICO ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_progress_and_export_overview"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Progresso nel Tempo & Esportazione",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${uiState.snapshots.size} settimane salvate nello storico",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Report Sintetico",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Esporta un riepilogo sintetico con spunte (✅/❌), percentuali e bilancio orario da condividere o copiare, oppure visualizza l'andamento del tuo progresso cronologico.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onOpenExportSheet,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_open_export_progress_full"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Visualizza Progresso & Esporta Report", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // --- 2. DETTAGLIO: COSA HO FATTO E COSA NO (FILTRI & LISTA) ---
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Dettaglio Attività & Promemoria",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${filteredList.size} elementi",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Status Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StatusFilter.entries.forEach { f ->
                        val isSelected = statusFilter == f
                        FilterChip(
                            selected = isSelected,
                            onClick = { statusFilter = f },
                            label = { Text(f.label, fontSize = 12.sp) },
                            modifier = Modifier.testTag("filter_status_${f.name.lowercase()}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Day Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedDayFilter == null,
                        onClick = { selectedDayFilter = null },
                        label = { Text("Tutta la Settimana", fontSize = 11.sp) }
                    )
                    DayOfWeek.entries.forEach { d ->
                        val isSelected = selectedDayFilter == d
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedDayFilter = if (isSelected) null else d },
                            label = { Text(d.shortName, fontSize = 11.sp) },
                            modifier = Modifier.testTag("filter_day_${d.shortName.lowercase()}")
                        )
                    }
                }
            }
        }

        // Item rows
        if (filteredList.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Nessuna attività corrisponde ai filtri selezionati.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(20.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            items(filteredList, key = { it.id }) { item ->
                DetailedItemExecutionCard(
                    item = item,
                    onSetStatus = { status, mins -> onSetItemStatus(item, status, mins) },
                    onOpenTimeDialog = { onOpenTimeDialog(item) }
                )
            }
        }

        // --- 3. TEMPO PER CATEGORIA ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "Tempo Svolto per Categoria",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Riepilogo ore fatte vs non fatte per ogni area di vita",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    RoutineCategory.entries.forEach { category ->
                        val categoryItems = allItems.filter { it.category == category }
                        if (categoryItems.isNotEmpty()) {
                            val catColor = try {
                                Color(android.graphics.Color.parseColor(category.defaultColorHex))
                            } catch (e: Exception) {
                                MaterialTheme.colorScheme.primary
                            }

                            val catPlanned = categoryItems.sumOf { it.plannedMinutes }
                            val catDone = categoryItems.sumOf { it.doneMinutes }
                            val catNotDone = categoryItems.sumOf { it.notDoneMinutes }

                            Column(modifier = Modifier.padding(vertical = 5.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(catColor)
                                        )
                                        Text(
                                            text = category.label,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Text(
                                        text = "🟢 ${RoutineItem.formatMinutes(catDone)} fatti • 🔴 ${RoutineItem.formatMinutes(catNotDone)} persi",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Progress bar for category
                                val catRatio = if (catPlanned > 0) (catDone.toFloat() / catPlanned.toFloat()) else 0f
                                LinearProgressIndicator(
                                    progress = { catRatio },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = catColor,
                                    trackColor = catColor.copy(alpha = 0.2f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 4. GESTIONE CICLO SETTIMANALE ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "Gestione Ciclo Settimanale",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Reimposta gli stati di esecuzione per ricominciare una nuova settimana.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    FilledTonalButton(
                        onClick = onOpenExportSheet,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_export_before_reset"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Esporta e Salva Settimana prima di azzerare 📈")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = onResetWeeklyReminders,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_reset_weekly_stats"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Nuova Settimana (Azzera Spunte e Tempi)")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = onRestoreStarterRoutine,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_restore_starter_routine"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ripristina Esempio Routine")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = onClearAll,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_clear_all_routine"),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Svuota Tutta la Routine")
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailedItemExecutionCard(
    item: RoutineItem,
    onSetStatus: (ExecutionStatus, Int?) -> Unit,
    onOpenTimeDialog: () -> Unit
) {
    val status = item.executionStatus
    val isFatto = status == ExecutionStatus.COMPLETED
    val isNonFatto = status == ExecutionStatus.MISSED
    val isPending = status == ExecutionStatus.PENDING

    val accentColor = try {
        Color(android.graphics.Color.parseColor(item.colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenTimeDialog() }
            .testTag("stats_detail_item_${item.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isFatto -> Color(0xFF10B981).copy(alpha = 0.08f)
                isNonFatto -> Color(0xFFEF4444).copy(alpha = 0.08f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            when {
                isFatto -> Color(0xFF10B981).copy(alpha = 0.35f)
                isNonFatto -> Color(0xFFEF4444).copy(alpha = 0.35f)
                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header: Day & Status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = item.dayEnum.fullName,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = accentColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = item.category.label,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = accentColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        isFatto -> Color(0xFF10B981)
                        isNonFatto -> Color(0xFFEF4444)
                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = when {
                                isFatto -> Icons.Default.Check
                                isNonFatto -> Icons.Default.Close
                                else -> Icons.Default.HourglassEmpty
                            },
                            contentDescription = null,
                            tint = if (isPending) MaterialTheme.colorScheme.onSurfaceVariant else Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = when {
                                isFatto -> "FATTO"
                                isNonFatto -> "NON FATTO"
                                else -> "NON ASSEGNATO"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isPending) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Title
            Text(
                text = item.title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textDecoration = if (isNonFatto) TextDecoration.LineThrough else TextDecoration.None,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Time & Metrics breakdown
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Time summary
                Column {
                    if (item.formattedTime.isNotEmpty()) {
                        Text(
                            text = "Orario: ${item.formattedTime} (previsti ${item.formattedPlannedDuration})",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "Durata prevista: ${item.formattedPlannedDuration}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    when {
                        isFatto -> {
                            val done = item.doneMinutes
                            val notDone = item.notDoneMinutes
                            Text(
                                text = "🟢 Svolto: ${RoutineItem.formatMinutes(done)}" + if (notDone > 0) " (🔴 non fatti ${RoutineItem.formatMinutes(notDone)})" else "",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF047857)
                            )
                        }
                        isNonFatto -> {
                            Text(
                                text = "🔴 Saltato interamente: ${item.formattedPlannedDuration} persi",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFDC2626)
                            )
                        }
                        else -> {
                            Text(
                                text = "⏳ Non ancora registrato come fatto o non fatto",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Quick Action Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Quick FATTO (Check)
                    IconButton(
                        onClick = {
                            onSetStatus(ExecutionStatus.COMPLETED, item.plannedMinutes)
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isFatto) Color(0xFF10B981) else MaterialTheme.colorScheme.surface)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Segna Fatto",
                            tint = if (isFatto) Color.White else Color(0xFF10B981),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Quick NON FATTO (Cross X)
                    IconButton(
                        onClick = {
                            onSetStatus(ExecutionStatus.MISSED, 0)
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isNonFatto) Color(0xFFEF4444) else MaterialTheme.colorScheme.surface)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Segna Non Fatto",
                            tint = if (isNonFatto) Color.White else Color(0xFFEF4444),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Open time adjustment dialog
                    IconButton(
                        onClick = onOpenTimeDialog,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Imposta tempo svolto",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
