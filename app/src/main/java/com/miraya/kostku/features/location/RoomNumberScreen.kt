package com.miraya.kostku.features.location

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.miraya.kostku.*
import com.miraya.kostku.ui.common.AppTextField
import com.miraya.kostku.ui.common.AppTopBar
import com.miraya.kostku.ui.theme.Dimens
import com.miraya.kostku.ui.theme.Screen
import com.miraya.kostku.ui.theme.Spacing

/**
 * Daftar **nomor kamar** lokasi (dari Pengaturan): menampilkan tiap kamar beserta
 * ringkasan utilitas (token listrik / PDAM), dengan tombol edit per baris dan tombol
 * "Tambah nomor kamar" di bawah. Ini yang menggantikan aksi tambah/hapus di daftar Kamar
 * pada layar detail.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RoomNumberScreen(
    location: RemoteLocation,
    rooms: List<RemoteRoom>,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onAdd: () -> Unit
) {
    val shown = rooms
        .sortedWith(compareBy({ it.roomTypeName ?: "" }, { it.name.lowercase() }))
    Scaffold(
        topBar = { AppTopBar("Nomor kamar", onBack = onBack) },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Box(Modifier.fillMaxWidth().padding(horizontal = Screen.edge, vertical = Spacing.md)) {
                    Button(onClick = onAdd, modifier = Modifier.fillMaxWidth().height(Dimens.ButtonHeight), shape = MaterialTheme.shapes.large) {
                        Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(Spacing.sm))
                        Text("Tambah nomor kamar", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad)) {
            Column(Modifier.padding(horizontal = Screen.edge, vertical = Spacing.sm), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(location.name, style = MaterialTheme.typography.titleMedium)
                Text("Tambah & ubah nomor/urutan kamar", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (rooms.isEmpty()) {
                Text("Belum ada kamar. Tambahkan nomor kamar terlebih dahulu.", Modifier.padding(Screen.edge))
            } else {
                LazyColumn(
                    Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(start = Screen.edge, end = Screen.edge, top = Spacing.xs, bottom = Screen.bottom),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    items(shown, key = { it.id }) { room ->
                        Card(Modifier.fillMaxWidth()) {
                            Row(Modifier.padding(Spacing.md), horizontalArrangement = Arrangement.spacedBy(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
                                val hasUtility = !room.tokenListrik.isNullOrBlank() || !room.pdamNumber.isNullOrBlank()
                                Surface(
                                    color = if (hasUtility) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = MaterialTheme.shapes.small,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.VpnKey,
                                            null,
                                            Modifier.size(18.dp),
                                            tint = if (hasUtility) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text("Kamar ${room.name}", style = MaterialTheme.typography.titleSmall)
                                    Text("Tipe ${room.roomTypeName ?: "belum ditentukan"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(utilitySummary(room), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                IconButton({ onEdit(room.id) }) { Icon(Icons.Default.Edit, "Edit kamar") }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Ringkasan utilitas satu kamar untuk daftar nomor kamar. */
private fun utilitySummary(room: RemoteRoom): String {
    val token = room.tokenListrik?.takeIf { it.isNotBlank() }
    val pdam = room.pdamNumber?.takeIf { it.isNotBlank() }
    return when {
        token == null && pdam == null -> "Token & PDAM belum diisi"
        else -> "Token ${token ?: "tidak tersedia"} • PDAM ${pdam ?: "tidak tersedia"}"
    }
}
