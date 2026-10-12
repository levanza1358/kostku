package com.miraya.kostku.features.location

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.miraya.kostku.*
import com.miraya.kostku.ui.common.*
import com.miraya.kostku.ui.theme.Screen
import com.miraya.kostku.ui.theme.Spacing

@Composable
internal fun RoomListTab(rooms: List<RemoteRoom>, occupants: Map<String, String>, query: String, onQuery: (String) -> Unit, onDetail: (String) -> Unit, onEdit: (String) -> Unit) {
    val shown = rooms
        .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) }
        .sortedWith(compareBy({ it.roomTypeName ?: "" }, { it.name.lowercase() }))
    Column(Modifier.fillMaxSize()) {
        AppTextField(
            value = query,
            onValueChange = onQuery,
            label = "Cari nomor kamar…",
            leadingIcon = { Icon(Icons.Default.Search, null) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = Screen.edge, vertical = Spacing.sm)
        )
        if (rooms.isEmpty()) {
            Text("Belum ada kamar di lokasi ini.", Modifier.padding(Screen.edge))
            return@Column
        }
        LazyColumn(
            Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(start = Screen.edge, end = Screen.edge, bottom = Screen.bottom),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            items(shown, key = { it.id }) { room ->
                val occupantName = occupants[room.id]
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(Spacing.md), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Kamar ${room.name}", style = MaterialTheme.typography.titleMedium)
                            StatusBadge(roomStatusLabel(room.status), room.status)
                        }
                        // Nama penghuni aktif tampil di baris kamar yang terisi.
                        if (occupantName != null) {
                            Text(occupantName, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                        } else {
                            Text("Tipe: ${room.roomTypeName ?: "belum ditentukan"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
                            FilledTonalButton(
                                { onDetail(room.id) },
                                shape = MaterialTheme.shapes.large,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = Spacing.sm)
                            ) { Text("Detail", style = MaterialTheme.typography.labelLarge) }
                            IconButton({ onEdit(room.id) }) { Icon(Icons.Default.Edit, "Edit kamar") }
                        }
                    }
                }
            }
        }
    }
}
