package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.database.CalendarEntry
import com.example.ui.theme.GoldPulse
import com.example.ui.theme.SoftTeal
import com.example.ui.viewmodel.CalendarViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun CalendarGridView(
    currentMonth: Calendar,
    selectedDate: String,
    allEntries: List<CalendarEntry>,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Weekday headers
    val daysOfWeek = listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")

    val monthClone = currentMonth.clone() as Calendar
    monthClone.set(Calendar.DAY_OF_MONTH, 1)
    
    val firstDayOfWeek = monthClone.get(Calendar.DAY_OF_WEEK) // 1 = Sunday, 2 = Monday
    val offset = firstDayOfWeek - 1 // Sunday-based offset

    val maxDays = monthClone.getActualMaximum(Calendar.DAY_OF_MONTH)
    
    // Previous month details for filling blank start cells
    val prevMonthClone = monthClone.clone() as Calendar
    prevMonthClone.add(Calendar.MONTH, -1)
    val maxDaysPrev = prevMonthClone.getActualMaximum(Calendar.DAY_OF_MONTH)

    val currentYearFormat = SimpleDateFormat("yyyy", Locale.getDefault()).format(currentMonth.time)
    val currentMonthValFormat = SimpleDateFormat("MM", Locale.getDefault()).format(currentMonth.time)

    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    Column(modifier = modifier.fillMaxWidth()) {
        // Weekday row
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
                    modifier = Modifier.width(36.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Days Grid (6 rows * 7 cells = 42 cells total)
        val totalCells = 42
        val rows = (totalCells + 6) / 7

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth().height(240.dp),
            userScrollEnabled = false,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(totalCells) { index ->
                var dayNum = 0
                var dateStr = ""
                var isCurrentMonth = true

                if (index < offset) {
                    dayNum = maxDaysPrev - offset + index + 1
                    isCurrentMonth = false
                    
                    val tempCal = prevMonthClone.clone() as Calendar
                    tempCal.set(Calendar.DAY_OF_MONTH, dayNum)
                    dateStr = formatter.format(tempCal.time)
                } else if (index >= offset + maxDays) {
                    dayNum = index - offset - maxDays + 1
                    isCurrentMonth = false
                    
                    val nextMonthClone = monthClone.clone() as Calendar
                    nextMonthClone.add(Calendar.MONTH, 1)
                    nextMonthClone.set(Calendar.DAY_OF_MONTH, dayNum)
                    dateStr = formatter.format(nextMonthClone.time)
                } else {
                    dayNum = index - offset + 1
                    dateStr = "$currentYearFormat-$currentMonthValFormat-${String.format("%02d", dayNum)}"
                }

                val isSelected = dateStr == selectedDate
                val hasAlerts = allEntries.any { it.date == dateStr }

                CalendarCell(
                    dayNumber = dayNum,
                    isCurrentMonth = isCurrentMonth,
                    isSelected = isSelected,
                    hasAlerts = hasAlerts,
                    onClick = { onDateSelected(dateStr) },
                    modifier = Modifier.testTag("calendar_cell_$dateStr")
                )
            }
        }
    }
}

@Composable
fun CalendarCell(
    dayNumber: Int,
    isCurrentMonth: Boolean,
    isSelected: Boolean,
    hasAlerts: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    hasAlerts -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    else -> Color.Transparent
                }
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = dayNumber.toString(),
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    isCurrentMonth -> MaterialTheme.colorScheme.onBackground
                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                }
            )
            if (hasAlerts) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else GoldPulse)
                )
            }
        }
    }
}

