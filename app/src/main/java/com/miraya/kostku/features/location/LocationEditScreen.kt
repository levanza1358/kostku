package com.miraya.kostku.features.location

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.miraya.kostku.*
import com.miraya.kostku.ui.common.AppTextField
import com.miraya.kostku.ui.common.AppTopBar
import com.miraya.kostku.ui.common.SaveButton
import com.miraya.kostku.ui.common.SectionTitle
import com.miraya.kostku.ui.theme.Screen
import com.miraya.kostku.ui.theme.Spacing

/**
 * Layar form lokasi (tambah **dan** edit): logo (dengan fallback ikon rumah), data lokasi,
 * dan utilitas (nomor token listrik & PDAM) — semuanya opsional kecuali nama & alamat.
 * [location] null berarti **tambah lokasi** (judul "Tambah lokasi"). Menyimpan lewat [onSave];
 * logo diunggah/dihapus terpisah oleh pemanggil.
 */
@Composable
internal fun LocationEditScreen(
    location: RemoteLocation?,
    saving: Boolean,
    onBack: () -> Unit,
    onSave: (name: String, branch: String, address: String, description: String, tokenListrik: String?, pdamNumber: String?, logo: java.io.File?, removeLogo: Boolean) -> Unit
) {
    val key = location?.id ?: "new"
    var name by remember(key) { mutableStateOf(location?.name.orEmpty()) }
    var branch by remember(key) { mutableStateOf(location?.branch.orEmpty()) }
    var address by remember(key) { mutableStateOf(location?.address.orEmpty()) }
    var token by remember(key) { mutableStateOf(location?.tokenListrik.orEmpty()) }
    var pdam by remember(key) { mutableStateOf(location?.pdamNumber.orEmpty()) }
    var logo by remember(key) { mutableStateOf<java.io.File?>(null) }
    var removeLogo by remember(key) { mutableStateOf(false) }
    val canSave = name.isNotBlank() && address.isNotBlank()
    val adding = location == null

    Scaffold(
        topBar = { AppTopBar(if (adding) "Tambah lokasi" else "Edit lokasi", onBack = onBack) },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Box(Modifier.fillMaxWidth().padding(horizontal = Screen.edge, vertical = Spacing.md)) {
                    SaveButton(
                        label = if (adding) "Simpan" else "Simpan perubahan",
                        onClick = {
                            if (canSave) onSave(name.trim(), branch.trim(), address.trim(), "", token.trim().ifBlank { null }, pdam.trim().ifBlank { null }, logo, removeLogo)
                        },
                        saving = saving,
                        enabled = canSave,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    ) { pad ->
        Column(
            Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(horizontal = Screen.edge, vertical = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    com.miraya.kostku.media.LogoField(
                        currentUrl = if (removeLogo) null else location?.logoUrl,
                        onFile = { logo = it; if (it != null) removeLogo = false },
                        onRemove = { logo = null; removeLogo = true }
                    )
                }
            }
            SectionTitle("Data lokasi")
            AppTextField(name, { name = it }, label = "Nama", modifier = Modifier.fillMaxWidth())
            AppTextField(branch, { branch = it }, label = "Cabang", modifier = Modifier.fillMaxWidth())
            AppTextField(address, { address = it }, label = "Alamat", singleLine = false, minLines = 2, modifier = Modifier.fillMaxWidth())
            SectionTitle("Utilitas (opsional)")
            AppTextField(token, { token = it }, label = "Nomor token listrik (opsional)", modifier = Modifier.fillMaxWidth())
            AppTextField(pdam, { pdam = it }, label = "Nomor PDAM (opsional)", modifier = Modifier.fillMaxWidth())
            Text("Kosongkan jika tidak ada. Nilai utilitas tidak diwarisi ke kamar.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
