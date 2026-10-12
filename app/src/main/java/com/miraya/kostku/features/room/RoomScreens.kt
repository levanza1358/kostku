package com.miraya.kostku.features.room

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.miraya.kostku.*
import com.miraya.kostku.diag.AppLog
import com.miraya.kostku.media.PhotoField
import com.miraya.kostku.ui.common.AppTextField
import com.miraya.kostku.ui.common.AppTopBar
import com.miraya.kostku.ui.common.ChoiceDropdown
import com.miraya.kostku.ui.common.ConfirmDialog
import com.miraya.kostku.ui.common.FormSection
import com.miraya.kostku.ui.common.SaveButton
import com.miraya.kostku.ui.common.SelectableChip
import com.miraya.kostku.ui.theme.Dimens
import com.miraya.kostku.ui.theme.Screen
import com.miraya.kostku.ui.theme.Spacing
import java.io.File

private const val SCREEN = "RoomEditScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RoomEditScreen(room: RemoteRoom, types: List<RemoteRoomType>, saving: Boolean, onBack: () -> Unit, onSave: (String, String, String, String?, String?, File?, Boolean) -> Unit, onDelete: () -> Unit, compact: Boolean = false) {
    var name by remember { mutableStateOf(room.name) }
    var type by remember { mutableStateOf(types.firstOrNull { it.id == room.roomTypeId }) }
    var status by remember { mutableStateOf(room.status) }
    var token by remember { mutableStateOf(room.tokenListrik.orEmpty()) }
    var pdam by remember { mutableStateOf(room.pdamNumber.orEmpty()) }
    var photo by remember { mutableStateOf<File?>(null) }
    var removePhoto by remember { mutableStateOf(false) }
    var validationMessage by remember { mutableStateOf<String?>(null) }
    var confirmDelete by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val statuses = listOf(
        "Tersedia" to "vacant",
        "Terisi" to "occupied",
        "Kotor" to "dirty",
        "Maintenance" to "maintenance"
    )

    // Layar dibuka.
    LaunchedEffect(room.id) {
        AppLog.i("[$SCREEN] dibuka - roomId=${room.id}, roomTypeId=${room.roomTypeId ?: "null"}, jumlahTipe=${types.size}")
    }

    // Tipe kamar berubah -> catat pilihan.
    LaunchedEffect(type?.id) {
        AppLog.decision(SCREEN, "tipe kamar dipilih", "roomTypeId=${type?.id ?: "belum dipilih"} (${type?.name ?: "-"})")
    }

    // Catat apakah tombol Simpan aktif dan alasannya.
    val canSave = type != null
    LaunchedEffect(canSave, saving) {
        if (saving) {
            AppLog.decision(SCREEN, "Simpan nonaktif", "sedang menyimpan (saving=true)")
        } else if (!canSave) {
            AppLog.decision(SCREEN, "Simpan nonaktif", "tipe kamar belum dipilih")
        } else {
            AppLog.decision(SCREEN, "Simpan aktif", "siap disimpan")
        }
    }

    fun attemptSave() {
        val selectedType = type
        if (selectedType == null) {
            // FIX BUG: sebelumnya tombol nonaktif tanpa umpan balik apa pun.
            // Sekarang pengguna melihat pesan jelas (Toast, tampil di halaman ini juga).
            AppLog.w("[$SCREEN] Simpan ditekan, tetapi form belum lengkap - tipe kamar belum dipilih")
            validationMessage = "Pilih tipe kamar terlebih dahulu."
            val shown = "Pilih tipe kamar terlebih dahulu."
            AppLog.snackbar(shown)
            Toast.makeText(context, shown, Toast.LENGTH_SHORT).show()
            return
        }
        validationMessage = null
        val resolvedStatus = status
        if (name.isBlank()) {
            AppLog.w("[$SCREEN] Simpan ditekan, tetapi nama kamar kosong")
            validationMessage = "Nama kamar belum diisi."
            val shown = "Nama kamar belum diisi."
            AppLog.snackbar(shown)
            Toast.makeText(context, shown, Toast.LENGTH_SHORT).show()
            return
        }
        AppLog.actionStart(
            "Simpan kamar",
            "roomId=${room.id}, nama=\"$name\", roomTypeId=${selectedType.id}, status=$resolvedStatus, " +
                "adaFotoBaru=${photo != null}, hapusFoto=$removePhoto"
        )
        onSave(name, selectedType.id, resolvedStatus, token.trim().ifBlank { null }, pdam.trim().ifBlank { null }, photo, removePhoto)
    }

    Scaffold(
        topBar = { AppTopBar(if (compact) "Edit nomor kamar" else "Edit kamar", onBack = onBack) },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                if (compact) {
                    Box(Modifier.fillMaxWidth().padding(horizontal = Screen.edge, vertical = Spacing.md)) {
                        SaveButton(
                            label = "Simpan",
                            onClick = ::attemptSave,
                            saving = saving,
                            modifier = Modifier.fillMaxWidth(),
                            height = Dimens.ButtonHeight
                        )
                    }
                } else Row(
                    Modifier.fillMaxWidth().padding(horizontal = Screen.edge, vertical = Spacing.md),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    // Hapus selalu lewat konfirmasi agar tidak terhapus karena salah tekan.
                    OutlinedButton(
                        onClick = { confirmDelete = true },
                        enabled = !saving,
                        modifier = Modifier.weight(1f).height(Dimens.ButtonHeight),
                        shape = MaterialTheme.shapes.large
                    ) { Text("Hapus") }
                    // Tombol tetap bisa diklik agar pengguna mendapat umpan balik saat form belum lengkap.
                    SaveButton(
                        label = "Simpan",
                        onClick = ::attemptSave,
                        saving = saving,
                        modifier = Modifier.weight(1f),
                        height = Dimens.ButtonHeight
                    )
                }
            }
        }
    ) { pad ->
        Column(
            Modifier.padding(pad).verticalScroll(rememberScrollState()).padding(horizontal = Screen.edge, vertical = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            AppTextField(
                value = name,
                onValueChange = { name = it; validationMessage = null },
                label = if (compact) "Nomor kamar *" else "Nama kamar *",
                modifier = Modifier.fillMaxWidth()
            )
            ChoiceDropdown("Tipe kamar *", type, types, { it.name }) { type = it; validationMessage = null }
            if (compact) {
                FormSection("Utilitas (opsional)")
                AppTextField(token, { token = it }, label = "Nomor token listrik", modifier = Modifier.fillMaxWidth())
                AppTextField(pdam, { pdam = it }, label = "Nomor PDAM", modifier = Modifier.fillMaxWidth())
                Text("Kosongkan jika kamar ini tidak punya token/PDAM.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                FormSection("Status")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    statuses.forEach { (label, value) ->
                        SelectableChip(label, selected = status == value, onClick = { status = value })
                    }
                }
                FormSection("Utilitas (opsional)")
                AppTextField(token, { token = it }, label = "Nomor token listrik", modifier = Modifier.fillMaxWidth())
                AppTextField(pdam, { pdam = it }, label = "Nomor PDAM", modifier = Modifier.fillMaxWidth())
                Text("Kosongkan jika kamar ini tidak punya token/PDAM.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                FormSection("Foto kamar")
                PhotoField("", room.photoUrl, onFile = { photo = it; removePhoto = false }, onRemove = { photo = null; removePhoto = true })
            }
            validationMessage?.let { message ->
                Text(
                    message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
    if (confirmDelete) ConfirmDialog(
        title = "Hapus kamar?",
        message = "Kamar \"${room.name}\" akan dihapus permanen. Tindakan ini tidak dapat dibatalkan.",
        saving = saving,
        dismiss = { confirmDelete = false }
    ) {
        confirmDelete = false
        onDelete()
    }
}
