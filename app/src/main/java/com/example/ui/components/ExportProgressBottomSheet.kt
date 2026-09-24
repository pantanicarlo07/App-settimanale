package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ExecutionStatus
import com.example.data.ProgressSnapshotEntity
import com.example.data.RoutineItem
import com.example.data.RoutineType
import com.example.data.StatsExporter
import com.example.ui.RoutineUiState
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.IsoFields
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportProgressBottomSheet(
    uiState: RoutineUiState,
    onDismiss: () -> Unit,
    onSaveCurrentSnapshot: (String) -> Unit,
    onDeleteSnapshot: (Long) -> Unit,
    onExportCsv: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableIntStateOf(0) }

    val defaultPeriodLabel = remember {
        val now = LocalDate.now()
        val weekNum = now.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
        val monthName = now.month.getDisplayName(TextStyle.FULL, Locale.ITALIAN).replaceFirstChar { it.uppercase() }
        "Settimana $weekNum ($monthName ${now.year})"
    }
    var periodLabel by remember { mutableStateOf(defaultPeriodLabel) }
    var includeReminders by remember { mutableStateOf(true) }

    // Live report preview calculation
    val reportText by remember(uiState.allItems, periodLabel, includeReminders) {
        derivedStateOf {
            StatsExporter.generateSyntheticTextReport(
                items = uiState.allItems,
                periodLabel = periodLabel,
                includeReminders = includeReminders
            )
        }
    }

    val totalItems = uiState.allItems.size
    val completedCount = uiState.allItems.count { it.executionStatus == ExecutionStatus.COMPLETED }
    val missedCount = uiState.allItems.count { it.executionStatus == ExecutionStatus.MISSED }
    val totalDoneMins = uiState.allItems.sumOf { it.doneMinutes }
    val totalNotDoneMins = uiState.allItems.sumOf { it.notDoneMinutes }
    val completionPercent = if (totalItems > 0) (completedCount * 100) / totalItems else 0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("export_progress_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Esporta Statistiche & Progresso",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Sintesi di completamento e andamento nel tempo",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Chiudi")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tab Row: Report Sintetico vs Progresso nel Tempo
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Report Sintetico", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    },
                    modifier = Modifier.testTag("tab_export_synthetic")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text(
                                "Progresso (${uiState.snapshots.size})",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    },
                    modifier = Modifier.testTag("tab_progress_over_time")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                0 -> {
                    SyntheticExportTabContent(
                        periodLabel = periodLabel,
                        onPeriodLabelChange = { periodLabel = it },
                        includeReminders = includeReminders,
                        onIncludeRemindersChange = { includeReminders = it },
                        reportText = reportText,
                        completionPercent = completionPercent,
                        completedCount = completedCount,
                        missedCount = missedCount,
                        totalDoneMins = totalDoneMins,
                        totalNotDoneMins = totalNotDoneMins,
                        onCopyText = {
                            StatsExporter.copyToClipboard(context, "Report Sintetico Routine", reportText)
                        },
                        onShareReport = {
                            StatsExporter.shareText(context, "Report Progresso - $periodLabel", reportText)
                        },
                        onExportCsv = onExportCsv,
                        onSaveSnapshot = {
                            onSaveCurrentSnapshot(periodLabel)
                        }
                    )
                }
                1 -> {
                    ProgressOverTimeTabContent(
                        snapshots = uiState.snapshots,
                        currentCompletion = completionPercent,
                        currentPeriodLabel = periodLabel,
                        onSaveCurrentSnapshot = { onSaveCurrentSnapshot(periodLabel) },
                        onDeleteSnapshot = onDeleteSnapshot,
                        onShareSnapshot = { snapshot ->
                            StatsExporter.shareText(
                                context,
                                "Archivio Progresso - ${snapshot.periodLabel}",
                                snapshot.syntheticSummaryText.ifBlank {
                                    "Progresso ${snapshot.periodLabel}:\nCompletamento: ${snapshot.completionPercentage}%\nTempo svolto: ${snapshot.formattedDoneDuration}\nTempo perso: ${snapshot.formattedNotDoneDuration}"
                                }
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SyntheticExportTabContent(
    periodLabel: String,
    onPeriodLabelChange: (String) -> Unit,
    includeReminders: Boolean,
    onIncludeRemindersChange: (Boolean) -> Unit,
    reportText: String,
    completionPercent: Int,
    completedCount: Int,
    missedCount: Int,
    totalDoneMins: Int,
    totalNotDoneMins: Int,
    onCopyText: () -> Unit,
    onShareReport: () -> Unit,
    onExportCsv: () -> Unit,
    onSaveSnapshot: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Live KPI Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF10B981).copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("COMPLETAMENTO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF047857))
                    Text("$completionPercent%", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF047857))
                    Text("$completedCount fatti", fontSize = 10.sp, color = Color(0xFF047857).copy(alpha = 0.8f))
                }
            }

            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("TEMPO FATTO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(RoutineItem.formatMinutes(totalDoneMins), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    Text("ore dedicate", fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }

            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFEF4444).copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("NON FATTO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                    Text(RoutineItem.formatMinutes(totalNotDoneMins), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFDC2626))
                    Text("$missedCount con X", fontSize = 10.sp, color = Color(0xFFDC2626).copy(alpha = 0.8f))
                }
            }
        }

        // Period Label Input
        OutlinedTextField(
            value = periodLabel,
            onValueChange = onPeriodLabelChange,
            label = { Text("Etichetta Periodo Esportazione") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_export_period_label"),
            shape = RoundedCornerShape(12.dp)
        )

        // Include Reminders Switch
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Includi promemoria e to-do", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Text("Aggiunge lo stato delle checklist oltre agli eventi a orario", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = includeReminders,
                onCheckedChange = onIncludeRemindersChange,
                modifier = Modifier.testTag("switch_include_reminders")
            )
        }

        // Action Buttons Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onShareReport,
                modifier = Modifier
                    .weight(1.3f)
                    .testTag("btn_share_synthetic_report"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Condividi Report", fontSize = 13.sp)
            }

            OutlinedButton(
                onClick = onCopyText,
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_copy_synthetic_report"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copia", fontSize = 13.sp)
            }

            FilledTonalButton(
                onClick = onExportCsv,
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_export_csv_file"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("CSV", fontSize = 13.sp)
            }
        }

        // Save Snapshot in History Action
        FilledTonalButton(
            onClick = onSaveSnapshot,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("btn_save_snapshot_history"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.BookmarkBorder, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Salva Istantanea nel Progresso Storico 📈", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }

        // Synthetic Report Text Preview Box
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Anteprima Formato Sintetico",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Pronto per la condivisione",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = reportText,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .testTag("report_text_preview")
                )
            }
        }
    }
}

