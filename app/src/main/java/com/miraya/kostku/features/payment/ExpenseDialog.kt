package com.miraya.kostku.features.payment

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.miraya.kostku.*
import com.miraya.kostku.media.PhotoField
import com.miraya.kostku.ui.common.*
import com.miraya.kostku.ui.theme.Spacing
import java.io.File
import java.time.LocalDate

@Composable
internal fun ExpenseDialog(
    tenants: List<RemoteTenant>,
    locations: List<RemoteLocation>,
    categories: List<String>,
    saving: Boolean,
    dismiss: () -> Unit,
    existing: RemoteExpense? = null,
    save: (String, String?, String?, String?, Int, String, String, String?, File?) -> Unit
) {
    var kind by remember { mutableStateOf(existing?.kind ?: "operational") }
    var tenant by remember { mutableStateOf(tenants.firstOrNull { it.id == existing?.tenantId }) }
    var location by remember { mutableStateOf(locations.firstOrNull { it.id == existing?.propertyId }) }
    var date by remember {
        mutableStateOf(runCatching { LocalDate.parse(existing?.spentAt?.take(10) ?: LocalDate.now().toString()) }.getOrDefault(LocalDate.now()))
    }
    var amount by remember { mutableStateOf(existing?.amount?.takeIf { it != 0 }?.toString() ?: "") }
    var method by remember { mutableStateOf(existing?.method ?: "cash") }
    var notes by remember { mutableStateOf(existing?.notes.orEmpty()) }
    var photo by remember { mutableStateOf<File?>(null) }
    val amountValue = amount.toIntOrNull() ?: 0
    val kindOptions = listOf("Operasional" to "operational", "Refund ke penghuni" to "refund")
    // Kategori dari Pengaturan; nilai lama tetap ditampilkan walau sudah tak ada di daftar.
    val categoryOptions = remember(categories, existing?.category) {
        buildList { addAll(categories); existing?.category?.let { if (it !in categories) add(it) } }.ifEmpty { listOf("Lainnya") }
    }
    var category by remember { mutableStateOf(existing?.category ?: categoryOptions.first()) }
    val canSave = !saving && amountValue > 0 && (kind != "refund" || tenant != null)

    AlertDialog(
        onDismissRequest = dismiss,
        modifier = Modifier.fillMaxWidth(0.94f),
        properties = DialogProperties(usePlatformDefaultWidth = false),
        title = { Text(if (existing == null) "Catat pengeluaran" else "Edit pengeluaran") },
        text = {
            Column(Modifier.heightIn(max = 520.dp), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                ChoiceDropdown("Jenis pengeluaran *", kindOptions.firstOrNull { it.second == kind }, kindOptions, { it.first }) { kind = it.second }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    Box(Modifier.weight(1f)) {
                        if (kind == "refund") {
                            if (tenants.isEmpty()) {
                                Text("Belum ada penghuni untuk direfund.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                            } else {
                                ChoiceDropdown("Penghuni", tenant, tenants, { it.name }) { tenant = it }
                            }
                        } else {
                            ChoiceDropdown("Kategori", category, categoryOptions, { it }) { category = it }
                        }
                    }
                    Box(Modifier.weight(1f)) { ChoiceDropdown("Metode", methodOptions.firstOrNull { it.second == method }, methodOptions, { it.first }) { method = it.second } }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    Box(Modifier.weight(1f)) {
                        AppTextField(
                            value = amount,
                            onValueChange = { amount = it.filter(Char::isDigit) },
                            label = "Jumlah (Rp) *",
                            keyboardType = KeyboardType.Number,
                            prefix = { Text("Rp ") },
                            isError = amount.isNotBlank() && amountValue <= 0,
                            supportingText = if (amount.isNotBlank() && amountValue <= 0) "Jumlah harus lebih dari nol." else null,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (locations.isNotEmpty()) {
                        Box(Modifier.weight(1f)) { ChoiceDropdown("Lokasi", location, locations, { it.name }) { location = it } }
                    }
                }
                AppTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = "Catatan (opsional)",
                    singleLine = false,
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                DateField("Tanggal", date, { date = it })
                PhotoField("Bukti (opsional)", onFile = { photo = it })
            }
        },
        confirmButton = {
            SaveDialogButton(
                label = "Simpan",
                onClick = {
                    save(
                        kind,
                        if (kind == "operational") category else null,
                        if (kind == "refund") tenant?.id else null,
                        location?.id,
                        amountValue,
                        "${date} 00:00:00",
                        method,
                        notes.ifBlank { null },
                        photo
                    )
                },
                saving = saving,
                enabled = canSave
            )
        },
        dismissButton = { TextButton(onClick = dismiss, enabled = !saving) { Text("Batal") } }
    )
}
