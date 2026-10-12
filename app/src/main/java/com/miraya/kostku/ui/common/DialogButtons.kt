package com.miraya.kostku.ui.common

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.miraya.kostku.ui.theme.Spacing

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    saving: Boolean,
    dismiss: () -> Unit,
    confirmLabel: String = "Konfirmasi",
    destructive: Boolean = false,
    confirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = dismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = { SaveDialogButton(confirmLabel, confirm, saving, destructive = destructive) },
        dismissButton = { TextButton(onClick = dismiss, enabled = !saving) { Text("Batal") } }
    )
}

/**
 * Tombol simpan ringkas untuk slot `confirmButton` AlertDialog. Saat [saving] menampilkan
 * spinner + "Menyimpan…" agar pengguna tahu proses sedang berjalan. Bila [destructive],
 * tombol memakai warna error (mis. tombol "Hapus" pada dialog konfirmasi).
 */
@Composable
fun SaveDialogButton(
    label: String = "Simpan",
    onClick: () -> Unit,
    saving: Boolean,
    enabled: Boolean = true,
    savingLabel: String = "Menyimpan…",
    destructive: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = enabled && !saving,
        shape = MaterialTheme.shapes.large,
        colors = if (destructive) ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError
        ) else ButtonDefaults.buttonColors()
    ) {
        if (saving) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = LocalContentColor.current
            )
            Spacer(Modifier.width(Spacing.sm))
            Text(savingLabel)
        } else {
            Text(label)
        }
    }
}
