package com.miraya.kostku.features.roomtype

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.miraya.kostku.RemoteLocation
import com.miraya.kostku.RemoteRoomType
import com.miraya.kostku.RemoteRoomTypePrice
import com.miraya.kostku.durationUnitLabel
import com.miraya.kostku.rupiah
import com.miraya.kostku.ui.common.AppTextField
import com.miraya.kostku.ui.common.AppTopBar
import com.miraya.kostku.ui.common.ChoiceDropdown
import com.miraya.kostku.ui.common.ConfirmDialog
import com.miraya.kostku.ui.common.SaveDialogButton
import com.miraya.kostku.ui.theme.FieldShape
import com.miraya.kostku.ui.theme.Screen
import com.miraya.kostku.ui.theme.Spacing

@Composable
internal fun RoomTypePriceScreen(location: RemoteLocation, type: RemoteRoomType, prices: List<RemoteRoomTypePrice>, saving: Boolean, onBack: () -> Unit, onAddRooms: () -> Unit, onSave: (RemoteRoomTypePrice?, Int, Int, String, Int) -> Unit, onDelete: (RemoteRoomTypePrice) -> Unit) {
    var edit by remember { mutableStateOf<RemoteRoomTypePrice?>(null) }
    var duplicateMessage by remember { mutableStateOf<String?>(null) }
    var deleteTarget by remember { mutableStateOf<RemoteRoomTypePrice?>(null) }
    val sorted = prices.sortedWith(compareBy<RemoteRoomTypePrice> { durationRank(it.durationUnit) }.thenBy { it.durationValue }.thenBy { it.occupancy }.thenBy { it.amount })
    Scaffold(topBar = { AppTopBar("Harga — ${type.name}", onBack = onBack) }) { pad ->
        LazyColumn(
            Modifier.fillMaxSize().padding(pad).padding(horizontal = Screen.edge, vertical = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            item { Text(location.name, style = MaterialTheme.typography.labelLarge); Text("Harga sewa", style = MaterialTheme.typography.titleLarge) }
            item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) { Button({ edit = RemoteRoomTypePrice("", type.id, 1, 1, "month", 0) }, Modifier.weight(1f), shape = MaterialTheme.shapes.large) { Text("Tambah harga") }; OutlinedButton(onAddRooms, Modifier.weight(1f), shape = MaterialTheme.shapes.large) { Text("Tambah kamar") } } }
            if (sorted.isEmpty()) item { Text("Belum ada harga untuk tipe ini") }
            items(sorted, key = { it.id }) { p ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(Spacing.md), horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(rupiah(p.amount), style = MaterialTheme.typography.titleMedium)
                            Text("${p.occupancy} orang • ${p.durationValue} ${durationUnitLabel(p.durationUnit)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton({ edit = p }) { Icon(Icons.Default.Edit, "Edit harga") }
                        IconButton({ deleteTarget = p }) { Icon(Icons.Default.Delete, "Hapus harga", tint = MaterialTheme.colorScheme.error) }
                    }
                }
            }
        }
    }
    edit?.let { current -> PriceDialog(current, saving, { edit = null }) { occupancy, duration, unit, amount ->
        if (prices.any { it.id != current.id && it.occupancy == occupancy && it.durationValue == duration && it.durationUnit == unit }) duplicateMessage = "Durasi tersebut sudah ada"
        else { onSave(if (current.id.isBlank()) null else current, occupancy, duration, unit, amount); edit = null }
    } }
    duplicateMessage?.let { message -> AlertDialog(onDismissRequest = { duplicateMessage = null }, title = { Text("Data sudah ada") }, text = { Text(message) }, confirmButton = { Button({ duplicateMessage = null }) { Text("Mengerti") } }) }
    deleteTarget?.let { target -> ConfirmDialog(
        title = "Hapus harga?",
        message = "Harga ${rupiah(target.amount)} (${target.occupancy} orang • ${target.durationValue} ${durationUnitLabel(target.durationUnit)}) akan dihapus. Tindakan ini tidak dapat dibatalkan.",
        saving = saving,
        dismiss = { deleteTarget = null }
    ) { val t = target; deleteTarget = null; onDelete(t) } }
}

@Composable private fun PriceDialog(existing: RemoteRoomTypePrice, saving: Boolean, dismiss: () -> Unit, save: (Int, Int, String, Int) -> Unit) {
    var occupancy by remember(existing) { mutableStateOf(existing.occupancy.coerceAtLeast(1).toString()) }
    var duration by remember(existing) { mutableStateOf(existing.durationValue.coerceAtLeast(1).toString()) }
    var unit by remember(existing) { mutableStateOf(existing.durationUnit) }
    var amount by remember(existing) { mutableStateOf(if (existing.amount == 0) "" else existing.amount.toString()) }
    val occupancyValue = occupancy.toIntOrNull()?.coerceIn(1, 10) ?: 1
    val durationValue = duration.toIntOrNull()?.coerceIn(1, 24) ?: 1
    AlertDialog(onDismissRequest = dismiss, title = { Text(if (existing.id.isBlank()) "Tambah harga" else "Edit harga") }, text = { Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        NumberAdjuster("Jumlah orang", occupancy, 1..10) { occupancy = it }; NumberAdjuster("Durasi", duration, 1..24) { duration = it }
        ChoiceDropdown("Satuan", durationUnits.firstOrNull { it.second == unit }, durationUnits, { it.first }) { unit = it.second }
        AppTextField(
            value = amount,
            onValueChange = { amount = it.filter(Char::isDigit) },
            label = "Harga *",
            prefix = { Text("Rp ") },
            keyboardType = KeyboardType.Number,
            modifier = Modifier.fillMaxWidth()
        )
    } }, confirmButton = { SaveDialogButton(label = "Simpan harga", onClick = { amount.toIntOrNull()?.let { if (it >= 0) save(occupancyValue, durationValue, unit, it) } }, saving = saving, enabled = amount.toIntOrNull()?.let { it >= 0 } == true) }, dismissButton = { TextButton(onClick = dismiss, enabled = !saving) { Text("Batal") } })
}

@Composable private fun NumberAdjuster(label: String, value: String, range: IntRange, onValueChange: (String) -> Unit) {
    val number = value.toIntOrNull()?.coerceIn(range) ?: range.first
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton({ onValueChange((number - 1).coerceAtLeast(range.first).toString()) }, enabled = number > range.first) { Icon(Icons.Default.Remove, "Kurangi $label") }
            OutlinedTextField(
                value = value,
                onValueChange = { onValueChange(it.filter(Char::isDigit).take(3)) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = FieldShape
            )
            IconButton({ onValueChange((number + 1).coerceAtMost(range.last).toString()) }, enabled = number < range.last) { Icon(Icons.Default.Add, "Tambah $label") }
        }
        Text("${range.first}–${range.last}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
