package com.miraya.kostku.features.payment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.miraya.kostku.*
import com.miraya.kostku.media.ListPhotoThumb
import com.miraya.kostku.ui.common.*
import com.miraya.kostku.ui.theme.Screen
import com.miraya.kostku.ui.theme.Spacing

/** Tab "Pemasukan": daftar pembayaran dari penghuni + ringkasan total (mock 5.2). */
@Composable
internal fun IncomeTab(data: RemoteSnapshot?, saving: Boolean, open: (String) -> Unit) {
    val all = data?.payments.orEmpty()
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(vertical = Spacing.sm)) {
            NetCard("Pemasukan", rupiah(all.filter { it.status != "cancelled" }.sumOf { it.amount }), Icons.Default.TrendingUp, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
        }
        when {
            all.isEmpty() -> EmptyState(
                "Belum ada pemasukan",
                "Tap tombol + untuk mencatat pembayaran manual dari penghuni.",
                Icons.Default.Payments
            )
            else -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                contentPadding = PaddingValues(top = Spacing.xs, bottom = Screen.bottom)
            ) {
                items(all, key = { it.id }) { p ->
                    RowActionMenu(
                        items = listOf(
                            RowAction("Kwitansi") { open("receipt-${p.id}") },
                            RowAction("Edit") { open("payment-edit-${p.id}") },
                            RowAction("Hapus", destructive = true) { open("payment-delete-${p.id}") }
                        ),
                        enabled = !saving
                    ) { toggle ->
                        Card(Modifier.fillMaxWidth()) {
                            ListItem(
                                headlineContent = { Text(rupiah(p.amount), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary) },
                                supportingContent = { Text(listOfNotNull("Pemasukan", p.category, paymentStatusLabel(p.status), formatDate(p.period)).joinToString(" • ")) },
                                leadingContent = { ListPhotoThumb(p.photoUrl, "Bukti pembayaran", Icons.Default.TrendingUp) },
                                trailingContent = { OverflowButton(toggle, "Opsi pemasukan") }
                            )
                        }
                    }
                }
            }
        }
    }
}