@Composable
fun EntryItem(
    entry: CalendarEntry,
    onToggleComplete: (Boolean) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val completedAlpha = if (entry.isCompleted) 0.6f else 1.0f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (entry.isCompleted) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Task status indicator
            IconButton(
                onClick = { onToggleComplete(!entry.isCompleted) },
                modifier = Modifier.size(36.dp).testTag("complete_checkbox_${entry.id}")
            ) {
                Icon(
                    imageVector = if (entry.isCompleted) Icons.Default.CheckCircle else Icons.Outlined.CheckCircle,
                    contentDescription = "Toggle Complete Status",
                    tint = if (entry.isCompleted) SoftTeal else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Text Info
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = entry.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (entry.isCompleted) {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        textDecoration = if (entry.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (entry.priority == "HIGH" && !entry.isCompleted) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "HIGH",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                if (entry.description.isNotEmpty()) {
                    Text(
                        text = entry.description,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notification Configuration",
                        tint = if (entry.isCompleted) {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        } else {
                            GoldPulse
                        },
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${entry.time} " + when {
                            entry.recurrence != "NONE" -> "• 🔁 ${entry.recurrence.lowercase()}"
                            else -> ""
                        } + " • 🔔 " + when (entry.notifyTimeOffsetMinutes) {
                            0 -> "at event"
                            5 -> "5m prior"
                            15 -> "15m prior"
                            60 -> "1h prior"
                            else -> "${entry.notifyTimeOffsetMinutes}m prior"
                        },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Delete action
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp).testTag("delete_button_${entry.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Reminder",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditEntryDialog(
    selectedDate: String,
    onDismiss: () -> Unit,
    onSave: (CalendarEntry) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    // Default current hour details for timing inputs
    val currentHour = String.format("%02d", Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
    val currentMinute = String.format("%02d", (Calendar.getInstance().get(Calendar.MINUTE) / 5) * 5)
    var timeInput by remember { mutableStateOf("$currentHour:$currentMinute") }
    
    var recurrence by remember { mutableStateOf("NONE") } // "NONE", "DAILY", "WEEKLY", "MONTHLY"
    var notifyOffset by remember { mutableStateOf(0) } // 0, 5, 15, 60
    var soundEnabled by remember { mutableStateOf(true) }
    var vibrateEnabled by remember { mutableStateOf(true) }
    var priority by remember { mutableStateOf("DEFAULT") } // "HIGH", "DEFAULT", "LOW"

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "New Calendar Reminder",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )

                // Date Label
                Text(
                    text = "Date: $selectedDate",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_title_field"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                // Description Input
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                // Time Input & Recurrence (Row)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = timeInput,
                        onValueChange = { timeInput = it },
                        label = { Text("Time (HH:MM)") },
                        placeholder = { Text("12:00") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    // Priority Choices
                    Column(modifier = Modifier.weight(1.2f)) {
                        Text("Priority", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf("LOW", "DEFAULT", "HIGH").forEach { p ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (priority == p) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { priority = p }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = p,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (priority == p) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Recurrence Chooser (Segmented Row)
                Column {
                    Text("Recurrence", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("NONE", "DAILY", "WEEKLY", "MONTHLY").forEach { recurType ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (recurrence == recurType) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { recurrence = recurType }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = recurType,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (recurrence == recurType) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Notification Timing Offset Dropdown
                var expandedOffsetDropdown by remember { mutableStateOf(false) }
                val offsetOptions = mapOf(
                    0 to "At time of event",
                    5 to "5 minutes prior",
                    15 to "15 minutes prior",
                    60 to "1 hour prior"
                )

                ExposedDropdownMenuBox(
                    expanded = expandedOffsetDropdown,
                    onExpandedChange = { expandedOffsetDropdown = !expandedOffsetDropdown }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = offsetOptions[notifyOffset] ?: "At time of event",
                        onValueChange = {},
                        label = { Text("Notification Reminder Lead") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedOffsetDropdown) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedOffsetDropdown,
                        onDismissRequest = { expandedOffsetDropdown = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        offsetOptions.forEach { (offsetVal, labelStr) ->
                            DropdownMenuItem(
                                text = { Text(labelStr, color = MaterialTheme.colorScheme.onSurface) },
                                onClick = {
                                    notifyOffset = offsetVal
                                    expandedOffsetDropdown = false
                                }
                            )
                        }
                    }
                }

                // Preferences switches (Sound / Vibration in a clean row)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = soundEnabled,
                            onCheckedChange = { soundEnabled = it }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sound", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = vibrateEnabled,
                            onCheckedChange = { vibrateEnabled = it }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Vibration", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                // Validate time input format HH:MM
                                val timeToSave = if (timeInput.matches(Regex("\\d{2}:\\d{2}"))) {
                                    timeInput
                                } else {
                                    "12:00"
                                }

                                val entry = CalendarEntry(
                                    title = title.trim(),
                                    description = description.trim(),
                                    date = selectedDate,
                                    time = timeToSave,
                                    recurrence = recurrence,
                                    notifyTimeOffsetMinutes = notifyOffset,
                                    soundEnabled = soundEnabled,
                                    vibrateEnabled = vibrateEnabled,
                                    priority = priority
                                )
                                onSave(entry)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        enabled = title.isNotBlank(),
                        modifier = Modifier.testTag("save_reminder_button")
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SyncDialog(
    viewModel: CalendarViewModel,
    onDismiss: () -> Unit
) {
    val syncCodeVal by viewModel.syncCode.collectAsState()
    val isSyncingVal by viewModel.isSyncing.collectAsState()
    val syncMessageVal by viewModel.syncMessage.collectAsState()

    var inputCode by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Cloud Synchronization",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Sync your calendar across your phone, tablet or emulator. Copy your sync code onto another device to instantly restore all entries.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Current Device Sync Code View
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Your Device Sync Code:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (syncCodeVal.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = syncCodeVal,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            TextButton(
                                onClick = { clipboardManager.setText(AnnotatedString(syncCodeVal)) }
                            ) {
                                Text("COPY", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Text(
                            text = "Not backed up yet. Perform your first sync below!",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }

                // Action 1: Upload / Push current data
                Button(
                    onClick = { viewModel.pushSync() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("cloud_backup_button"),
                    enabled = !isSyncingVal
                ) {
                    Text(
                        text = if (syncCodeVal.isNotBlank()) "Back Up Database" else "Generate Code & Sync",
                        fontWeight = FontWeight.Bold
                    )
                }

                // Separator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
                    Text(
                        text = "OR RESTORE",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp),
                        fontWeight = FontWeight.Bold
                    )
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
                }

                // Action 2: Input code & Restore
                OutlinedTextField(
                    value = inputCode,
                    onValueChange = { inputCode = it },
                    label = { Text("Enter Sync Code") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("sync_input_field"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                Button(
                    onClick = { viewModel.pullSync(inputCode) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("cloud_restore_button"),
                    enabled = !isSyncingVal && inputCode.trim().isNotBlank()
                ) {
                    Text(text = "Restore from Sync Code", fontWeight = FontWeight.Bold)
                }

                // Display Status messages clearly to the user
                AnimatedVisibility(
                    visible = syncMessageVal != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    syncMessageVal?.let { message ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = message,
                                    fontSize = 12.sp,
                                    color = if (message.startsWith("Success")) SoftTeal else GoldPulse,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                TextButton(
                                    onClick = { viewModel.clearSyncMessage() },
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) {
                                    Text("Dismiss", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss, enabled = !isSyncingVal) {
                        Text("Close", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
