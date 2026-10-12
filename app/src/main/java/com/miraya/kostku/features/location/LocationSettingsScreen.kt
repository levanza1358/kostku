package com.miraya.kostku.features.location

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.miraya.kostku.*
import com.miraya.kostku.ui.common.AppTopBar
import com.miraya.kostku.ui.common.SectionTitle
import com.miraya.kostku.ui.theme.Screen
import com.miraya.kostku.ui.theme.Spacing

/**
 * Pengaturan lokasi: tempat mengatur **tipe kamar** dan **nomor kamar**, mengedit info
 * lokasi (termasuk logo & utilitas), dan menghapus lokasi. Dipisahkan dari layar detail
 * agar daftar Kamar tetap ringkas (hanya Detail + Edit).
 */
@Composable
internal fun LocationSettingsScreen(
    location: RemoteLocation,
    data: RemoteSnapshot,
    onBack: () -> Unit,
    open: (String) -> Unit
) {
    Scaffold(topBar = { AppTopBar("Pengaturan", onBack = onBack) }) { pad ->
        Column(
            Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(horizontal = Screen.edge, vertical = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            // Identitas lokasi + logo.
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(Spacing.lg), horizontalArrangement = Arrangement.spacedBy(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
                    LocationLogo(location.logoUrl, Modifier.size(52.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(location.name, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(listOf(location.branch, location.address).filter(String::isNotBlank).joinToString(" • "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }

            SettingsRow("Edit info lokasi", "Nama, cabang, alamat, logo", Icons.Default.Edit) { open("location-edit-${location.id}") }

            SectionTitle("Kamar")
            SettingsRow("Tipe kamar", "Tambah / ubah tipe kamar", Icons.Default.GridView) { open("roomtype-manage-${location.id}") }
            SettingsRow("Nomor kamar", "Tambah / ubah nomor kamar", Icons.Default.VpnKey) { open("rooms-${location.id}") }

            SectionTitle("Lainnya")
            Card(onClick = { open("location-delete-${location.id}") }, modifier = Modifier.fillMaxWidth()) {
                ListItem(
                    leadingContent = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                    headlineContent = { Text("Hapus lokasi", color = MaterialTheme.colorScheme.error) },
                    supportingContent = { Text("Hapus lokasi + seluruh kamar") }
                )
            }
        }
    }
}

@Composable
private fun SettingsRow(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        ListItem(
            leadingContent = { Icon(icon, null) },
            headlineContent = { Text(title) },
            supportingContent = { Text(subtitle) },
            trailingContent = { Icon(Icons.Default.ChevronRight, null) }
        )
    }
}

/** Baris label + nilai utilitas; nilai kosong ditampilkan sebagai "Tidak tersedia" (redup). */
@Composable
internal fun UtilityRow(label: String, value: String) {
    val available = value != "Tidak tersedia"
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(Spacing.md))
        Text(
            value,
            style = MaterialTheme.typography.bodyLarge,
            color = if (available) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
