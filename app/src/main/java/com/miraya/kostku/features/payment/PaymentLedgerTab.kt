package com.miraya.kostku.features.payment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.miraya.kostku.*
import com.miraya.kostku.media.ListPhotoThumb
import com.miraya.kostku.ui.common.*
import com.miraya.kostku.ui.theme.Screen
import com.miraya.kostku.ui.theme.Spacing

/** Satu baris buku besar gabungan (pemasukan atau pengeluaran) untuk tab "Semua". */
private data class LedgerEntry(
    val id: String,
    val dateKey: String,
    val amount: Int,
    val income: Boolean,
    val title: String,
    val subtitle: String,
    val editRoute: String,
    val deleteRoute: String,
    val photoUrl: String? = null
)

/** Tab "Semua": pemasukan + pengeluaran digabung, diurutkan terbaru lebih dulu. */
@Composable
internal fun LedgerTab(data: RemoteSnapshot?, saving: Boolean, open: (String) -> Unit) {
    val payments = data?.payments.orEmpty()
    val expenses = data?.expenses.orEmpty()
    val tenantName = { id: String? -> data?.tenants?.firstOrNull { it.id == id }?.name }
    val entries = buildList {
        payments.forEach { p ->
            add(
                LedgerEntry(
                    id = "p-${p.id}", dateKey = p.period, amount = p.amount, income = true,
                    title = rupiah(p.amount),
                    subtitle = listOfNotNull("Pemasukan", p.category, paymentStatusLabel(p.status), formatDate(p.period)).joinToString(" • "),
                    editRoute = "payment-edit-${p.id}",
                    deleteRoute = "payment-delete-${p.id}",
                    photoUrl = p.photoUrl
                )
            )
        }
        expenses.forEach { e ->
            val detail = if (e.kind == "refund") tenantName(e.tenantId) ?: "Penghuni" else e.category ?: "Operasional"
            add(
                LedgerEntry(
                    id = "e-${e.id}", dateKey = e.spentAt, amount = e.amount, income = false,
                    title = rupiah(e.amount),
                    subtitle = listOf("Pengeluaran", detail, paymentMethodLabel(e.method), formatDate(e.spentAt)).joinToString(" • "),
                    editRoute = "expense-edit-${e.id}",
                    deleteRoute = "expense-delete-${e.id}",
                    photoUrl = e.photoUrl
                )
            )
        }
    }.sortedByDescending { it.dateKey }

    val incomeTotal = payments.filter { it.status != "cancelled" }.sumOf { it.amount }
    val expenseTotal = expenses.sumOf { it.amount }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(vertical = Spacing.sm), horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            NetCard("Pemasukan", rupiah(incomeTotal), Icons.Default.TrendingUp, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
            NetCard("Pengeluaran", rupiah(expenseTotal), Icons.Default.TrendingDown, MaterialTheme.colorScheme.error, Modifier.weight(1f))
        }
        when {
            entries.isEmpty() -> EmptyState(
                "Belum ada catatan",
                "Catat pemasukan dari penghuni atau pengeluaran kos.",
                Icons.Default.Payments
            )
            else -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                contentPadding = PaddingValues(top = Spacing.xs, bottom = Screen.bottom)
            ) {
                items(entries, key = { it.id }) { entry ->
                    LedgerCard(entry, saving, open)
                }
            }
        }
    }
}

@Composable
private fun LedgerCard(entry: LedgerEntry, saving: Boolean, open: (String) -> Unit) {
    val accent = if (entry.income) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    RowActionMenu(
        items = buildList {
            if (entry.income) add(RowAction("Kwitansi") { open("receipt-${entry.id.removePrefix("p-")}") })
            add(RowAction("Edit") { open(entry.editRoute) })
            add(RowAction("Hapus", destructive = true) { open(entry.deleteRoute) })
        },
        enabled = !saving
    ) { toggle ->
        Card(Modifier.fillMaxWidth()) {
            ListItem(
                headlineContent = { Text(entry.title, style = MaterialTheme.typography.titleMedium, color = accent) },
                supportingContent = { Text(entry.subtitle) },
                leadingContent = { ListPhotoThumb(entry.photoUrl, "Bukti catatan", if (entry.income) Icons.Default.TrendingUp else Icons.Default.TrendingDown) },
                trailingContent = { OverflowButton(toggle, "Opsi catatan") }
            )
        }
    }
}

@Composable
internal fun NetCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, accent: androidx.compose.ui.graphics.Color, modifier: Modifier) {
    Card(modifier) {
        Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(18.dp))
                Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(value, style = MaterialTheme.typography.headlineSmall, color = accent)
        }
    }
}
