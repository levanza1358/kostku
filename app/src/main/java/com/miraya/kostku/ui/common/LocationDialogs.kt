package com.miraya.kostku.ui.common

import androidx.compose.runtime.Composable
import com.miraya.kostku.KostkuApi
import com.miraya.kostku.RemoteSnapshot
import com.miraya.kostku.createProperty
import com.miraya.kostku.deleteProperty
import com.miraya.kostku.features.location.LocationEditScreen
import com.miraya.kostku.media.MediaPipeline
import com.miraya.kostku.updateProperty
import com.miraya.kostku.uploadPhoto

/** Dialog tambah/hapus lokasi (dipanggil dari [MainDialogs]). */
@Composable
internal fun LocationDialogRoute(
    dialog: String?,
    data: RemoteSnapshot?,
    saving: Boolean,
    mutate: (String, suspend () -> Unit) -> Unit,
    dismiss: () -> Unit,
    api: KostkuApi
) {
    when {
        // Tambah lokasi: layar penuh (logo + data lokasi + utilitas), sama seperti Edit lokasi.
        dialog == "location-add" -> LocationEditScreen(
            location = null,
            saving = saving,
            onBack = dismiss
        ) { name, branch, address, description, token, pdam, logo, removeLogo ->
            mutate("Lokasi berhasil ditambahkan.") {
                val id = api.createProperty(name, branch, address, description).getOrThrow().optString("id")
                if (token != null || pdam != null) runCatching { api.updateProperty(id, name, branch, address, description, token, pdam) }
                if (logo != null && id.isNotBlank()) runCatching { api.uploadPhoto("property", id, logo) }
                MediaPipeline.cleanup(logo)
                dismiss()
            }
        }

        dialog?.startsWith("location-delete-") == true -> {
            val deleteId = dialog.removePrefix("location-delete-")
            val target = data?.locations?.firstOrNull { it.id == deleteId }
            ConfirmDialog(
                "Hapus lokasi?",
                "Lokasi \"${target?.name ?: "ini"}\" beserta kamar di dalamnya akan dihapus. Tindakan ini tidak dapat dibatalkan.",
                saving,
                dismiss,
                confirmLabel = "Hapus",
                destructive = true
            ) {
                mutate("Lokasi berhasil dihapus.") {
                    api.deleteProperty(deleteId).getOrThrow()
                    dismiss()
                }
            }
        }
    }
}
