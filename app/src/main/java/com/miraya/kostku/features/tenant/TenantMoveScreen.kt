package com.miraya.kostku.features.tenant

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.miraya.kostku.*
import com.miraya.kostku.ui.common.FormCard
import com.miraya.kostku.ui.common.FormRow
import com.miraya.kostku.ui.common.FormSection
import com.miraya.kostku.ui.theme.Spacing

/**
 * 3. Pindah kamar — pilih kamar kosong (bisa lintas lokasi) lewat daftar, seperti mock 3.9.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TenantMoveScreen(
    tenant: RemoteTenant,
    data: RemoteSnapshot,
    saving: Boolean,
    onBack: () -> Unit,
    onSave: (roomId: String) -> Unit
) {
    val candidates = remember(data, tenant.roomId) {
        data.rooms.filter { it.status == "vacant" && it.id != tenant.roomId }
            .sortedWith(compareBy({ it.roomTypeName ?: "" }, { it.name.lowercase() }))
    }
    val locationName = { propertyId: String -> data.locations.firstOrNull { it.id == propertyId }?.name }
    val roomLabel = { name: String -> "Kamar $name" }
    var selected by remember(candidates) { mutableStateOf(candidates.firstOrNull()) }
    val currentRoom = data.rooms.firstOrNull { it.id == tenant.roomId }
    val currentLocation = currentRoom?.let { locationName(it.propertyId) }

    TenantActionScaffold("Pindah kamar", onBack, "Pindahkan", saving, {
        selected?.let { onSave(it.id) }
    }, ctaIcon = Icons.Default.SwapHoriz) {
        FormCard {
            FormRow(title = "Kamar saat ini", subtitle = listOfNotNull(currentLocation, currentRoom?.let { roomLabel(it.name) }).joinToString(" • ").ifBlank { "Tidak diketahui" })
        }

        Card(
            Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        ) {
            Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                FormSection("Kamar tujuan")
                if (candidates.isEmpty()) {
                    Text("Belum ada kamar kosong yang bisa dipilih.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                } else {
                    candidates.forEach { room ->
                        val isSelected = room.id == selected?.id
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .alpha(if (isSelected) 1f else 0.55f)
                                .clickable { selected = room },
                            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                shape = CircleShape,
                                modifier = Modifier.size(30.dp)
                            ) {
                                if (isSelected) {
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.VpnKey, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(listOfNotNull(locationName(room.propertyId), roomLabel(room.name)).joinToString(" • "), style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    "${roomStatusLabel(room.status)} • Tipe ${room.roomTypeName ?: "belum ditentukan"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isSelected) Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Text("Hanya kamar kosong yang dapat dipilih.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Catatan bergaris putus (mock 3.9).
        val outlineColor = MaterialTheme.colorScheme.outline
        Row(
            Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawRoundRect(
                        color = outlineColor,
                        cornerRadius = CornerRadius(16.dp.toPx()),
                        style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f))
                    )
                }
                .padding(Spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Info, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(18.dp))
            Text("Penghuni & sewa tetap; hanya kamar yang berpindah.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
