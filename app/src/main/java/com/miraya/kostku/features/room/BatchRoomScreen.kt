package com.miraya.kostku.features.room

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.miraya.kostku.RemoteLocation
import com.miraya.kostku.RemoteRoomType
import com.miraya.kostku.ui.common.AppTextField
import com.miraya.kostku.ui.common.AppTopBar
import com.miraya.kostku.ui.common.ChoiceDropdown
import com.miraya.kostku.ui.theme.Dimens
import com.miraya.kostku.ui.theme.Screen
import com.miraya.kostku.ui.theme.Spacing

/**
 * 4.8 Tambah banyak kamar: tipe + awalan + jumlah, lalu pratinjau "Akan dibuat:".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BatchRoomScreen(
    location: RemoteLocation,
    types: List<RemoteRoomType>,
    initialType: RemoteRoomType?,
    saving: Boolean,
    progress: Int?,
    onBack: () -> Unit,
    onCreate: (RemoteRoomType, List<String>) -> Unit
) {
    var selectedType by remember(initialType, types) { mutableStateOf(initialType ?: types.firstOrNull()) }
    var prefix by remember { mutableStateOf("Kamar") }
    var count by remember { mutableStateOf("5") }
    val total = count.toIntOrNull()?.coerceIn(1, 100) ?: 1
    val names = (1..total).map { "$prefix $it".trim() }
    Scaffold(
        topBar = { AppTopBar("Tambah banyak kamar", onBack = onBack) },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Button(
                    onClick = { selectedType?.let { onCreate(it, names) } },
                    enabled = !saving && selectedType != null && names.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().padding(Screen.edge).height(Dimens.ButtonHeight),
                    shape = MaterialTheme.shapes.large
                ) { Text(if (saving) "Menyimpan…" else "Generate kamar") }
            }
        }
    ) { pad ->
        Column(
            Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(horizontal = Screen.edge, vertical = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            ChoiceDropdown("Tipe kamar *", selectedType, types, { it.name }, enabled = types.isNotEmpty()) { selectedType = it }
            if (types.isEmpty()) {
                Text(
                    "Belum ada tipe kamar yang punya harga di lokasi ini. Buat tipe kamar lalu tambahkan harganya dulu (alur: tipe kamar → harga → nomor kamar).",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            AppTextField(prefix, { prefix = it }, label = "Awalan nama", modifier = Modifier.fillMaxWidth())
            AppTextField(count, { count = it.filter(Char::isDigit).take(3) }, label = "Jumlah (1–100) *", modifier = Modifier.fillMaxWidth(), keyboardType = KeyboardType.Number)
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    Text("Akan dibuat", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(names.take(100).joinToString(", "), style = MaterialTheme.typography.bodyMedium)
                }
            }
            progress?.let { LinearProgressIndicator(progress = { it / 100f }, modifier = Modifier.fillMaxWidth()); Text("Memproses $it%") }
        }
    }
}
