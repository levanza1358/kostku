package com.miraya.kostku.features.payment

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

/** Menu titik-tiga: mengembalikan toggle ke pemanggil agar tombol ikonnya ikut ditampilkan. */
@Composable
internal fun RowActionMenu(
    items: List<RowAction>,
    enabled: Boolean,
    content: @Composable (() -> Unit) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        content { if (enabled) expanded = !expanded }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Text(
                            item.label,
                            color = if (item.destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = { expanded = false; item.action() }
                )
            }
        }
    }
}

internal data class RowAction(val label: String, val destructive: Boolean = false, val action: () -> Unit)

@Composable
internal fun OverflowButton(toggle: () -> Unit, contentDescription: String) {
    IconButton(onClick = toggle) { Icon(Icons.Default.MoreVert, contentDescription) }
}