@Composable
private fun ProgressOverTimeTabContent(
    snapshots: List<ProgressSnapshotEntity>,
    currentCompletion: Int,
    currentPeriodLabel: String,
    onSaveCurrentSnapshot: () -> Unit,
    onDeleteSnapshot: (Long) -> Unit,
    onShareSnapshot: (ProgressSnapshotEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Trend Hero Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Andamento nel Tempo",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Confronto storico tra settimane registrate",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (snapshots.isNotEmpty()) {
                    val latest = snapshots.first()
                    val oldest = snapshots.last()
                    val diff = latest.completionPercentage - oldest.completionPercentage

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (diff >= 0) "+$diff%" else "$diff%",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (diff >= 0) Color(0xFF047857) else Color(0xFFDC2626)
                        )
                        Text(
                            text = if (diff >= 0)
                                "progresso complessivo rispetto alla prima registrazione!"
                            else
                                "variazione rispetto alla prima registrazione",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                } else {
                    Text(
                        text = "Salva l'istantanea della settimana corrente per iniziare a tracciare la tua evoluzione nel tempo.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onSaveCurrentSnapshot,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_save_current_snapshot_from_trend"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.BookmarkBorder, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Salva Settimana Corrente ($currentCompletion%)", fontSize = 12.sp)
                }
            }
        }

        // Timeline of Snapshots
        Text(
            text = "Settimane Registrate (${snapshots.size})",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (snapshots.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Nessun progresso storico archiviato",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Salva la settimana corrente per visualizzare qui il confronto cronologico.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            snapshots.forEach { snapshot ->
                SnapshotCardItem(
                    snapshot = snapshot,
                    onDelete = { onDeleteSnapshot(snapshot.id) },
                    onShare = { onShareSnapshot(snapshot) }
                )
            }
        }
    }
}

@Composable
private fun SnapshotCardItem(
    snapshot: ProgressSnapshotEntity,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    var expandedDetails by remember { mutableStateOf(false) }

    val statusColor = when {
        snapshot.completionPercentage >= 75 -> Color(0xFF10B981)
        snapshot.completionPercentage >= 50 -> Color(0xFFF59E0B)
        else -> Color(0xFFEF4444)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("snapshot_card_${snapshot.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            statusColor.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = snapshot.periodLabel,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Registrato il ${snapshot.formattedDate}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "${snapshot.completionPercentage}%",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { snapshot.completionPercentage.toFloat() / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = statusColor,
                trackColor = MaterialTheme.colorScheme.surface,
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Stats grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "✅ ${snapshot.completedEventsCount}/${snapshot.totalEventsCount} eventi",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "⏱️ ${snapshot.formattedDoneDuration} svolte",
                    fontSize = 11.sp,
                    color = Color(0xFF047857),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "❌ ${snapshot.formattedNotDoneDuration} perse",
                    fontSize = 11.sp,
                    color = Color(0xFFDC2626),
                    fontWeight = FontWeight.Medium
                )
            }

            if (snapshot.efficiencyNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = snapshot.efficiencyNotes,
                    fontSize = 11.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Actions: Expand, Share, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (expandedDetails) "Nascondi dettaglio ▲" else "Vedi report salvato ▼",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { expandedDetails = !expandedDetails }
                        .padding(4.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onShare,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Condividi questa istantanea",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Elimina istantanea",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            AnimatedVisibility(visible = expandedDetails) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(10.dp)
                ) {
                    Text(
                        text = snapshot.syntheticSummaryText.ifBlank { "Nessun dettaglio testuale aggiuntivo." },
                        fontSize = 10.sp,
                        lineHeight = 15.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
