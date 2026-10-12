package com.miraya.kostku.features.location

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.miraya.kostku.media.ClickableRemotePhoto
import com.miraya.kostku.ui.common.*
import com.miraya.kostku.ui.theme.Screen
import com.miraya.kostku.ui.theme.Spacing
import java.time.LocalDate

@Composable
internal fun LocationDashboard(
    location: RemoteLocation,
    rooms: List<RemoteRoom>,
    tenants: List<RemoteTenant>,
    payments: List<RemotePayment>,
    expenses: List<RemoteExpense>,
    open: (String) -> Unit
) {
    val occupied = rooms.count { it.status == "occupied" }
    val vacant = rooms.count { it.status == "vacant" }
    val dirty = rooms.count { it.status == "dirty" }
    val maintenance = rooms.count { it.status == "maintenance" }
    val occupancyPct = if (rooms.isEmpty()) 0 else (occupied * 100 / rooms.size)
    val activeTenants = tenants.filter { it.active }
    val monthKey = LocalDate.now().toString().take(7)
    val incomeMonth = payments.filter { it.period.take(7) == monthKey }.sumOf { it.amount }
    val expenseMonth = expenses.filter { it.spentAt.take(7) == monthKey }.sumOf { it.amount }
    val dueSoon = activeTenants
        .map { it to daysUntil(it.moveOut) }
        .filter { it.second != null && it.second!! >= 0 }
        .minByOrNull { it.second!! }

    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = Screen.edge, vertical = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        // Kartu okupansi dengan bar progres + chip status kamar.
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("TERISI", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${rooms.size} kamar", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Text("$occupied / ${rooms.size}", style = MaterialTheme.typography.headlineMedium)
                    // Chip aksen (mock .chip.acc): latar accentContainer, teks aksen.
                    Text(
                        "$occupancyPct%",
                        Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.small)
                            .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                LinearProgressIndicator(
                    progress = { occupancyPct / 100f },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    StatChip("$occupied terisi", Color(0xFF16A34A))
                    StatChip("$vacant tersedia", Color(0xFF64748B))
                    if (dirty > 0) StatChip("$dirty kotor", Color(0xFFF59E0B))
                    if (maintenance > 0) StatChip("$maintenance perbaikan", Color(0xFF2563EB))
                }
            }
        }
        // Ringkasan keuangan bulan ini.
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            SummaryCard("Masuk bulan ini", rupiah(incomeMonth), Icons.Default.TrendingUp, Modifier.weight(1f))
            SummaryCard("Keluar bulan ini", rupiah(expenseMonth), Icons.Default.TrendingDown, Modifier.weight(1f), Color(0xFFEF4444))
        }
        // Jatuh tempo terdekat.
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Jatuh tempo terdekat", style = MaterialTheme.typography.titleSmall)
                    dueSoon?.let { DayLeftChip(it.second) }
                }
                if (dueSoon == null) {
                    Text("Belum ada penghuni aktif dengan tanggal keluar.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    val tenant = dueSoon.first
                    val room = rooms.firstOrNull { it.id == tenant.roomId }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                        ClickableRemotePhoto(tenant.photoUrl, Modifier.size(36.dp), "Foto ${tenant.name}")
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(tenant.name, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(listOfNotNull(room?.name?.let { "Kamar $it" }, tenant.moveOut?.let { formatDate(it) }).joinToString(" • "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                        }
                    }
                }
            }
        }
        // Perlu tindakan: pintasan menambah penghuni / mencatat pembayaran.
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                Text("Perlu tindakan", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    FilledTonalButton(
                        { open("add-tenant-${location.id}") },
                        shape = MaterialTheme.shapes.large,
                        contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.sm)
                    ) {
                        Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(Spacing.xs))
                        Text("Tambah penghuni", style = MaterialTheme.typography.labelLarge)
                    }
                    Button(
                        { open("payment") },
                        shape = MaterialTheme.shapes.large,
                        contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.sm)
                    ) {
                        Icon(Icons.Default.TrendingUp, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(Spacing.xs))
                        Text("Catat pembayaran", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}
