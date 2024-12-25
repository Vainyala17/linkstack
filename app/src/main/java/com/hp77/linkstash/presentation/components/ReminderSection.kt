package com.hp77.linkstash.presentation.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.hp77.linkstash.util.DateUtils
import java.util.*

@Composable
fun ReminderSection(
    reminderTime: Long?,
    onSetReminder: (Long) -> Unit,
    onRemoveReminder: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "Reminder",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (reminderTime == null) {
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Alarm,
                    contentDescription = "Set Reminder",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Set Reminder")
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Reminder set for: ${DateUtils.formatDateTime(reminderTime)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                IconButton(onClick = onRemoveReminder) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove Reminder"
                    )
                }
            }
        }

        if (showDatePicker) {
            val calendar = Calendar.getInstance()
            
            // Show Date Picker
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    // After date is selected, show Time Picker
                    TimePickerDialog(
                        context,
                        { _, hourOfDay, minute ->
                            calendar.set(year, month, dayOfMonth, hourOfDay, minute)
                            onSetReminder(calendar.timeInMillis)
                            showDatePicker = false
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        false
                    ).show()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
            
            showDatePicker = false
        }
    }
}
