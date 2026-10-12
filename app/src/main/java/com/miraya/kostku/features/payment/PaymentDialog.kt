package com.miraya.kostku.features.payment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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

/** Opsi metode & status pembayaran (dipakai dialog pemasukan dan pengeluaran). */
internal val methodOptions = listOf(
    "Tunai" to "cash",
    "Transfer" to "transfer",
    "Lainnya" to "other"
)
internal val statusOptions = listOf(
    "Lunas" to "paid",
    "Sebagian" to "partial",
    "Belum" to "pending",
    "Terlambat" to "late"
)

@Composable
internal fun PaymentDialog(
    tenants: List<RemoteTenant>,
    categories: List<String>,
    saving: Boolean,
    dismiss: () -> Unit,
    existing: RemotePayment? = null,
    save: (String, String, Int, String, String, String?, File?) -> Unit
) {
    var tenant by remember { mutableStateOf(tenants.firstOrNull { it.id == existing?.tenantId } ?: tenants.firstOrNull()) }
    var periodDate by remember {
        mutableStateOf(
            runCatching { LocalDate.parse(existing?.period?.take(10) ?: LocalDate.now().withDayOfMonth(1).toString()) }
                .getOrDefault(LocalDate.now().withDayOfMonth(1))
        )
    }
    var amount by remember { mutableStateOf(existing?.amount?.toString() ?: "") }
    var method by remember { mutableStateOf(existing?.method ?: "cash") }
    var status by remember { mutableStateOf(existing?.status?.takeIf { it != "cancelled" } ?: "paid") }
    var category by remember { mutableStateOf(existing?.category) }
    var photo by remember { mutableStateOf<File?>(null) }
    val amountValue = amount.toIntOrNull() ?: 0
    // "-- Tanpa kategori --" (null) selalu tersedia, diikuti kategori pemasukan yang diatur di Pengaturan.
    val categoryOptions: List<String?> = buildList { add(null); addAll(categories) }

    AlertDialog(
        onDismissRequest = dismiss,
        modifier = Modifier.fillMaxWidth(0.94f),
        properties = DialogProperties(usePlatformDefaultWidth = false),
        title = { Text(if (existing == null) "Catat pemasukan" else "Edit pemasukan") },
        text = {
            Column(
                Modifier.heightIn(max = 520.dp),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                ChoiceDropdown("Penghuni", tenant, tenants, { it.name }) { tenant = it }
                DateField("Periode / tanggal", periodDate, { periodDate = it })
                AppTextField(
                    value = amount,
                    onValueChange = { amount = it.filter(Char::isDigit) },
                    label = "Jumlah (Rupiah)",
                    keyboardType = KeyboardType.Number,
                    prefix = { Text("Rp ") },
                    isError = amount.isNotBlank() && amountValue <= 0,
                    supportingText = if (amount.isNotBlank() && amountValue <= 0) "Jumlah harus lebih dari nol." else null,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    Box(Modifier.weight(1f)) { ChoiceDropdown("Kategori", category, categoryOptions, { it ?: "— Tanpa kategori —" }) { category = it } }
                    Box(Modifier.weight(1f)) { ChoiceDropdown("Metode", methodOptions.firstOrNull { it.second == method }, methodOptions, { it.first }) { method = it.second } }
                }
                Text("Status", style = MaterialTheme.typography.labelLarge)
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    statusOptions.forEach { option ->
                        FilterChip(selected = status == option.second, onClick = { status = option.second }, label = { Text(option.first) })
                    }
                }
                PhotoField("Bukti pembayaran", onFile = { photo = it })
            }
        },
        confirmButton = {
            SaveDialogButton(
                label = "Simpan",
                onClick = {
                    val target = tenant
                    if (target != null && amountValue > 0) {
                        save(target.id, periodDate.toString(), amountValue, method, status, category, photo)
                    }
                },
                saving = saving,
                enabled = tenant != null && amountValue > 0
            )
        },
        dismissButton = {
            TextButton(onClick = dismiss, enabled = !saving) { Text("Batal") }
        }
    )
}
