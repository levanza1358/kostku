package com.miraya.kostku.ui.common

import androidx.compose.runtime.Composable
import com.miraya.kostku.KostkuApi
import com.miraya.kostku.RemoteSnapshot

/**
 * Penyalur dialog untuk TAB UTAMA. Tiap kelompok fitur ditangani berkasnya sendiri:
 *  - [LocationDialogRoute] : tambah/edit/hapus lokasi
 *  - [PaymentDialogRoute]  : catat/edit/hapus pemasukan
 *  - [ExpenseDialogRoute]  : catat/edit/hapus pengeluaran
 */
@Composable
internal fun MainDialogs(
    dialog: String?,
    data: RemoteSnapshot?,
    saving: Boolean,
    mutate: (String, suspend () -> Unit) -> Unit,
    dismiss: () -> Unit,
    api: KostkuApi
) {
    when {
        dialog == "location-add" || dialog?.startsWith("location-delete-") == true ->
            LocationDialogRoute(dialog, data, saving, mutate, dismiss, api)

        dialog == "payment" || dialog?.startsWith("payment-") == true ->
            PaymentDialogRoute(dialog, data, saving, mutate, dismiss, api)

        dialog == "expense" || dialog?.startsWith("expense-") == true ->
            ExpenseDialogRoute(dialog, data, saving, mutate, dismiss, api)
    }
}
