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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DayOfWeek
import com.example.data.RoutineCategory
import com.example.data.RoutineItem
import com.example.data.RoutinePriority
import com.example.data.RoutineType

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditRoutineBottomSheet(
    initialItem: RoutineItem?,
    onDismiss: () -> Unit,
    onSave: (RoutineItem, Set<Int>, Boolean) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val isNew = initialItem?.id == 0L || initialItem?.id == null
    var routineType by remember { mutableStateOf(initialItem?.type ?: RoutineType.EVENT) }
    var title by remember { mutableStateOf(initialItem?.title ?: "") }
    var notes by remember { mutableStateOf(initialItem?.notes ?: "") }
    var selectedCategory by remember { mutableStateOf(initialItem?.category ?: RoutineCategory.WORK) }
    var selectedPriority by remember { mutableStateOf(initialItem?.priority ?: RoutinePriority.MEDIUM) }
    var selectedColorHex by remember { mutableStateOf(initialItem?.colorHex ?: "#4F46E5") }

    // Multi-day selection (for new items)
    var selectedDays by remember {
        mutableStateOf(
            if (initialItem != null) setOf(initialItem.dayOfWeek)
            else setOf(1)
        )
    }

    // Time fields
    var startHour by remember { mutableIntStateOf(initialItem?.startHour ?: 9) }
    var startMinute by remember { mutableIntStateOf(initialItem?.startMinute ?: 0) }
    var endHour by remember { mutableIntStateOf(initialItem?.endHour ?: 10) }
    var endMinute by remember { mutableIntStateOf(initialItem?.endMinute ?: 0) }
    var hasSpecificTime by remember {
        mutableStateOf(initialItem?.startHour != null || routineType == RoutineType.EVENT)
    }

    val availableColors = listOf(
        "#3B82F6", "#4F46E5", "#8B5CF6", "#EC4899",
        "#10B981", "#06B6D4", "#F59E0B", "#EF4444"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = if (isNew) "Aggiungi alla Routine" else "Modifica Elemento",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Type selector (Evento vs Promemoria)
            TabRow(
                selectedTabIndex = routineType.ordinal,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = routineType == RoutineType.EVENT,
                    onClick = {
                        routineType = RoutineType.EVENT
                        hasSpecificTime = true
                    },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Evento a orario")
                        }
                    },
                    modifier = Modifier.testTag("tab_type_event")
                )
                Tab(
                    selected = routineType == RoutineType.REMINDER,
                    onClick = { routineType = RoutineType.REMINDER },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Promemoria / Task")
                        }
                    },
                    modifier = Modifier.testTag("tab_type_reminder")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(if (routineType == RoutineType.EVENT) "Titolo Evento (es. Studio, Allenamento)" else "Promemoria (es. Bere 2L acqua, Spesa)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_routine_title"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Days Selection (Perpetual days of the week: Lun-Dom)
            Text(
                text = if (isNew) "Giorni della Routine (Ciclici)" else "Giorno della settimana",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))

            if (isNew) {
                // Quick preset buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = { selectedDays = setOf(1, 2, 3, 4, 5) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Lun-Ven", fontSize = 12.sp)
                    }
                    TextButton(
                        onClick = { selectedDays = setOf(6, 7) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Weekend", fontSize = 12.sp)
                    }
                    TextButton(
                        onClick = { selectedDays = (1..7).toSet() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Tutti i giorni", fontSize = 12.sp)
                    }
                }
            }

            // Day pill selectors
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DayOfWeek.entries.forEach { day ->
                    val isDaySelected = selectedDays.contains(day.dayIndex)
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (isDaySelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable {
                                selectedDays = if (isNew) {
                                    if (isDaySelected) {
                                        if (selectedDays.size > 1) selectedDays - day.dayIndex else selectedDays
                                    } else {
                                        selectedDays + day.dayIndex
                                    }
                                } else {
                                    setOf(day.dayIndex)
                                }
                            }
                            .testTag("selector_day_${day.shortName.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day.shortName.take(2),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDaySelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Time selection section
            if (routineType == RoutineType.EVENT) {
                Text(
                    text = "Orari dell'evento",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TimeSelectorBox(
                        label = "Inizio",
                        hour = startHour,
                        minute = startMinute,
                        onHourChange = { startHour = it },
                        onMinuteChange = { startMinute = it },
                        modifier = Modifier.weight(1f)
                    )
                    TimeSelectorBox(
                        label = "Fine",
                        hour = endHour,
                        minute = endMinute,
                        onHourChange = { endHour = it },
                        onMinuteChange = { endMinute = it },
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                // Reminder optional time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Orario promemoria (opzionale)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(onClick = { hasSpecificTime = !hasSpecificTime }) {
                        Text(if (hasSpecificTime) "Rimuovi ora" else "Imposta ora")
                    }
                }
                if (hasSpecificTime) {
                    TimeSelectorBox(
                        label = "Ora Promemoria",
                        hour = startHour,
                        minute = startMinute,
                        onHourChange = { startHour = it },
                        onMinuteChange = { startMinute = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Priority selector
                Text(
                    text = "Priorità promemoria",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RoutinePriority.entries.forEach { p ->
                        val isPrioritySelected = selectedPriority == p
                        FilterChip(
                            selected = isPrioritySelected,
                            onClick = { selectedPriority = p },
                            label = { Text(p.label) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category picker
            Text(
                text = "Categoria",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                RoutineCategory.entries.forEach { cat ->
                    val isCatSelected = selectedCategory == cat
                    FilterChip(
                        selected = isCatSelected,
                        onClick = {
                            selectedCategory = cat
                            selectedColorHex = cat.defaultColorHex
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = cat.getIcon(),
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        label = { Text(cat.label, fontSize = 12.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Accent color selector
            Text(
                text = "Colore distintivo",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                availableColors.forEach { hex ->
                    val c = Color(android.graphics.Color.parseColor(hex))
                    val isColorSelected = selectedColorHex.equals(hex, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(c)
                            .clickable { selectedColorHex = hex }
                            .then(
                                if (isColorSelected) {
                                    Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                } else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isColorSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notes input
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Note o dettagli (opzionale)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_routine_notes"),
                shape = RoundedCornerShape(12.dp),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Save action button
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val baseItem = (initialItem ?: RoutineItem(
                            title = title,
                            type = routineType,
                            dayOfWeek = selectedDays.firstOrNull() ?: 1
                        )).copy(
                            title = title.trim(),
                            notes = notes.trim(),
                            type = routineType,
                            startHour = if (hasSpecificTime) startHour else null,
                            startMinute = if (hasSpecificTime) startMinute else null,
                            endHour = if (routineType == RoutineType.EVENT) endHour else null,
                            endMinute = if (routineType == RoutineType.EVENT) endMinute else null,
                            category = selectedCategory,
                            priority = selectedPriority,
                            colorHex = selectedColorHex
                        )
                        onSave(baseItem, selectedDays, isNew)
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("btn_save_routine_item"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = if (isNew) "Salva nella Routine" else "Salva Modifiche",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun TimeSelectorBox(
    label: String,
    hour: Int,
    minute: Int,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Hour stepper
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TextButton(
                        onClick = { onHourChange((hour + 1) % 24) },
                        modifier = Modifier.size(32.dp)
                    ) { Text("+", fontSize = 14.sp) }
                    Text(
                        text = String.format("%02d", hour),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(
                        onClick = { onHourChange(if (hour == 0) 23 else hour - 1) },
                        modifier = Modifier.size(32.dp)
                    ) { Text("-", fontSize = 14.sp) }
                }

                Text(":", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))

                // Minute stepper
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TextButton(
                        onClick = { onMinuteChange((minute + 15) % 60) },
                        modifier = Modifier.size(32.dp)
                    ) { Text("+", fontSize = 14.sp) }
                    Text(
                        text = String.format("%02d", minute),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(
                        onClick = { onMinuteChange(if (minute < 15) 45 else minute - 15) },
                        modifier = Modifier.size(32.dp)
                    ) { Text("-", fontSize = 14.sp) }
                }
            }
        }
    }
}
