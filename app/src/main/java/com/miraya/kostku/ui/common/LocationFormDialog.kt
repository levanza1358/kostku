package com.miraya.kostku.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.miraya.kostku.RemoteLocation
import com.miraya.kostku.ui.theme.Spacing

@Composable
internal fun LocationFormDialog(
    existing: RemoteLocation?,
    saving: Boolean,
    dismiss: () -> Unit,
    save: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(existing?.name.orEmpty()) }
    var branch by remember { mutableStateOf(existing?.branch.orEmpty()) }
    var address by remember { mutableStateOf(existing?.address.orEmpty()) }
    var description by remember { mutableStateOf(existing?.description.orEmpty()) }
    val title = if (existing == null) "Tambah lokasi" else "Edit lokasi"
    AlertDialog(
        onDismissRequest = dismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                AppTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Nama lokasi",
                    modifier = Modifier.fillMaxWidth()
                )
                AppTextField(
                    value = branch,
                    onValueChange = { branch = it },
                    label = "Cabang",
                    modifier = Modifier.fillMaxWidth()
                )
                AppTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = "Alamat",
                    singleLine = false,
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                AppTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Deskripsi",
                    singleLine = false,
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            SaveDialogButton(
                label = "Simpan",
                onClick = {
                    if (name.isNotBlank() && address.isNotBlank()) {
                        save(name.trim(), branch.trim(), address.trim(), description.trim())
                    }
                },
                saving = saving,
                enabled = name.isNotBlank() && address.isNotBlank()
            )
        },
        dismissButton = {
            TextButton(onClick = dismiss, enabled = !saving) { Text("Batal") }
        }
    )
}
