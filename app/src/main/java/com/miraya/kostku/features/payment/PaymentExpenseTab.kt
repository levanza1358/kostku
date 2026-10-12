package com.miraya.kostku.features.payment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.miraya.kostku.*
import com.miraya.kostku.media.ListPhotoThumb
import com.miraya.kostku.ui.common.*
import com.miraya.kostku.ui.theme.Screen
import com.miraya.kostku.ui.theme.Spacing

/** Tab "Pengeluaran": biaya operasional & refund ke penghuni + ringkasan total (mock 5.3). */
@Composable
internal fun ExpenseTab(data: RemoteSnapshot?, saving: Boolean, open: (String) -> Unit) {
    val all = data?.expenses.orEmpty()
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(vertical = Spacing.sm)) {
            NetCard("Pengeluaran", rupiah(all.sumOf { it.amount }), Icons.Default.TrendingDown, MaterialTheme.colorScheme.error, Modifier.weight(1f))
        }
        when {
            all.isEmpty() -> EmptyState(
                "Belum ada pengeluaran",
                "Tap tombol + untuk mencatat biaya operasional atau refund ke penghuni.",
                Icons.Default.TrendingDown
            )
            else -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                contentPadding = PaddingValues(top = Spacing.xs, bottom = Screen.bottom)
            ) {
                items(all, key = { it.id }) { e ->
                    val tenantName = data?.tenants?.firstOrNull { it.id == e.tenantId }?.name
                    val detail = if (e.kind == "refund") tenantName ?: "Penghuni" else e.category ?: "Operasional"
                    RowActionMenu(
                        items = listOf(
                            RowAction("Edit") { open("expense-edit-${e.id}") },
                            RowAction("Hapus", destructive = true) { open("expense-delete-${e.id}") }
                        ),
                        enabled = !saving
                    ) { toggle ->
                        Card(Modifier.fillMaxWidth()) {
                            ListItem(
                                headlineContent = { Text(rupiah(e.amount), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error) },
                                supportingContent = { Text(listOfNotNull("Pengeluaran", detail, paymentMethodLabel(e.method), formatDate(e.spentAt)).joinToString(" • ")) },
                                leadingContent = { ListPhotoThumb(e.photoUrl, "Bukti pengeluaran", Icons.Default.TrendingDown) },
                                trailingContent = { OverflowButton(toggle, "Opsi pengeluaran") }
                            )
                        }
                    }
                }
            }
        }
    }
}
