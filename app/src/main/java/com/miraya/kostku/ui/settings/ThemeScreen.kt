package com.miraya.kostku.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.miraya.kostku.ui.common.AppTopBar
import com.miraya.kostku.ui.common.SegmentedPill
import com.miraya.kostku.ui.theme.AccentList
import com.miraya.kostku.ui.theme.Screen
import com.miraya.kostku.ui.theme.Spacing
import com.miraya.kostku.ui.theme.isDarkTheme
import com.miraya.kostku.ui.theme.readableOn

@Composable
internal fun ThemeScreen(mode: String, accent: String, setMode: (String) -> Unit, setAccent: (String) -> Unit, onBack: () -> Unit) {
    val modes = listOf("light" to "Terang", "dark" to "Gelap", "black" to "Hitam")
    val dark = isDarkTheme(mode)
    Scaffold(
        topBar = { AppTopBar("Tema", onBack = onBack) }
    ) { pad ->
        Column(
            Modifier.padding(pad).verticalScroll(rememberScrollState()).padding(horizontal = Screen.edge, vertical = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Text("MODE", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            SegmentedPill(modes.map { it.second }, modes.firstOrNull { it.first == mode }?.second ?: "Gelap") { label ->
                modes.firstOrNull { it.second == label }?.let { setMode(it.first) }
            }

            Text("AKSEN WARNA", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            // Warna swatch memakai nada sesuai mode aktif agar kontrasnya tampak nyata.
            AccentList.chunked(5).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    row.forEach { item ->
                        val swatch = if (dark) item.dark else item.light
                        val selected = accent == item.id
                        Column(
                            Modifier.weight(1f).clip(MaterialTheme.shapes.medium).clickable { setAccent(item.id) }.padding(vertical = Spacing.xs),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                        ) {
                            Box(
                                Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(swatch)
                                    .then(
                                        if (selected) Modifier.border(
                                            3.dp,
                                            MaterialTheme.colorScheme.primary,
                                            CircleShape
                                        ) else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (selected) Icon(Icons.Default.Check, "Terpilih", tint = readableOn(swatch))
                            }
                            Text(item.label, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                        }
                    }
                    // Isi sisa baris agar lebar kolom tetap sama.
                    repeat(5 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}
