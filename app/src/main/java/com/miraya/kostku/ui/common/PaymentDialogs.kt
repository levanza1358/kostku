package com.miraya.kostku.ui.common

import androidx.compose.runtime.Composable
import com.miraya.kostku.KostkuApi
import com.miraya.kostku.RemoteSnapshot
import com.miraya.kostku.createPayment
import com.miraya.kostku.deletePayment
import com.miraya.kostku.features.payment.PaymentDialog
import com.miraya.kostku.updatePayment
import com.miraya.kostku.uploadPhoto
import java.time.LocalDateTime

/** Nama kategori pemasukan (untuk dropdown di dialog pembayaran). */
private fun incomeCategories(data: RemoteSnapshot?): List<String> =
    data?.categories.orEmpty().filter { it.kind == "income" }.map { it.name }

/** Dialog catat/edit/hapus pemasukan (dipanggil dari [MainDialogs]). */
@Composable
internal fun PaymentDialogRoute(
    dialog: String?,
    data: RemoteSnapshot?,
    saving: Boolean,
    mutate: (String, suspend () -> Unit) -> Unit,
    dismiss: () -> Unit,
    api: KostkuApi
) {
    when {
        dialog == "payment" -> PaymentDialog(
            data?.tenants.orEmpty(),
            incomeCategories(data),
            saving,
            dismiss
        ) { tenantId, period, amount, method, status, category, photo ->
            mutate("Pembayaran berhasil dicatat.") {
                val id = api.createPayment(
                    tenantId,
                    period,
                    amount,
                    LocalDateTime.now().toString(),
                    method,
                    status,
                    category
                ).getOrThrow().optString("id")
                if (photo != null && id.isNotBlank()) {
                    runCatching { api.uploadPhoto("payment", id, photo) }
                        .onFailure { /* pesan ditangani pemanggil */ }
                }
                dismiss()
            }
        }

        dialog?.startsWith("payment-edit-") == true -> {
            val editId = dialog.removePrefix("payment-edit-")
            val target = data?.payments?.firstOrNull { it.id == editId }
            if (target != null) PaymentDialog(
                data?.tenants.orEmpty(),
                incomeCategories(data),
                saving,
                dismiss,
                existing = target
            ) { tenantId, period, amount, method, status, category, photo ->
                mutate("Pembayaran berhasil diperbarui.") {
                    api.updatePayment(
                        target.id,
                        period,
                        amount,
                        LocalDateTime.now().toString(),
                        method,
                        status,
                        null,
                        null,
                        category
                    ).getOrThrow()
                    if (photo != null) {
                        runCatching { api.uploadPhoto("payment", target.id, photo) }
                    }
                    dismiss()
                }
            }
        }

        dialog?.startsWith("payment-delete-") == true -> {
            val deleteId = dialog.removePrefix("payment-delete-")
            ConfirmDialog(
                "Hapus pembayaran?",
                "Catatan pemasukan ini akan dihapus permanen.",
                saving,
                dismiss,
                confirmLabel = "Hapus"
            ) {
                mutate("Pembayaran berhasil dihapus.") {
                    api.deletePayment(deleteId).getOrThrow()
                    dismiss()
                }
            }
        }
    }
}
