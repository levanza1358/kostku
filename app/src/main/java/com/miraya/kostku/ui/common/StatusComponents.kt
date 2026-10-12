package com.miraya.kostku.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.miraya.kostku.ui.theme.Spacing

/** Judul bagian layar dengan garis pembatas halus. */
@Composable
fun SectionTitle(title: String, modifier: Modifier = Modifier) {
    Column(modifier.padding(top = Spacing.sm)) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        HorizontalDivider(Modifier.padding(top = Spacing.xs))
    }
}

/** Warna status untuk kamar / pembayaran / aktif. */
@Composable
fun statusColor(status: String): Color = when (status.lowercase()) {
    "paid", "vacant", "active" -> Color(0xFF16A34A)
    "partial", "occupied" -> Color(0xFF2563EB)
    "pending", "maintenance" -> Color(0xFFF59E0B)
    "late", "dirty" -> Color(0xFFEF4444)
    "cancelled", "moved_out", "archived" -> Color(0xFF64748B)
    else -> MaterialTheme.colorScheme.primary
}

/** Badge status kecil: titik + label berwarna sesuai status. */
@Composable
fun StatusBadge(label: String, status: String, modifier: Modifier = Modifier) {
    val color = statusColor(status)
    Row(
        modifier
            .background(color.copy(alpha = 0.16f), MaterialTheme.shapes.small)
            .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(Modifier.size(8.dp)) { drawCircle(color) }
        Text(label, style = MaterialTheme.typography.labelMedium, color = color, maxLines = 1)
    }
}

/** Chip kecil dengan titik warna eksplisit (untuk ringkasan hunian: terisi/tersedia/kotor). */
@Composable
fun StatChip(label: String, color: Color, modifier: Modifier = Modifier) {
    Row(
        modifier
            .background(color.copy(alpha = 0.16f), MaterialTheme.shapes.small)
            .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(Modifier.size(8.dp)) { drawCircle(color) }
        Text(label, style = MaterialTheme.typography.labelMedium, color = color, maxLines = 1)
    }
}

/** Chip pilihan (tanpa centang) untuk baris status: terisi beraksen, lain netral. */
@Composable
fun SelectableChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val bg by androidx.compose.animation.animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        label = "chipBg"
    )
    val fg by androidx.compose.animation.animateColorAsState(
        if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "chipFg"
    )
    Surface(onClick = onClick, color = bg, contentColor = fg, shape = MaterialTheme.shapes.small, modifier = modifier) {
        Text(label, Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm), style = MaterialTheme.typography.labelLarge, maxLines = 1)
    }
}

/**
 * Chip pilihan status (mock `.chip ok`/`.chip neutral`): saat terpilih memakai [activeColor],
 * saat tidak memakai latar netral dengan teks redup.
 */
@Composable
fun SelectChip(label: String, selected: Boolean, activeColor: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val fg = if (selected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(
        onClick = onClick,
        color = fg.copy(alpha = 0.16f),
        contentColor = fg,
        shape = MaterialTheme.shapes.small,
        modifier = modifier
    ) {
        Text(label, Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm), style = MaterialTheme.typography.labelLarge, maxLines = 1)
    }
}

/**
 * Warna chip "sisa hari" penghuni: hijau (≥14 hari), kuning (7–13), merah (<7 atau sudah
 * lewat), dan abu-abu bila tidak ada tanggal keluar. Menunjukkan **jarak hari**, bukan
 * status keanggotaan.
 */
@Composable
fun dayLeftColor(days: Long?): Color = when {
    days == null -> Color(0xFF64748B)
    days < 0 -> Color(0xFFEF4444)
    days >= 14 -> Color(0xFF16A34A)
    days >= 7 -> Color(0xFFF59E0B)
    else -> Color(0xFFEF4444)
}

/** Chip "sisa hari" untuk daftar/detail penghuni (tanpa titik, latar berwarna lembut). */
@Composable
fun DayLeftChip(days: Long?, modifier: Modifier = Modifier) {
    val color = dayLeftColor(days)
    val label = com.miraya.kostku.dayLeftLabel(days)
    Text(
        label,
        modifier
            .background(color.copy(alpha = 0.16f), MaterialTheme.shapes.small)
            .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
        style = MaterialTheme.typography.labelMedium,
        color = color,
        maxLines = 1
    )
}

/**
 * Pratinjau tema langsung: merender komponen nyata (judul bagian, tombol beraksen dengan
 * teks di atasnya, badge status) memakai [scheme] sehingga kontras teks-aksen terlihat
 * persis seperti di aplikasi.
 */
@Composable
fun ThemePreview(scheme: ColorScheme, modifier: Modifier = Modifier) {
    MaterialTheme(colorScheme = scheme) {
        Column(
            modifier
                .background(scheme.background, MaterialTheme.shapes.medium)
                .padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Text(
                "Judul bagian",
                style = MaterialTheme.typography.titleSmall,
                color = scheme.primary
            )
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = {},
                    shape = MaterialTheme.shapes.large,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
                ) { Text("Tombol") }
                FilledTonalButton(
                    onClick = {},
                    shape = MaterialTheme.shapes.large,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
                ) { Text("Sekunder") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
                StatusBadge("Lunas", "paid")
                StatusBadge("Menunggu", "pending")
                Text("Teks biasa", style = MaterialTheme.typography.bodyMedium, color = scheme.onSurface)
            }
        }
    }
}
