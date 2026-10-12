package com.miraya.kostku.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.miraya.kostku.ui.theme.Spacing

/** Baris chip filter yang dapat digulir horizontal. */
@Composable
fun FilterChipsRow(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), modifier = Modifier.fillMaxWidth()) {
        items(options.size) { index ->
            val option = options[index]
            FilterChip(
                selected = option == selected,
                onClick = { onSelect(option) },
                label = { Text(option) }
            )
        }
    }
}

/**
 * Pemilih segmen yang tidak melebar penuh (mock `.seg`): kapsul berbingkai tipis dengan
 * pilihan aktif berlatar accentContainer. Pilihan tanpa bobot — lebarnya mengikuti teks.
 */
@Composable
fun SegmentedPillCompact(options: List<String>, selected: String, modifier: Modifier = Modifier, onSelect: (String) -> Unit) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(Modifier.padding(4.dp)) {
            options.forEach { option ->
                val on = option == selected
                Surface(
                    onClick = { if (!on) onSelect(option) },
                    shape = CircleShape,
                    color = if (on) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (on) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Text(
                        option,
                        Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm),
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
