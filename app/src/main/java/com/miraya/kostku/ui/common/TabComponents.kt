package com.miraya.kostku.ui.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.miraya.kostku.ui.theme.Spacing

/** Satu tab: hanya label teks (mock `.tabs` — tanpa ikon). */
internal data class AppTab(val label: String)

/**
 * Baris tab aplikasi (mock `.tabs`): tinggi 52 dp, tiap tab sama lebar, label
 * teks saja tanpa ikon, garis bawah tipis di seluruh lebar, dan indikator aksen
 * 3 dp di bawah tab terpilih.
 */
@Composable
internal fun AppTabRow(
    tabs: List<AppTab>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    onSelect: (Int) -> Unit
) {
    val accent = MaterialTheme.colorScheme.primary
    val divider = MaterialTheme.colorScheme.outline
    Row(
        modifier
            .fillMaxWidth()
            .height(52.dp)
            .drawBehind {
                val stroke = 1.dp.toPx()
                val y = size.height - stroke / 2f
                drawLine(divider, Offset(0f, y), Offset(size.width, y), strokeWidth = stroke)
            }
    ) {
        tabs.forEachIndexed { index, tab ->
            val selected = index == selectedIndex
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onSelect(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    tab.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                if (selected) {
                    Box(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                            .background(accent)
                    )
                }
            }
        }
    }
}

/**
 * Konten tab dengan transisi halus (fade + geser kecil) saat berpindah tab,
 * supaya perpindahan terasa mengalir dan bukan lompatan mendadak.
 */
@Composable
internal fun TabContent(
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    content: @Composable (Int) -> Unit
) {
    AnimatedContent(
        targetState = selectedIndex,
        modifier = modifier,
        transitionSpec = {
            val forward = targetState >= initialState
            val offset = { full: Int -> if (forward) full / 14 else -full / 14 }
            (fadeIn(tween(200)) + slideInHorizontally(tween(220), offset))
                .togetherWith(fadeOut(tween(140)))
        },
        label = "tabContent"
    ) { index -> content(index) }
}

/**
 * Segmented pill (mock `.seg`): baris pilihan dalam kapsul berbingkai dengan pilihan
 * aktif berlatar aksen. Dipakai untuk pemilih sub-bagian (mis. Semua/Pemasukan/Pengeluaran,
 * Pemasukan/Pengeluaran kategori) — berbeda dari [AppTabRow] yang bergaris-bawah + ikon.
 */
@Composable
internal fun SegmentedPill(
    options: List<String>,
    selected: String,
    modifier: Modifier = Modifier,
    onSelect: (String) -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(Modifier.padding(4.dp)) {
            options.forEach { option ->
                val on = option == selected
                Surface(
                    onClick = { if (!on) onSelect(option) },
                    modifier = Modifier.weight(1f),
                    shape = CircleShape,
                    color = if (on) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (on) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Text(
                        option,
                        Modifier.padding(vertical = Spacing.sm).fillMaxWidth(),
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
