package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.data.DayOfWeek
import com.example.data.ExecutionStatus
import com.example.data.RoutineItem
import com.example.data.RoutineType
import java.time.LocalDate

/**
 * A Jetpack Compose UI component that displays a 7-column layout representing the days of the week,
 * allowing users to view and add events to specific days.
 */
@Composable
fun SevenColumnWeekView(
    allItems: List<RoutineItem>,
    onAddEventForDay: (DayOfWeek) -> Unit,
    onEditItem: (RoutineItem) -> Unit,
    onDeleteItem: (RoutineItem) -> Unit,
    onToggleReminder: (RoutineItem) -> Unit,
    onDayHeaderClick: (DayOfWeek) -> Unit,
    onTestNotify: (RoutineItem) -> Unit = {},
    onSetItemStatus: ((RoutineItem, ExecutionStatus, Int?) -> Unit)? = null,
    onOpenExecutionDialog: ((RoutineItem) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val todayIndex = try {
        LocalDate.now().dayOfWeek.value
    } catch (e: Exception) {
        1
    }

    val horizontalScrollState = rememberScrollState()

    // Auto-scroll near today's column on initial composition if it's midweek or weekend
    LaunchedEffect(Unit) {
        if (todayIndex > 2) {
            val approximateScrollOffset = ((todayIndex - 2) * 210 * 2.5f).toInt()
            horizontalScrollState.animateScrollTo(approximateScrollOffset)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Top summary row with navigation hints
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Tabellone Settimanale (7 Colonne)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Segna Fatto (✓) o Non Fatto (✗) e imposta la durata svolta per ciascun evento",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 7-Column Horizontal Container
        Row(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(horizontalScrollState)
                .testTag("seven_column_week_container"),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DayOfWeek.entries.forEach { day ->
                val isToday = day.dayIndex == todayIndex
                val dayItems = allItems.filter { it.dayOfWeek == day.dayIndex }
                val events = dayItems.filter { it.type == RoutineType.EVENT }.sortedBy { it.sortMinutes }
                val reminders = dayItems.filter { it.type == RoutineType.REMINDER }

                DayColumnComponent(
                    day = day,
                    isToday = isToday,
                    events = events,
                    reminders = reminders,
                    onAddEvent = { onAddEventForDay(day) },
                    onEditItem = onEditItem,
                    onDeleteItem = onDeleteItem,
                    onToggleReminder = onToggleReminder,
                    onHeaderClick = { onDayHeaderClick(day) },
                    onTestNotify = onTestNotify,
                    onSetItemStatus = onSetItemStatus,
                    onOpenExecutionDialog = onOpenExecutionDialog,
                    modifier = Modifier
                        .width(235.dp)
                        .fillMaxHeight()
                        .testTag("day_column_${day.shortName.lowercase()}")
                )
            }
        }
    }
}

@Composable
private fun DayColumnComponent(
    day: DayOfWeek,
    isToday: Boolean,
    events: List<RoutineItem>,
    reminders: List<RoutineItem>,
    onAddEvent: () -> Unit,
    onEditItem: (RoutineItem) -> Unit,
    onDeleteItem: (RoutineItem) -> Unit,
    onToggleReminder: (RoutineItem) -> Unit,
    onHeaderClick: () -> Unit,
    onTestNotify: (RoutineItem) -> Unit,
    onSetItemStatus: ((RoutineItem, ExecutionStatus, Int?) -> Unit)?,
    onOpenExecutionDialog: ((RoutineItem) -> Unit)?,
    modifier: Modifier = Modifier
) {
    val totalCount = events.size + reminders.size
    val columnBorderColor = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)

    Card(
        modifier = modifier
            .border(
                width = if (isToday) 2.dp else 1.dp,
                color = columnBorderColor,
                shape = RoundedCornerShape(18.dp)
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isToday) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isToday) 3.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            // Column Header
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onHeaderClick() }
                    .testTag("day_header_${day.shortName.lowercase()}"),
                color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = day.fullName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (isToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                            if (isToday) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                ) {
                                    Text(
                                        text = "OGGI",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = "$totalCount attività (${events.size} ev • ${reminders.size} task)",
                            fontSize = 11.sp,
                            color = if (isToday) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Direct Add Button on Header
                    FilledTonalButton(
                        onClick = onAddEvent,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("column_add_btn_${day.shortName.lowercase()}"),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (isToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                            contentColor = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Aggiungi evento a ${day.fullName}",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Scrollable column body with events and tasks
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Section: Events
                if (events.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "EVENTI (${events.size})",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    events.forEach { event ->
                        ColumnEventCard(
                            item = event,
                            onEdit = { onEditItem(event) },
                            onDelete = { onDeleteItem(event) },
                            onNotify = { onTestNotify(event) },
                            onSetStatus = { status, mins -> onSetItemStatus?.invoke(event, status, mins) },
                            onOpenTimeDialog = { onOpenExecutionDialog?.invoke(event) }
                        )
                    }
                }

                // Section: Reminders
                if (reminders.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "PROMEMORIA (${reminders.size})",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    reminders.forEach { reminder ->
                        ColumnReminderCard(
                            item = reminder,
                            onToggle = { onToggleReminder(reminder) },
                            onEdit = { onEditItem(reminder) },
                            onDelete = { onDeleteItem(reminder) },
                            onSetStatus = { status -> onSetItemStatus?.invoke(reminder, status, if (status == ExecutionStatus.COMPLETED) reminder.plannedMinutes else 0) }
                        )
                    }
                }

                // Empty state card
                if (events.isEmpty() && reminders.isEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Nessun evento",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tocca '+' o il pulsante qui sotto per pianificare",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Bottom Add Card
                OutlinedButton(
                    onClick = onAddEvent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .testTag("column_bottom_add_${day.shortName.lowercase()}"),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "+ Evento / Task",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun ColumnEventCard(
    item: RoutineItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onNotify: () -> Unit,
    onSetStatus: (ExecutionStatus, Int?) -> Unit,
    onOpenTimeDialog: () -> Unit
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onEdit() }
            .testTag("column_event_card_${item.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isFatto -> Color(0xFF10B981).copy(alpha = 0.08f)
                isNonFatto -> Color(0xFFEF4444).copy(alpha = 0.08f)
                else -> MaterialTheme.colorScheme.surface
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
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // Category & Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = accentColor.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(accentColor)
                        )
                        Text(
                            text = item.category.label,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }
                }

                // Clickable status pill to open execution dialog
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when {
                        isFatto -> Color(0xFF10B981)
                        isNonFatto -> Color(0xFFEF4444)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    modifier = Modifier.clickable { onOpenTimeDialog() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = when {
                                isFatto -> Icons.Default.Check
                                isNonFatto -> Icons.Default.Close
                                else -> Icons.Default.HourglassEmpty
                            },
                            contentDescription = null,
                            tint = if (isPending) MaterialTheme.colorScheme.onSurfaceVariant else Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = when {
                                isFatto -> "FATTO • ${item.formattedDoneDuration}"
                                isNonFatto -> "NON FATTO (X)"
                                else -> "DA FARE"
                            },
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPending) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textDecoration = if (isNonFatto) TextDecoration.LineThrough else TextDecoration.None,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (item.formattedTime.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = item.formattedTime,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (item.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = item.notes,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Action row: Quick Fatto (Check), Quick Non Fatto (Cross), Timer, Edit, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    // Quick FATTO (Check)
                    IconButton(
                        onClick = { onSetStatus(ExecutionStatus.COMPLETED, item.plannedMinutes) },
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (isFatto) Color(0xFF10B981).copy(alpha = 0.2f) else Color.Transparent)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Segna Fatto",
                            tint = if (isFatto) Color(0xFF047857) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    // Quick NON FATTO (Cross X)
                    IconButton(
                        onClick = { onSetStatus(ExecutionStatus.MISSED, 0) },
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (isNonFatto) Color(0xFFEF4444).copy(alpha = 0.2f) else Color.Transparent)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Segna Non Fatto",
                            tint = if (isNonFatto) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    // Imposta tempo
                    IconButton(
                        onClick = onOpenTimeDialog,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Imposta tempo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Modifica",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Elimina",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ColumnReminderCard(
    item: RoutineItem,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSetStatus: (ExecutionStatus) -> Unit
) {
    val status = item.executionStatus
    val isFatto = status == ExecutionStatus.COMPLETED || item.isCompleted
    val isNonFatto = status == ExecutionStatus.MISSED

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onToggle() }
            .testTag("column_reminder_card_${item.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isFatto -> Color(0xFF10B981).copy(alpha = 0.08f)
                isNonFatto -> Color(0xFFEF4444).copy(alpha = 0.08f)
                else -> MaterialTheme.colorScheme.surface
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
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onToggle,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = if (isFatto) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Spunta Fatto",
                    tint = if (isFatto) Color(0xFF10B981) else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(3.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontSize = 11.sp,
                    fontWeight = if (isFatto) FontWeight.Normal else FontWeight.Medium,
                    color = when {
                        isFatto -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        isNonFatto -> Color(0xFFDC2626)
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                    textDecoration = if (isFatto || isNonFatto) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (isNonFatto) {
                    Text(
                        text = "NON FATTO ❌",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFDC2626)
                    )
                }
            }

            // Quick cross X button to mark as Non Fatto
            IconButton(
                onClick = {
                    if (isNonFatto) onSetStatus(ExecutionStatus.PENDING)
                    else onSetStatus(ExecutionStatus.MISSED)
                },
                modifier = Modifier.size(22.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Segna non fatto",
                    tint = if (isNonFatto) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(13.dp)
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(22.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Elimina",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}
