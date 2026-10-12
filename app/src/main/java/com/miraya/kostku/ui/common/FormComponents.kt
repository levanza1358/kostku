package com.miraya.kostku.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.miraya.kostku.ui.theme.FieldShape
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> ChoiceDropdown(
    label: String,
    selected: T?,
    values: List<T>,
    text: (T) -> String,
    enabled: Boolean = true,
    onSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = { if (enabled) expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected?.let(text).orEmpty(),
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            singleLine = true,
            shape = FieldShape,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded && enabled) }
        )
        ExposedDropdownMenu(expanded = expanded && enabled, onDismissRequest = { expanded = false }) {
            if (values.isEmpty()) {
                DropdownMenuItem(text = { Text("Data belum tersedia") }, onClick = { expanded = false })
            } else {
                values.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(text(item)) },
                        onClick = { onSelected(item); expanded = false }
                    )
                }
            }
        }
    }
}

/** Bidang tanggal read-only yang membuka DatePicker Material 3. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateField(
    label: String,
    value: LocalDate?,
    onDate: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    var show by remember { mutableStateOf(false) }
    val state = rememberDatePickerState(
        initialSelectedDateMillis = value?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
    )
    OutlinedTextField(
        value = value?.let { com.miraya.kostku.formatDate(it) }.orEmpty(),
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        trailingIcon = { IconButton(onClick = { show = true }) { Icon(Icons.Default.DateRange, "Pilih tanggal") } },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        shape = FieldShape
    )
    if (show) {
        DatePickerDialog(
            onDismissRequest = { show = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        onDate(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    show = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { show = false }) { Text("Batal") } }
        ) { DatePicker(state = state) }
    }
}
