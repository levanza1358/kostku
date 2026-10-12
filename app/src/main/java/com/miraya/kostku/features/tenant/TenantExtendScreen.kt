package com.miraya.kostku.features.tenant

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.miraya.kostku.*
import com.miraya.kostku.ui.common.ChoiceDropdown
import com.miraya.kostku.ui.common.FormSection
import com.miraya.kostku.ui.common.KeyValueRow
import com.miraya.kostku.ui.common.SelectChip
import com.miraya.kostku.ui.theme.Spacing
import java.time.LocalDate

/**
 * 3.10 Perpanjang kos — pilih durasi & harga (auto-fetch dari server) lalu perbarui tanggal
 * keluar. Tampilan mengikuti mock 3.10: jumlah penghuni → durasi → kartu "tanggal keluar
 * baru" → status Lunas/Belum lunas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TenantExtendScreen(
    tenant: RemoteTenant,
    currentTypeId: String?,
    categories: List<String>,
    api: KostkuApi,
    saving: Boolean,
    onBack: () -> Unit,
    onSave: (moveOut: LocalDate, monthlyRent: Int, amountPaid: Int, status: String, method: String, category: String?, discountType: String?, discountValue: Int) -> Unit
) {
    var typePrices by remember { mutableStateOf<List<RemoteRoomTypePrice>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    LaunchedEffect(currentTypeId) {
        if (currentTypeId == null) { typePrices = emptyList(); loading = false } else {
            loading = true; typePrices = api.roomTypePrices(currentTypeId).getOrDefault(emptyList()); loading = false
        }
    }
    // Jumlah orang (occupancy) lebih dulu; durasi menyesuaikan orang yang dipilih.
    val occupancies = typePrices.map { it.occupancy }.distinct().sorted()
    var occupancyOverride by remember { mutableStateOf<Int?>(null) }
    val occupancy = occupancyOverride?.takeIf { it in occupancies } ?: occupancies.firstOrNull()
    val allDurations = typePrices.filter { it.occupancy == occupancy }
        .map { it.durationValue to it.durationUnit }.distinct()
        .sortedWith(compareBy({ listOf("day", "week", "month", "year").indexOf(it.second).coerceAtLeast(0) }, { it.first }))
    var durationOverride by remember(occupancy) { mutableStateOf<Pair<Int, String>?>(null) }
    val duration = durationOverride?.takeIf { it in allDurations } ?: allDurations.firstOrNull()
    val priceOptions = typePrices.filter {
        it.durationValue == duration?.first && it.durationUnit == duration?.second && it.occupancy == occupancy
    }
    // Mock 3.10 hanya menampilkan Jumlah penghuni + Durasi; harga terpilih otomatis (opsi pertama).
    val price = priceOptions.firstOrNull()

    // Tanggal keluar saat ini → ditambah durasi yang dipilih, menumpuk bila sewa masih berjalan.
    val currentEnd = tenant.moveOut?.take(10)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
    val stillRunning = currentEnd != null && currentEnd.isAfter(LocalDate.now())
    val base = if (stillRunning) currentEnd!! else LocalDate.now()
    val newEnd = duration?.let { (v, u) -> addDuration(base, v, u) }
    val newRent = price?.amount ?: tenant.rent
    val extraDays = newEnd?.let { java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), it) }

    // Pembayaran perpanjangan: hanya status Lunas / Belum lunas (mock 3.10). Metode & kategori
    // memakai nilai bawaan agar pencatatan tetap lengkap tanpa menambah bidang di layar.
    val subtotal = newRent
    var lunas by remember { mutableStateOf(true) }
    val amountPaid = if (lunas) subtotal else 0
    val method = "cash"
    val category = categories.firstOrNull { it.equals("Sewa", true) }

    fun attemptExtend() {
        val end = newEnd ?: return
        val status = if (lunas) "paid" else "pending"
        onSave(end, newRent, amountPaid, status, method, category, null, 0)
    }

    TenantActionScaffold("Perpanjang kos", onBack, "Perpanjang", saving, {
        if (newEnd != null) attemptExtend()
    }, ctaIcon = Icons.Default.EventRepeat) {
        Card(Modifier.fillMaxWidth()) {
            KeyValueRow("Tanggal keluar saat ini", currentEnd?.let { formatDate(it) } ?: "Tidak diketahui")
        }
        when {
            loading -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                Text("Memuat durasi & harga…", style = MaterialTheme.typography.bodySmall)
            }
            typePrices.isEmpty() -> Text("Belum ada harga untuk kamar ini.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            else -> {
                ChoiceDropdown(
                    "Jumlah penghuni",
                    occupancy,
                    occupancies,
                    { "$it orang" }
                ) { occupancyOverride = it; durationOverride = null }
                ChoiceDropdown("Durasi perpanjangan", duration, allDurations, { durationSummary(it.first, it.second) }) {
                    durationOverride = it
                }
            }
        }
        Card(
            Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Text("TANGGAL KELUAR BARU", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(
                    newEnd?.let { formatDate(it) } ?: "Pilih durasi dulu",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                if (extraDays != null) Text("Sisa $extraDays hari dari hari ini", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
        FormSection("Pembayaran perpanjangan")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Harga sewa baru", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(rupiah(newRent), style = MaterialTheme.typography.titleMedium)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            SelectChip("Lunas", lunas, Color(0xFF6EE7B7), { lunas = true })
            SelectChip("Belum lunas", !lunas, MaterialTheme.colorScheme.onSurfaceVariant, { lunas = false })
        }
        if (lunas) {
            Text("Lunas — dibayar penuh ${rupiah(subtotal)}.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        } else {
            Text("Belum lunas — ${rupiah(subtotal)} dicatat sebagai tagihan.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
