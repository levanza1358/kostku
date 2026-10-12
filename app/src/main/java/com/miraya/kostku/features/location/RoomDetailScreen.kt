package com.miraya.kostku.features.location

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.miraya.kostku.*
import com.miraya.kostku.ui.common.AppTopBar
import com.miraya.kostku.ui.common.StatChip
import com.miraya.kostku.ui.common.statusColor
import com.miraya.kostku.ui.theme.Dimens
import com.miraya.kostku.ui.theme.Screen
import com.miraya.kostku.ui.theme.Spacing

/**
 * Detail nomor kamar (mock "Detail nomor kamar"): hero ikon kunci + nama kamar + lokasi/tipe +
 * chip status, lalu satu kartu kv Penghuni / Nomor token listrik / Nomor PDAM — "Tidak tersedia"
 * bila belum diisi. Tombol mengarah ke layar edit kamar.
 */
@Composable
internal fun RoomDetailScreen(
    room: RemoteRoom,
    tenant: RemoteTenant?,
    location: RemoteLocation?,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    Scaffold(
        topBar = { AppTopBar("Detail kamar", onBack = onBack) },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Box(Modifier.fillMaxWidth().padding(horizontal = Screen.edge, vertical = Spacing.md)) {
                    Button(onClick = onEdit, modifier = Modifier.fillMaxWidth().height(Dimens.ButtonHeight), shape = MaterialTheme.shapes.large) {
                        Text("Edit kamar", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    ) { pad ->
        Column(
            Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(horizontal = Screen.edge, vertical = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            // Hero: ikon kunci + identitas kamar (mock memakai ikon, bukan foto).
            Card(
                Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(Modifier.padding(Spacing.lg), horizontalArrangement = Arrangement.spacedBy(15.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.medium, modifier = Modifier.size(48.dp)) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.VpnKey, null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                        Text("Kamar ${room.name}", style = MaterialTheme.typography.titleMedium)
                        Text(
                            listOfNotNull(location?.name, room.roomTypeName?.let { "Tipe $it" }).joinToString(" • ").ifBlank { "Tipe belum ditentukan" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        StatChip(roomStatusLabel(room.status), statusColor(room.status))
                    }
                }
            }

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    UtilityRow("Penghuni", tenant?.name ?: "Tidak tersedia")
                    UtilityRow("Nomor token listrik", utilityText(room.tokenListrik))
                    UtilityRow("Nomor PDAM", utilityText(room.pdamNumber))
                }
            }
            Text("Nomor utilitas opsional. Data contoh, bukan nomor pelanggan asli.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
