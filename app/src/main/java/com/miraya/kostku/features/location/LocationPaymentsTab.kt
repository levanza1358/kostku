package com.miraya.kostku.features.location

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.miraya.kostku.*
import com.miraya.kostku.media.ListPhotoThumb
import com.miraya.kostku.ui.common.*
import com.miraya.kostku.ui.theme.Screen
import com.miraya.kostku.ui.theme.Spacing

/** Buku besar pembayaran lokasi: pemasukan (dari penghuni) + pengeluaran (biaya lokasi). */
@Composable
internal fun LocationPaymentsTab(payments: List<RemotePayment>, expenses: List<RemoteExpense>, data: RemoteSnapshot, filter: Int, onFilter: (Int) -> Unit) {
    val tenantName = { id: String -> data.tenants.firstOrNull { it.id == id }?.name ?: "Penghuni" }
    val rows = buildList {
        payments.forEach { p ->
            add(PaymentRow("p-${p.id}", p.period, p.amount, true, listOfNotNull("Pemasukan", p.category, tenantName(p.tenantId), formatDate(p.period)).joinToString(" • "), p.photoUrl))
        }
        expenses.forEach { e ->
            val detail = if (e.kind == "refund") tenantName(e.tenantId ?: "") else e.category ?: "Operasional"
            add(PaymentRow("e-${e.id}", e.spentAt, e.amount, false, listOf("Pengeluaran", detail, formatDate(e.spentAt)).joinToString(" • "), e.photoUrl))
        }
    }.sortedByDescending { it.dateKey }
    val visible = when (filter) {
        1 -> rows.filter { it.income }
        2 -> rows.filter { !it.income }
        else -> rows
    }
    Column(Modifier.fillMaxSize()) {
        SegmentedPillCompact(listOf("Semua", "Masuk", "Keluar"), listOf("Semua", "Masuk", "Keluar")[filter], Modifier.padding(horizontal = Screen.edge)) {
            onFilter(listOf("Semua", "Masuk", "Keluar").indexOf(it))
        }
        if (visible.isEmpty()) {
            Text("Belum ada catatan pembayaran untuk lokasi ini.", Modifier.padding(Screen.edge))
        } else {
            LazyColumn(
                Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(top = Spacing.md, bottom = Screen.bottom),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                items(visible, key = { it.id }) { row ->
                    val accent = if (row.income) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    Card(Modifier.fillMaxWidth().padding(horizontal = Screen.edge)) {
                        ListItem(
                            headlineContent = { Text(rupiah(row.amount), style = MaterialTheme.typography.titleMedium, color = accent) },
                            supportingContent = { Text(row.subtitle) },
                            leadingContent = { ListPhotoThumb(row.photo, "Bukti catatan", if (row.income) Icons.Default.TrendingUp else Icons.Default.TrendingDown, size = 44.dp) }
                        )
                    }
                }
            }
        }
    }
}

private data class PaymentRow(val id: String, val dateKey: String, val amount: Int, val income: Boolean, val subtitle: String, val photo: String?)
