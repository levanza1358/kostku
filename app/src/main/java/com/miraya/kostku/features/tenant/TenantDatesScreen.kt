package com.miraya.kostku.features.tenant

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.miraya.kostku.*
import com.miraya.kostku.ui.common.DateField
import com.miraya.kostku.ui.theme.Spacing
import java.time.LocalDate

/**
 * 3.11 Pindah tanggal — ubah tanggal masuk & tanggal keluar (mock: dua bidang tanggal
 * berdiri sendiri + catatan bergaris putus, tanpa kartu pembungkus).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TenantDatesScreen(
    tenant: RemoteTenant,
    saving: Boolean,
    onBack: () -> Unit,
    onSave: (moveIn: LocalDate, moveOut: LocalDate?) -> Unit
) {
    val parse = { s: String? -> s?.take(10)?.let { runCatching { LocalDate.parse(it) }.getOrNull() } }
    var moveIn by remember { mutableStateOf(parse(tenant.moveIn) ?: LocalDate.now()) }
    var moveOut by remember { mutableStateOf(parse(tenant.moveOut)) }

    TenantActionScaffold("Pindah tanggal", onBack, "Simpan tanggal", saving, {
        onSave(moveIn, moveOut)
    }) {
        DateField("Tanggal masuk", moveIn, { moveIn = it })
        DateField("Tanggal keluar", moveOut, { moveOut = it })

        // Catatan bergaris putus (mock 3.11).
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
            Text("Mengubah tanggal tidak menghitung ulang harga.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
