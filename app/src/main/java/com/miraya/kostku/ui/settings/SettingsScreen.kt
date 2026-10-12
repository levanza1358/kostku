package com.miraya.kostku.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.miraya.kostku.*
import com.miraya.kostku.ui.common.AppTopBar
import com.miraya.kostku.ui.common.SectionTitle
import com.miraya.kostku.ui.common.SettingsSection
import com.miraya.kostku.ui.theme.Dimens
import com.miraya.kostku.ui.theme.Screen
import com.miraya.kostku.ui.theme.Spacing
import com.miraya.kostku.ui.theme.accentLabel

@Composable
internal fun SettingsTab(p: UserProfile, data: RemoteSnapshot?, mode: String, accent: String, open: (String) -> Unit, logout: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        AppTopBar("Pengaturan")
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = Screen.edge, vertical = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Card { ListItem(leadingContent = { Icon(Icons.Default.AccountCircle, null, Modifier.size(40.dp)) }, headlineContent = { Text(p.name.ifBlank { "Pengguna Kostku" }, style = MaterialTheme.typography.titleMedium) }, supportingContent = { Text(p.email) }) }
            SettingsSection("Ringkasan", "${data?.locations?.size ?: 0} lokasi • ${data?.rooms?.size ?: 0} kamar • ${data?.tenants?.size ?: 0} penghuni", Icons.Default.Dashboard)

            SectionTitle("Keuangan")
            SettingsSection("Kategori", "Atur kategori pemasukan & pengeluaran", Icons.Default.Sell) { open("categories") }

            SectionTitle("Riwayat")
            SettingsSection("Penghuni keluar", "Daftar penghuni yang sudah keluar, per bulan", Icons.Default.History) { open("movedOut") }

            SectionTitle("Tampilan")
            SettingsSection("Tema", themeSummary(mode, accent), Icons.Default.Palette) { open("theme") }

            SectionTitle("Tentang")
            SettingsSection("Tentang Aplikasi", "Kostku — kelola kos lebih mudah", Icons.Default.Info) { open("about") }
            SettingsSection("Bantuan", "Panduan penggunaan aplikasi", Icons.Default.HelpOutline) { open("help") }

            Spacer(Modifier.height(Spacing.sm))
            Button(
                onClick = logout,
                modifier = Modifier.fillMaxWidth().height(Dimens.ButtonHeight),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError),
                shape = MaterialTheme.shapes.large
            ) { Icon(Icons.Default.Logout, null); Spacer(Modifier.width(Spacing.sm)); Text("Keluar") }
        }
    }
}

private fun themeSummary(mode: String, accent: String): String {
    val modeLabel = when (mode) { "dark" -> "Gelap"; "black" -> "Hitam"; else -> "Terang" }
    return "$modeLabel • Aksen ${accentLabel(accent)}"
}
