package com.miraya.kostku.features.location

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.miraya.kostku.*
import com.miraya.kostku.media.RemotePhotoPreview
import com.miraya.kostku.ui.common.*
import com.miraya.kostku.ui.theme.Screen
import com.miraya.kostku.ui.theme.Spacing

@Composable
internal fun LocationTab(data: RemoteSnapshot?, saving: Boolean, open: (String) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        AppTopBar("Lokasi")
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().padding(horizontal = Screen.edge, vertical = Spacing.md)) {
                val locations = data?.locations.orEmpty()
                val rooms = data?.rooms.orEmpty()
                if (locations.isEmpty()) {
                    EmptyState("Belum ada lokasi", "Tap tombol + untuk menambahkan alamat kos.", Icons.Default.LocationOn)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(Spacing.md), contentPadding = PaddingValues(bottom = Screen.fab)) {
                        items(locations, key = { it.id }) { l ->
                            Card(onClick = { open("location-open-${l.id}") }, modifier = Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
                                        LocationLogo(l.logoUrl, Modifier.size(48.dp))
                                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                                            Text(l.name, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text(listOf(l.branch, l.address).filter(String::isNotBlank).joinToString(" • "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                    val locationRooms = rooms.filter { it.propertyId == l.id }
                                    val occupied = locationRooms.count { it.status == "occupied" }
                                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                                        // Mock: "terisi" hijau bila ada penghuni, abu bila 0; "N kamar" selalu abu.
                                        StatChip("$occupied terisi", if (occupied > 0) Color(0xFF16A34A) else Color(0xFF64748B))
                                        StatChip("${locationRooms.size} kamar", Color(0xFF64748B))
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
                                        FilledTonalButton(
                                            onClick = { open("location-open-${l.id}") },
                                            shape = MaterialTheme.shapes.large,
                                            contentPadding = PaddingValues(horizontal = 18.dp)
                                        ) {
                                            Icon(Icons.Default.Home, null, Modifier.size(18.dp))
                                            Spacer(Modifier.width(Spacing.sm))
                                            Text("Buka")
                                        }
                                        Spacer(Modifier.weight(1f))
                                        IconButton({ open("location-edit-${l.id}") }) { Icon(Icons.Default.Edit, "Edit lokasi") }
                                        IconButton({ open("location-delete-${l.id}") }) { Icon(Icons.Default.Delete, "Hapus lokasi", tint = MaterialTheme.colorScheme.error) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            FloatingActionButton(
                onClick = { open("location-add") },
                modifier = Modifier.align(Alignment.BottomEnd).padding(Screen.edge)
            ) { Icon(Icons.Default.Add, "Tambah lokasi") }
        }
    }
}

/**
 * Logo lokasi: gambar tersimpan bila ada, atau ikon rumah sebagai cadangan (fallback).
 * Dipakai di daftar lokasi, tab Pengaturan, dan kwitansi agar konsisten.
 */
@Composable
internal fun LocationLogo(url: String?, modifier: Modifier = Modifier) {
    if (url.isNullOrBlank()) {
        Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.medium, modifier = modifier) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Home, "Lokasi kos", tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    } else {
        RemotePhotoPreview(url, modifier, "Logo lokasi", shape = MaterialTheme.shapes.medium)
    }
}
