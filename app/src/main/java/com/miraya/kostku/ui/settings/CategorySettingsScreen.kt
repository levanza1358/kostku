package com.miraya.kostku.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.miraya.kostku.KostkuApi
import com.miraya.kostku.RemoteCategory
import com.miraya.kostku.createCategory
import com.miraya.kostku.deleteCategory
import com.miraya.kostku.renameCategory
import com.miraya.kostku.ui.common.AppTab
import com.miraya.kostku.ui.common.AppTabRow
import com.miraya.kostku.ui.common.AppTextField
import com.miraya.kostku.ui.common.AppTopBar
import com.miraya.kostku.ui.common.ConfirmDialog
import com.miraya.kostku.ui.common.EmptyState
import com.miraya.kostku.ui.common.SaveDialogButton
import com.miraya.kostku.ui.common.SegmentedPill
import com.miraya.kostku.ui.common.TabContent
import com.miraya.kostku.ui.theme.Screen
import com.miraya.kostku.ui.theme.Spacing

/**
 * Pengaturan kategori pemasukan/pengeluaran. Daftar bersifat global per akun dan
 * disimpan di server; pemilik bebas menambah, mengganti nama, dan menghapus.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CategorySettingsScreen(
    categories: List<RemoteCategory>,
    saving: Boolean,
    api: KostkuApi,
    mutate: (String, suspend () -> Unit) -> Unit,
    onBack: () -> Unit
) {
    var tab by remember { mutableIntStateOf(0) }
    val kind = if (tab == 0) "income" else "expense"
    var editing by remember { mutableStateOf<RemoteCategory?>(null) }
    var adding by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<RemoteCategory?>(null) }

    Scaffold(
        topBar = { AppTopBar("Kategori", onBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(onClick = { if (!saving) adding = true }) {
                Icon(Icons.Default.Add, "Tambah kategori")
            }
        }
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad)) {
            val kinds = listOf("Pemasukan", "Pengeluaran")
            SegmentedPill(kinds, kinds[tab], Modifier.padding(horizontal = Screen.edge)) { tab = kinds.indexOf(it) }
            val shown = categories.filter { it.kind == kind }
            TabContent(tab, Modifier.weight(1f)) {
                Column(Modifier.fillMaxSize().padding(horizontal = Screen.edge)) {
                    Spacer(Modifier.height(Spacing.sm))
                    if (shown.isEmpty()) {
                        EmptyState(
                            if (kind == "income") "Belum ada kategori pemasukan" else "Belum ada kategori pengeluaran",
                            "Tap tombol + untuk menambah kategori.",
                            Icons.Default.Category
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                            contentPadding = PaddingValues(bottom = Screen.fab)
                        ) {
                            items(shown, key = { it.id }) { c ->
                                Card(Modifier.fillMaxWidth()) {
                                    Row(
                                        Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(c.name, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                                        IconButton({ if (!saving) editing = c }, enabled = !saving) { Icon(Icons.Default.Edit, "Edit kategori") }
                                        IconButton({ if (!saving) deleteTarget = c }, enabled = !saving) { Icon(Icons.Default.Delete, "Hapus kategori", tint = MaterialTheme.colorScheme.error) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (adding) CategoryFormDialog(null, saving, { adding = false }) { name ->
        mutate("Kategori berhasil ditambahkan.") {
            api.createCategory(kind, name).getOrThrow()
            adding = false
        }
    }
    editing?.let { target ->
        CategoryFormDialog(target, saving, { editing = null }) { name ->
            mutate("Kategori berhasil diperbarui.") {
                api.renameCategory(target.id, name).getOrThrow()
                editing = null
            }
        }
    }
    deleteTarget?.let { target ->
        ConfirmDialog(
            "Hapus kategori?",
            "Kategori \"${target.name}\" akan dihapus. Catatan pemasukan/pengeluaran yang sudah ada tidak berubah.",
            saving,
            { deleteTarget = null }
        ) {
            mutate("Kategori berhasil dihapus.") {
                api.deleteCategory(target.id).getOrThrow()
                deleteTarget = null
            }
        }
    }
}

@Composable
private fun CategoryFormDialog(
    existing: RemoteCategory?,
    saving: Boolean,
    dismiss: () -> Unit,
    save: (String) -> Unit
) {
    var name by remember { mutableStateOf(existing?.name.orEmpty()) }
    AlertDialog(
        onDismissRequest = dismiss,
        title = { Text(if (existing == null) "Tambah kategori" else "Edit kategori") },
        text = {
            AppTextField(
                value = name,
                onValueChange = { name = it },
                label = "Nama kategori",
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            SaveDialogButton(
                label = "Simpan",
                onClick = { if (name.isNotBlank()) save(name.trim()) },
                saving = saving,
                enabled = name.isNotBlank()
            )
        },
        dismissButton = { TextButton(onClick = dismiss, enabled = !saving) { Text("Batal") } }
    )
}
