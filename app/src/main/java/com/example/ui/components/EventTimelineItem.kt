package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ExecutionStatus
import com.example.data.RoutineItem

@Composable
fun EventTimelineItem(
    item: RoutineItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onNotifyTest: () -> Unit,
    onOpenExecutionDialog: () -> Unit,
    onQuickSetStatus: (ExecutionStatus, Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = try {
        Color(android.graphics.Color.parseColor(item.colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    val status = item.executionStatus
    val isFatto = status == ExecutionStatus.COMPLETED
    val isNonFatto = status == ExecutionStatus.MISSED
    val isPending = status == ExecutionStatus.PENDING

    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("event_item_${item.id}"),
        verticalAlignment = Alignment.Top
    ) {
        // Timeline Time Indicator on the left
        Column(
            modifier = Modifier
                .width(58.dp)
                .padding(top = 4.dp),
            horizontalAlignment = Alignment.End
        ) {
            val startText = if (item.startHour != null && item.startMinute != null) {
                String.format("%02d:%02d", item.startHour, item.startMinute)
            } else "--:--"
            Text(
                text = startText,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )
            if (item.endHour != null && item.endMinute != null) {
                val endText = String.format("%02d:%02d", item.endHour, item.endMinute)
                Text(
                    text = endText,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Vertical timeline bar & dot
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isFatto -> Color(0xFF10B981)
                            isNonFatto -> Color(0xFFEF4444)
                            else -> accentColor
                        }
                    )
            )
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(70.dp)
                    .background(accentColor.copy(alpha = 0.25f))
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Event Card
        Card(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 12.dp)
                .clickable { onEdit() },
            shape = RoundedCornerShape(16.dp),
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
                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                }
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category chip
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = accentColor.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = item.category.getIcon(),
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = item.category.label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = accentColor
                            )
                        }
                    }

                    // Status Pill (Clickable to open execution/time dialog)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = when {
                            isFatto -> Color(0xFF10B981)
                            isNonFatto -> Color(0xFFEF4444)
                            else -> MaterialTheme.colorScheme.surface
                        },
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            when {
                                isFatto -> Color(0xFF10B981)
                                isNonFatto -> Color(0xFFEF4444)
                                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            }
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onOpenExecutionDialog() }
                            .testTag("status_pill_${item.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
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
                                    isFatto -> "Fatto • ${item.formattedDoneDuration}"
                                    isNonFatto -> "Non fatto (X)"
                                    else -> "Non assegnato"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPending) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (isNonFatto) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (item.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.notes,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Time info & Quick Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.formattedTime.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = item.formattedTime,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    // Action buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Quick FATTO button
                        IconButton(
                            onClick = { onQuickSetStatus(ExecutionStatus.COMPLETED, item.plannedMinutes) },
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (isFatto) Color(0xFF10B981).copy(alpha = 0.2f) else Color.Transparent)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Segna come Fatto",
                                tint = if (isFatto) Color(0xFF047857) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Quick NON FATTO (X) button
                        IconButton(
                            onClick = { onQuickSetStatus(ExecutionStatus.MISSED, 0) },
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (isNonFatto) Color(0xFFEF4444).copy(alpha = 0.2f) else Color.Transparent)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Segna come Non Fatto",
                                tint = if (isNonFatto) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Time setting dialog
                        IconButton(
                            onClick = onOpenExecutionDialog,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Imposta tempo",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Edit
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Modifica",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Delete
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Elimina",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
