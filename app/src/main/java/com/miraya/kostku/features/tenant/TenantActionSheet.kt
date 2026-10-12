package com.miraya.kostku.features.tenant

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.miraya.kostku.ui.theme.Screen
import com.miraya.kostku.ui.theme.Spacing

/** Satu aksi pada lembar "Kelola penghuni". */
internal data class TenantAction(val label: String, val hint: String, val icon: ImageVector, val id: String, val warn: Boolean = false)

/** Lima aksi kelola penghuni yang tampil di bottom sheet FAB. */
internal val tenantActions = listOf(
    TenantAction("Edit data", "Ubah nama, kontak, dan foto", Icons.Default.Edit, "edit"),
    TenantAction("Perpanjang kos", "Tambah durasi sewa & perbarui harga", Icons.Default.EventRepeat, "extend"),
    TenantAction("Pindah kamar", "Pindahkan ke kamar lain di lokasi ini", Icons.Default.SwapHoriz, "move"),
    TenantAction("Pindah tanggal", "Ubah tanggal masuk & keluar", Icons.Default.EditCalendar, "dates"),
    TenantAction("Penghuni keluar", "Tandai keluar & arsipkan otomatis", Icons.Default.ExitToApp, "exit", warn = true)
)

/** Lembar aksi (bottom sheet) yang dibuka dari FAB di detail penghuni. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TenantActionSheet(
    onAction: (String) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState
) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.fillMaxWidth().padding(horizontal = Screen.edge).padding(bottom = Spacing.xl)) {
            Text(
                "Kelola penghuni",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = Spacing.sm)
            )
            tenantActions.forEach { a ->
                val warnColor = Color(0xFFFCD34D)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onDismiss(); onAction(a.id) }
                        .padding(vertical = Spacing.md, horizontal = Spacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.lg)
                ) {
                    Icon(a.icon, contentDescription = null, tint = if (a.warn) warnColor else MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(a.label, style = MaterialTheme.typography.titleMedium)
                        Text(a.hint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (a != tenantActions.last()) HorizontalDivider(Modifier.padding(horizontal = Spacing.xs))
            }
        }
    }
}
