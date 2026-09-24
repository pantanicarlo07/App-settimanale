package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ExecutionStatus
import com.example.data.RoutineItem
import com.example.data.RoutineType

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExecutionTimeDialog(
    item: RoutineItem,
    onDismiss: () -> Unit,
    onConfirm: (ExecutionStatus, Int?) -> Unit
) {
    var selectedStatus by remember { mutableStateOf(item.executionStatus) }
    val plannedMins = item.plannedMinutes

    // Initialize actual minutes done
    var actualMinutes by remember {
        mutableIntStateOf(
            item.actualMinutesSpent ?: if (item.executionStatus == ExecutionStatus.COMPLETED) plannedMins else plannedMins
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Registra Risultato",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${item.dayEnum.fullName} • ${item.title}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                // Status selection pills: FATTO (Verde), NON FATTO (Rosso), IN ATTESA (Grigio)
                Text(
                    text = "Stato dell'attività:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // FATTO
                    val isFatto = selectedStatus == ExecutionStatus.COMPLETED
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                selectedStatus = ExecutionStatus.COMPLETED
                                if (actualMinutes <= 0) actualMinutes = plannedMins
                            }
                            .then(
                                if (isFatto) Modifier.border(2.dp, Color(0xFF10B981), RoundedCornerShape(12.dp))
                                else Modifier
                            )
                            .testTag("dialog_status_completed"),
                        color = if (isFatto) Color(0xFF10B981).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = if (isFatto) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "FATTO",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isFatto) Color(0xFF047857) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // NON FATTO (X)
                    val isNonFatto = selectedStatus == ExecutionStatus.MISSED
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedStatus = ExecutionStatus.MISSED }
                            .then(
                                if (isNonFatto) Modifier.border(2.dp, Color(0xFFEF4444), RoundedCornerShape(12.dp))
                                else Modifier
                            )
                            .testTag("dialog_status_missed"),
                        color = if (isNonFatto) Color(0xFFEF4444).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = if (isNonFatto) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "NON FATTO",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isNonFatto) Color(0xFFB91C1C) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // NON ASSEGNATO
                    val isPending = selectedStatus == ExecutionStatus.PENDING
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedStatus = ExecutionStatus.PENDING }
                            .then(
                                if (isPending) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                                else Modifier
                            )
                            .testTag("dialog_status_pending"),
                        color = if (isPending) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.HourglassEmpty,
                                contentDescription = null,
                                tint = if (isPending) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "IN ATTESA",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPending) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Time tracking section (especially for Events)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Tempo pianificato:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                text = RoutineItem.formatMinutes(plannedMins),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (selectedStatus == ExecutionStatus.COMPLETED) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Quanto tempo hai dedicato effettivamente?",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            // Quick preset chips
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    "Tutto (${plannedMins}m)" to plannedMins,
                                    "Metà (${plannedMins / 2}m)" to (plannedMins / 2),
                                    "15m" to 15,
                                    "30m" to 30,
                                    "45m" to 45,
                                    "60m" to 60,
                                    "90m" to 90
                                ).filter { it.second in 1..plannedMins || it.second == plannedMins }.distinctBy { it.second }.forEach { preset ->
                                    val isSelected = actualMinutes == preset.second
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                        modifier = Modifier.clickable { actualMinutes = preset.second }
                                    ) {
                                        Text(
                                            text = preset.first,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Stepper controls
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = { actualMinutes = (actualMinutes - 15).coerceAtLeast(0) },
                                    modifier = Modifier.size(36.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                ) { Text("-15", fontSize = 10.sp) }

                                Spacer(modifier = Modifier.width(6.dp))

                                OutlinedButton(
                                    onClick = { actualMinutes = (actualMinutes - 5).coerceAtLeast(0) },
                                    modifier = Modifier.size(36.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                ) { Text("-5", fontSize = 10.sp) }

                                Text(
                                    text = RoutineItem.formatMinutes(actualMinutes),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 14.dp),
                                    color = Color(0xFF10B981)
                                )

                                OutlinedButton(
                                    onClick = { actualMinutes = (actualMinutes + 5).coerceAtMost(plannedMins * 2) },
                                    modifier = Modifier.size(36.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                ) { Text("+5", fontSize = 10.sp) }

                                Spacer(modifier = Modifier.width(6.dp))

                                OutlinedButton(
                                    onClick = { actualMinutes = (actualMinutes + 15).coerceAtMost(plannedMins * 2) },
                                    modifier = Modifier.size(36.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                ) { Text("+15", fontSize = 10.sp) }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Live feedback summary
                            val notDone = (plannedMins - actualMinutes).coerceAtLeast(0)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "🟢 Fatto: ${RoutineItem.formatMinutes(actualMinutes)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF047857)
                                )
                                if (notDone > 0) {
                                    Text(
                                        text = "🔴 Non fatto: ${RoutineItem.formatMinutes(notDone)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFDC2626)
                                    )
                                }
                            }
                        } else if (selectedStatus == ExecutionStatus.MISSED) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "🔴 Questo evento sarà conteggiato nelle statistiche come NON FATTO.",
                                fontSize = 12.sp,
                                color = Color(0xFFDC2626),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Tempo perso / non svolto: ${RoutineItem.formatMinutes(plannedMins)}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "⏳ L'evento è ancora in attesa di essere svolto durante la settimana.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalMinutes = when (selectedStatus) {
                        ExecutionStatus.COMPLETED -> actualMinutes
                        ExecutionStatus.MISSED -> 0
                        ExecutionStatus.PENDING -> null
                    }
                    onConfirm(selectedStatus, finalMinutes)
                },
                modifier = Modifier.testTag("dialog_confirm_button")
            ) {
                Text("Salva")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla")
            }
        }
    )
}
