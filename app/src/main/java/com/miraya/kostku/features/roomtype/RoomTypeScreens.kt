package com.miraya.kostku.features.roomtype

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.miraya.kostku.RemoteLocation
import com.miraya.kostku.RemoteRoomType
import com.miraya.kostku.ui.common.AppTextField
import com.miraya.kostku.ui.common.AppTopBar
import com.miraya.kostku.ui.common.ConfirmDialog
import com.miraya.kostku.ui.common.SaveDialogButton
import com.miraya.kostku.ui.theme.Screen
import com.miraya.kostku.ui.theme.Spacing

internal val durationUnits = listOf("Hari" to "day", "Minggu" to "week", "Bulan" to "month", "Tahun" to "year")
internal fun durationRank(unit: String) = listOf("day", "week", "month", "year").indexOf(unit).coerceAtLeast(0)

@Composable
internal fun RoomTypeScreen(location: RemoteLocation, types: List<RemoteRoomType>, saving: Boolean, onBack: () -> Unit, onOpen: (RemoteRoomType) -> Unit, onSave: (RemoteRoomType?, String) -> Unit, onDelete: (RemoteRoomType) -> Unit) {
    var editing by remember { mutableStateOf<RemoteRoomType?>(null) }
    var adding by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<RemoteRoomType?>(null) }
    Scaffold(topBar = { AppTopBar("Kelola tipe kamar", onBack = onBack) }) { pad ->
        LazyColumn(
            Modifier.fillMaxSize().padding(pad).padding(horizontal = Screen.edge, vertical = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            item { Text(location.name, style = MaterialTheme.typography.titleMedium); Text("Tipe kamar di lokasi ini", style = MaterialTheme.typography.bodyMedium) }
            item { Button({ adding = true }, enabled = !saving, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) { Text("Tambah tipe kamar") } }
            if (types.isEmpty()) item { Text("Belum ada tipe kamar", style = MaterialTheme.typography.bodyLarge) }
            itemsIndexed(types, key = { _, it -> it.id }) { index, type ->
                Card(onClick = { onOpen(type) }, modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(Spacing.md), horizontalArrangement = Arrangement.spacedBy(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = if (index == 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.GridView, null, Modifier.size(18.dp), tint = if (index == 0) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(type.name, style = MaterialTheme.typography.titleSmall)
                            Text("Kelola harga dan kamar", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        OutlinedButton({ editing = type }, shape = MaterialTheme.shapes.small, contentPadding = PaddingValues(horizontal = Spacing.md, vertical = 0.dp), modifier = Modifier.height(34.dp)) { Text("Edit", style = MaterialTheme.typography.labelLarge) }
                        IconButton({ deleteTarget = type }) { Icon(Icons.Default.Delete, "Hapus tipe kamar", tint = MaterialTheme.colorScheme.error) }
                    }
                }
            }
        }
    }
    if (adding || editing != null) TypeNameDialog(editing, saving, { adding = false; editing = null }) { name -> onSave(editing, name); adding = false; editing = null }
    deleteTarget?.let { target -> ConfirmDialog(
        title = "Hapus tipe kamar?",
        message = "Tipe \"${target.name}\" beserta harga & kamarnya akan dihapus. Tindakan ini tidak dapat dibatalkan.",
        saving = saving,
        dismiss = { deleteTarget = null },
        confirmLabel = "Hapus",
        destructive = true
    ) { val t = target; deleteTarget = null; onDelete(t) } }
}

@Composable private fun TypeNameDialog(existing: RemoteRoomType?, saving: Boolean, dismiss: () -> Unit, save: (String) -> Unit) {
    var name by remember(existing) { mutableStateOf(existing?.name.orEmpty()) }
    AlertDialog(
        onDismissRequest = dismiss,
        title = { Text(if (existing == null) "Tambah tipe kamar" else "Edit tipe kamar") },
        text = {
            AppTextField(
                value = name,
                onValueChange = { name = it },
                label = "Nama tipe kamar",
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = { SaveDialogButton(label = "Simpan", onClick = { if (name.isNotBlank()) save(name.trim()) }, saving = saving, enabled = name.isNotBlank()) },
        dismissButton = { TextButton(onClick = dismiss, enabled = !saving) { Text("Batal") } }
    )
}
