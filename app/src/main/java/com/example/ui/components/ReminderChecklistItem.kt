package com.example.ui.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.RoutinePriority

@Composable
fun ReminderChecklistItem(
    item: RoutineItem,
    onToggleCompleted: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSetStatus: ((ExecutionStatus) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val accentColor = try {
        Color(android.graphics.Color.parseColor(item.colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.secondary
    }

    val status = item.executionStatus
    val isFatto = status == ExecutionStatus.COMPLETED || item.isCompleted
    val isNonFatto = status == ExecutionStatus.MISSED

    val checkBgColor by animateColorAsState(
        targetValue = if (isFatto) Color(0xFF10B981)
        else if (isNonFatto) Color(0xFFEF4444)
        else Color.Transparent,
        label = "check_bg_anim"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onToggleCompleted() }
            .testTag("reminder_item_${item.id}"),
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
                isFatto -> Color(0xFF10B981).copy(alpha = 0.3f)
                isNonFatto -> Color(0xFFEF4444).copy(alpha = 0.3f)
                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isFatto) 0.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Interactive Checkbox / Cross Indicator
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(checkBgColor)
                    .border(
                        width = 2.dp,
                        color = when {
                            isFatto -> Color(0xFF10B981)
                            isNonFatto -> Color(0xFFEF4444)
                            else -> MaterialTheme.colorScheme.outline
                        },
                        shape = CircleShape
                    )
                    .clickable { onToggleCompleted() }
                    .testTag("checkbox_${item.id}"),
                contentAlignment = Alignment.Center
            ) {
                if (isFatto) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Fatto",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                } else if (isNonFatto) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Non fatto",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.title,
                    fontWeight = if (isFatto) FontWeight.Normal else FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = when {
                        isFatto -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        isNonFatto -> Color(0xFFDC2626)
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                    textDecoration = if (isFatto || isNonFatto) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (item.notes.isNotBlank()) {
                    Text(
                        text = item.notes,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = if (isFatto) 0.5f else 0.8f
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when {
                            isFatto -> Color(0xFF10B981).copy(alpha = 0.15f)
                            isNonFatto -> Color(0xFFEF4444).copy(alpha = 0.15f)
                            else -> MaterialTheme.colorScheme.surface
                        }
                    ) {
                        Text(
                            text = when {
                                isFatto -> "FATTO ✅"
                                isNonFatto -> "NON FATTO ❌"
                                else -> "DA FARE"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                isFatto -> Color(0xFF047857)
                                isNonFatto -> Color(0xFFDC2626)
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Priority Chip
                    val priorityColor = when (item.priority) {
                        RoutinePriority.HIGH -> Color(0xFFEF4444)
                        RoutinePriority.MEDIUM -> Color(0xFFF59E0B)
                        RoutinePriority.LOW -> Color(0xFF10B981)
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = priorityColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = item.priority.label,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = priorityColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Category Tag
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
            }

            // Quick actions
            Row(verticalAlignment = Alignment.CenterVertically) {
                // If onSetStatus is available, show cross X button for non fatto
                if (onSetStatus != null) {
                    IconButton(
                        onClick = {
                            if (isNonFatto) onSetStatus(ExecutionStatus.PENDING)
                            else onSetStatus(ExecutionStatus.MISSED)
                        },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Segna non fatto",
                            tint = if (isNonFatto) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .size(30.dp)
                        .testTag("edit_reminder_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Modifica",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(30.dp)
                        .testTag("delete_reminder_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Elimina",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}
