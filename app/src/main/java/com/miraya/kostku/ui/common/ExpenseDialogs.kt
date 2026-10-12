package com.miraya.kostku.ui.common

import androidx.compose.runtime.Composable
import com.miraya.kostku.KostkuApi
import com.miraya.kostku.RemoteSnapshot
import com.miraya.kostku.createExpense
import com.miraya.kostku.deleteExpense
import com.miraya.kostku.features.payment.ExpenseDialog
import com.miraya.kostku.updateExpense
import com.miraya.kostku.uploadPhoto

/** Nama kategori pengeluaran (untuk dropdown di dialog pengeluaran). */
private fun expenseCategories(data: RemoteSnapshot?): List<String> =
    data?.categories.orEmpty().filter { it.kind == "expense" }.map { it.name }

/** Dialog catat/edit/hapus pengeluaran (dipanggil dari [MainDialogs]). */
@Composable
internal fun ExpenseDialogRoute(
    dialog: String?,
    data: RemoteSnapshot?,
    saving: Boolean,
    mutate: (String, suspend () -> Unit) -> Unit,
    dismiss: () -> Unit,
    api: KostkuApi
) {
    when {
        dialog == "expense" -> ExpenseDialog(
            data?.tenants.orEmpty(),
            data?.locations.orEmpty(),
            expenseCategories(data),
            saving,
            dismiss
        ) { kind, category, tenantId, propertyId, amount, spentAt, method, notes, photo ->
            mutate("Pengeluaran berhasil dicatat.") {
                val id = api.createExpense(kind, category, tenantId, propertyId, amount, spentAt, method, notes).getOrThrow().optString("id")
                if (photo != null && id.isNotBlank()) {
                    runCatching { api.uploadPhoto("expense", id, photo) }
                }
                dismiss()
            }
        }

        dialog?.startsWith("expense-edit-") == true -> {
            val editId = dialog.removePrefix("expense-edit-")
            val target = data?.expenses?.firstOrNull { it.id == editId }
            if (target != null) ExpenseDialog(
                data?.tenants.orEmpty(),
                data?.locations.orEmpty(),
                expenseCategories(data),
                saving,
                dismiss,
                existing = target
            ) { kind, category, tenantId, propertyId, amount, spentAt, method, notes, photo ->
                mutate("Pengeluaran berhasil diperbarui.") {
                    api.updateExpense(target.id, kind, category, tenantId, propertyId, amount, spentAt, method, notes).getOrThrow()
                    if (photo != null) runCatching { api.uploadPhoto("expense", target.id, photo) }
                    dismiss()
                }
            }
        }

        dialog?.startsWith("expense-delete-") == true -> {
            val deleteId = dialog.removePrefix("expense-delete-")
            ConfirmDialog(
                "Hapus pengeluaran?",
                "Catatan pengeluaran ini akan dihapus permanen.",
                saving,
                dismiss
            ) {
                mutate("Pengeluaran berhasil dihapus.") {
                    api.deleteExpense(deleteId).getOrThrow()
                    dismiss()
                }
            }
        }
    }
}
