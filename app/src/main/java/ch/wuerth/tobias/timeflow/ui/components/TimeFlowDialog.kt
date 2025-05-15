package ch.wuerth.tobias.timeflow.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ch.wuerth.tobias.timeflow.data.TimeFlowItem
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeFlowDialog(
    timeFlowItem: TimeFlowItem? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, Instant, Instant) -> Unit
) {
    var title by remember { mutableStateOf(timeFlowItem?.title ?: "") }
    var fromDateTime by remember {
        mutableStateOf(
            timeFlowItem?.fromDateTime ?: Clock.System.now()
        )
    }

    var toDateTime by remember {
        mutableStateOf(
            timeFlowItem?.toDateTime ?: Clock.System.now().plus(kotlin.time.Duration.parse("24h"))
        )
    }

    // Keep track of focused field to prevent auto-focusing title after date/time selection
    var focusedField by remember { mutableStateOf<String?>(null) }

    var showFromDatePicker by remember { mutableStateOf(false) }
    var showFromTimePicker by remember { mutableStateOf(false) }
    var showToDatePicker by remember { mutableStateOf(false) }
    var showToTimePicker by remember { mutableStateOf(false) }

    val fromLocal = fromDateTime.toLocalDateTime(TimeZone.currentSystemDefault())
    val toLocal = toDateTime.toLocalDateTime(TimeZone.currentSystemDefault())

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = if (timeFlowItem == null) "Add TimeFlow" else "Edit TimeFlow",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) {
                                focusedField = "title"
                            }
                        },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))
                // From Date & Time
                Text(text = "From Date & Time")

                Button(
                    onClick = { showFromDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${fromLocal.date} ${
                            fromLocal.time.hour.toString().padStart(2, '0')
                        }:${fromLocal.time.minute.toString().padStart(2, '0')}",
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // To Date & Time
                Text(text = "To Date & Time")

                Button(
                    onClick = { showToDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${toLocal.date} ${
                            toLocal.time.hour.toString().padStart(2, '0')
                        }:${toLocal.time.minute.toString().padStart(2, '0')}",
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    TextButton(
                        onClick = {
                            if (title.isNotBlank() && fromDateTime < toDateTime) {
                                onConfirm(title, fromDateTime, toDateTime)
                                onDismiss()
                            }
                        }
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }

    // Date & Time Pickers
    if (showFromDatePicker) {
        val fromLocalDate = fromLocal.date
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = fromLocalDate.toEpochMilliseconds()
        )

        DatePickerDialog(
            onDismissRequest = { showFromDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            val newDate = Instant.fromEpochMilliseconds(it)
                                .toLocalDateTime(TimeZone.currentSystemDefault()).date
                            val newDateTime = newDate.atTime(fromLocal.time)
                                .toInstant(TimeZone.currentSystemDefault())
                            fromDateTime = newDateTime
                        }
                        showFromDatePicker = false
                        showFromTimePicker = true
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFromDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    if (showFromTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = fromLocal.time.hour,
            initialMinute = fromLocal.time.minute
        )

        Dialog(onDismissRequest = { showFromTimePicker = false }) {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TimePicker(
                        state = timePickerState,
                        modifier = Modifier.padding(16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showFromTimePicker = false }) {
                            Text("Cancel")
                        }

                        TextButton(
                            onClick = {
                                val newTime = fromLocal.date.atTime(
                                    hour = timePickerState.hour,
                                    minute = timePickerState.minute
                                ).toInstant(TimeZone.currentSystemDefault())
                                fromDateTime = newTime
                                showFromTimePicker = false
                                // Clear focus to prevent keyboard from showing up again
                                focusedField = null
                            }
                        ) {
                            Text("OK")
                        }
                    }
                }
            }
        }
    }

    if (showToDatePicker) {
        val toLocalDate = toLocal.date
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = toLocalDate.toEpochMilliseconds()
        )

        DatePickerDialog(
            onDismissRequest = { showToDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            val newDate = Instant.fromEpochMilliseconds(it)
                                .toLocalDateTime(TimeZone.currentSystemDefault()).date
                            val newDateTime = newDate.atTime(toLocal.time)
                                .toInstant(TimeZone.currentSystemDefault())
                            toDateTime = newDateTime
                        }
                        showToDatePicker = false
                        showToTimePicker = true
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showToDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    if (showToTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = toLocal.time.hour,
            initialMinute = toLocal.time.minute
        )

        Dialog(onDismissRequest = { showToTimePicker = false }) {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TimePicker(
                        state = timePickerState,
                        modifier = Modifier.padding(16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showToTimePicker = false }) {
                            Text("Cancel")
                        }

                        TextButton(
                            onClick = {
                                val newTime = toLocal.date.atTime(
                                    hour = timePickerState.hour,
                                    minute = timePickerState.minute
                                ).toInstant(TimeZone.currentSystemDefault())
                                toDateTime = newTime
                                showToTimePicker = false
                                // Clear focus to prevent keyboard from showing up again
                                focusedField = null
                            }
                        ) {
                            Text("OK")
                        }
                    }
                }
            }
        }
    }
}

private fun kotlinx.datetime.LocalDate.toEpochMilliseconds(): Long {
    return this.toEpochDays() * 24L * 60L * 60L * 1000L
}
