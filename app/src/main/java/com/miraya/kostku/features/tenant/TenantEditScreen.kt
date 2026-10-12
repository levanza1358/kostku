package com.miraya.kostku.features.tenant

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.miraya.kostku.*
import com.miraya.kostku.media.PhotoField
import com.miraya.kostku.ui.common.AppTextField
import com.miraya.kostku.ui.common.FormSection
import java.io.File

/**
 * 3.8 Edit data penghuni — nama, nomor HP, dan foto (tata letak datar seperti mock,
 * tanpa kartu pembungkus).
 */
@Composable
internal fun TenantEditScreen(
    tenant: RemoteTenant,
    saving: Boolean,
    onBack: () -> Unit,
    onSave: (name: String, phone: String, photo: File?, removePhoto: Boolean) -> Unit
) {
    var name by remember { mutableStateOf(tenant.name) }
    var phone by remember { mutableStateOf(tenant.phone) }
    var photo by remember { mutableStateOf<File?>(null) }
    var removePhoto by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf<String?>(null) }

    TenantActionScaffold("Edit data penghuni", onBack, "Simpan perubahan", saving, {
        nameError = if (name.isBlank()) "Nama penghuni wajib diisi." else null
        if (nameError == null) onSave(name.trim(), phone.trim(), photo, removePhoto)
    }) {
        FormSection("Data penghuni")
        AppTextField(
            value = name,
            onValueChange = { name = it; nameError = null },
            label = "Nama lengkap *",
            modifier = Modifier.fillMaxWidth(),
            isError = nameError != null,
            supportingText = nameError
        )
        AppTextField(
            value = phone,
            onValueChange = { phone = it },
            label = "Nomor HP",
            modifier = Modifier.fillMaxWidth(),
            keyboardType = KeyboardType.Phone
        )
        FormSection("Foto penghuni")
        PhotoField("", currentUrl = if (removePhoto) null else tenant.photoUrl, onFile = { photo = it; removePhoto = false }, onRemove = { photo = null; removePhoto = true })
        Text(
            "Kosongkan lalu simpan untuk menghapus foto.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
