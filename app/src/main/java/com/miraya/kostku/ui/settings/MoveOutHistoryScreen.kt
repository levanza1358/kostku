package com.miraya.kostku.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.miraya.kostku.KostkuApi
import com.miraya.kostku.RemoteMoveOutHistory
import com.miraya.kostku.formatDate
import com.miraya.kostku.moveOutHistory
import com.miraya.kostku.monthLabelLong
import com.miraya.kostku.rupiah
import com.miraya.kostku.media.ListPhotoThumb
import com.miraya.kostku.ui.common.AppTopBar
import com.miraya.kostku.ui.common.EmptyState
import com.miraya.kostku.ui.common.StatusBadge
import com.miraya.kostku.ui.theme.Screen
import com.miraya.kostku.ui.theme.Spacing
import java.time.YearMonth

@Composable
internal fun MoveOutHistoryScreen(api: KostkuApi, onBack: () -> Unit) {
    val now = remember { YearMonth.now() }
    var month by remember { mutableStateOf(now) }
    val key = "%04d-%02d".format(month.year, month.monthValue)
    // null = masih memuat; daftar diambil ulang setiap bulan berganti.
    val entries by produceState<List<RemoteMoveOutHistory>?>(initialValue = null, key) {
        value = api.moveOutHistory(key).getOrNull()
    }

    Scaffold(
        topBar = { AppTopBar("Penghuni keluar", onBack = onBack) }
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad)) {
            // Navigasi bulan: ‹  Okt 2026  ›  (tombol › nonaktif di bulan berjalan).
            Row(
                Modifier.fillMaxWidth().padding(horizontal = Screen.edge, vertical = Spacing.xs),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { month = month.minusMonths(1) }) {
                    Icon(Icons.Default.ChevronLeft, "Bulan sebelumnya")
                }
                Text(
                    monthLabelLong(month.year, month.monthValue),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { month = month.plusMonths(1) },
                    enabled = month < now
                ) { Icon(Icons.Default.ChevronRight, "Bulan berikutnya") }
            }
            HorizontalDivider()

            val list = entries
            when {
                list == null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        CircularProgressIndicator()
                        Text("Memuat riwayat…", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                list.isEmpty() -> EmptyState(
                    "Belum ada penghuni keluar",
                    "Tidak ada penghuni yang tercatat keluar pada bulan ini.",
                    Icons.Default.Person
                )
                else -> LazyColumn(
                    Modifier.fillMaxSize().padding(horizontal = Screen.edge),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                    contentPadding = PaddingValues(vertical = Spacing.md)
                ) {
                    items(list, key = { it.id }) { h ->
                        Card(Modifier.fillMaxWidth()) {
                            ListItem(
                                leadingContent = { ListPhotoThumb(h.photoUrl, "Foto ${h.name}", Icons.Default.Person) },
                                headlineContent = { Text(h.name, style = MaterialTheme.typography.titleMedium, maxLines = 1) },
                                supportingContent = {
                                    val place = h.roomName?.let { "Kamar $it" } ?: "Kamar tidak diketahui"
                                    Text(listOf(place, formatDate(h.moveOut), rupiah(h.rent)).joinToString(" • "))
                                },
                                trailingContent = { StatusBadge("Keluar", "moved_out") }
                            )
                        }
                    }
                }
            }
        }
    }
}
