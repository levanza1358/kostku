package com.miraya.kostku.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.miraya.kostku.ui.common.AppTopBar
import com.miraya.kostku.ui.common.SettingsSection
import com.miraya.kostku.ui.theme.Screen
import com.miraya.kostku.ui.theme.Spacing

/**
 * Layar informasi: "Tentang Aplikasi" sebagai halaman utama, dengan baris menuju
 * "Syarat & Ketentuan", "Kebijakan Privasi", dan "Bantuan" (mock 6.5). Navigasi antar
 * sub-halaman ditangani internal; tombol kembali menutup sub-halaman lebih dulu.
 */
@Composable
internal fun InfoScreen(page: String, onBack: () -> Unit) {
    var sub by remember(page) { mutableStateOf<String?>(null) }
    val current = sub ?: page
    val title = when (current) {
        "help" -> "Bantuan"
        "terms" -> "Syarat & Ketentuan"
        "privacy" -> "Kebijakan Privasi"
        else -> "Tentang Aplikasi"
    }
    Scaffold(
        topBar = { AppTopBar(title, onBack = { if (sub != null) sub = null else onBack() }) }
    ) { pad ->
        Column(
            Modifier.padding(pad).verticalScroll(rememberScrollState()).padding(horizontal = Screen.edge, vertical = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            when (current) {
                "help" -> Text(HELP, style = MaterialTheme.typography.bodyLarge)
                "terms" -> Text(TERMS, style = MaterialTheme.typography.bodyLarge)
                "privacy" -> Text(PRIVACY, style = MaterialTheme.typography.bodyLarge)
                else -> {
                    Column(
                        Modifier.fillMaxWidth().padding(vertical = Spacing.lg),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        Surface(shape = MaterialTheme.shapes.extraLarge, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(72.dp)) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.LocationOn, null, Modifier.size(36.dp), tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Text("Kostku", style = MaterialTheme.typography.titleLarge)
                        Text("Versi 1.0", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Card(Modifier.fillMaxWidth()) {
                        Text(
                            "Aplikasi pengelolaan kos: penghuni, kamar, pembayaran, dan pengeluaran dalam satu tempat.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(Spacing.lg)
                        )
                    }
                    SettingsSection("Syarat & Ketentuan", icon = Icons.Default.Description) { sub = "terms" }
                    SettingsSection("Kebijakan Privasi", icon = Icons.Default.PrivacyTip) { sub = "privacy" }
                    SettingsSection("Bantuan", icon = Icons.Default.HelpOutline) { sub = "help" }
                }
            }
        }
    }
}

private val HELP = """
    Panduan singkat:
    • Penghuni — tambah penghuni, pilih lokasi, tipe kamar, kamar, dan harga sewa.
    • Lokasi — kelola lokasi, tipe kamar, harga, dan kamar.
    • Pembayaran — catat pembayaran manual, filter status, serta batalkan bila perlu.
    • Foto — pilih dari Galeri atau Kamera, crop, lalu simpan.
    • Tandai penghuni keluar — data otomatis diarsipkan ke riwayat Penghuni keluar, lalu pilih status kamar (Tersedia / Kotor / Maintenance).
""".trimIndent()

private val TERMS = """
    Dengan menggunakan Kostku, Anda bertanggung jawab atas kebenaran data kos, kamar, penghuni,
    dan pembayaran yang Anda masukkan. Aplikasi disediakan "sebagaimana adanya" untuk membantu
    pengelolaan kos; pastikan pencadangan data penting secara berkala.
""".trimIndent()

private val PRIVACY = """
    Kostku menyimpan data kos Anda di server dan hanya menampilkan data milik akun Anda.
    Token sesi disimpan di perangkat dan tidak dibagikan. Foto dikompres lalu diunggah ke
    penyimpanan server.
""".trimIndent()
