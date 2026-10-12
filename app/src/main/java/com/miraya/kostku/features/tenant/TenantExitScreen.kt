package com.miraya.kostku.features.tenant

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.miraya.kostku.*
import com.miraya.kostku.ui.common.FormCard
import com.miraya.kostku.ui.common.FormRow
import com.miraya.kostku.ui.common.FormSection
import com.miraya.kostku.ui.common.SelectableChip
import com.miraya.kostku.ui.theme.Spacing
import java.time.LocalDate

/**
 * 5. Penghuni keluar — tandai keluar: data otomatis diarsipkan ke riwayat
 * "Penghuni keluar", lalu status kamar ditanyakan (Tersedia / Kotor / Maintenance).
 */
@Composable
internal fun TenantExitScreen(
    tenant: RemoteTenant,
    roomName: String?,
    saving: Boolean,
    onBack: () -> Unit,
    onMoveOut: (date: LocalDate, roomStatus: String) -> Unit
) {
    val end = tenant.moveOut?.take(10)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
    var picked by remember { mutableStateOf<String?>(null) }
    val roomStatuses = listOf(
        "Tersedia" to "vacant",
        "Kotor" to "dirty",
        "Maintenance" to "maintenance"
    )

    if (picked == null) {
        // Langkah 1 — ringkasan + aksi tandai keluar.
        TenantActionScaffold("Penghuni keluar", onBack, "Tandai keluar hari ini", saving, {
            picked = "vacant"
        }, ctaIcon = Icons.Default.ExitToApp) {
            FormCard {
                FormRow(title = "Status", subtitle = if (tenant.active) "Aktif" else "Sudah keluar")
                FormRow(title = "Kamar", subtitle = if (tenant.active) "Kamar akan tersedia kembali" else "Sudah tidak menempati kamar")
                FormRow(title = "Perkiraan akhir sewa", subtitle = end?.let { formatDate(it) } ?: "Tidak diketahui")
            }
            Text(
                "Data penghuni otomatis dipindahkan ke riwayat Penghuni keluar; riwayat pembayaran tetap tersimpan.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        // Langkah 2 — status kamar setelah penghuni keluar.
        TenantActionScaffold("Status kamar", { picked = null }, "Tandai keluar hari ini", saving, {
            onMoveOut(LocalDate.now(), picked ?: "vacant")
        }, ctaIcon = Icons.Default.ExitToApp) {
            Text(
                "Penghuni ditandai keluar. Status ${roomName?.let { "Kamar $it" } ?: "kamar"} akan diubah menjadi apa?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FormSection("Status kamar")
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                roomStatuses.forEach { (label, value) ->
                    SelectableChip(label, selected = picked == value, onClick = { picked = value })
                }
            }
            Text(
                "Status ini bisa diubah lagi lewat Edit kamar di tab Lokasi.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
