package com.miraya.kostku.features.tenant

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.miraya.kostku.*
import com.miraya.kostku.media.PhotoField
import com.miraya.kostku.ui.common.*
import com.miraya.kostku.ui.theme.Screen
import com.miraya.kostku.ui.theme.Spacing
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddTenantScreen(initialLocation: RemoteLocation?, locations: List<RemoteLocation>, rooms: List<RemoteRoom>, types: List<RemoteRoomType>, categories: List<String>, api: KostkuApi, saving: Boolean, onBack: () -> Unit, onSave: (String, String, String, String, String, String, String, String, Int, Int, String, String?, Int, File?, String, String?) -> Unit) {
    val scope = rememberCoroutineScope()
    // Identitas
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    // Penempatan (lokasi & kamar dari snapshot server; durasi & harga di-fetch dari server)
    var location by remember { mutableStateOf(initialLocation ?: locations.firstOrNull()) }
    val locationTypes = types.filter { it.propertyId == location?.id }
    var type by remember(location?.id) { mutableStateOf(locationTypes.firstOrNull()) }
    // Nomor kamar: auto-fetch dari server setiap kali lokasi berubah.
    var locationRooms by remember { mutableStateOf<List<RemoteRoom>>(emptyList()) }
    var roomsLoading by remember { mutableStateOf(false) }
    LaunchedEffect(location?.id) {
        val lid = location?.id
        if (lid == null) {
            locationRooms = emptyList(); roomsLoading = false
        } else {
            roomsLoading = true
            locationRooms = api.propertyRooms(lid).getOrDefault(rooms.filter { it.propertyId == lid })
            roomsLoading = false
        }
    }
    // Durasi & harga: auto-fetch dari server setiap kali tipe kamar berubah.
    var typePrices by remember { mutableStateOf<List<RemoteRoomTypePrice>>(emptyList()) }
    var pricesLoading by remember { mutableStateOf(false) }
    LaunchedEffect(type?.id) {
        val tid = type?.id
        if (tid == null) {
            typePrices = emptyList(); pricesLoading = false
        } else {
            pricesLoading = true
            typePrices = api.roomTypePrices(tid).getOrDefault(emptyList())
            pricesLoading = false
        }
    }
    val durations = typePrices
        .map { it.durationValue to it.durationUnit }
        .distinct()
        .sortedWith(compareBy({ listOf("day", "week", "month", "year").indexOf(it.second).coerceAtLeast(0) }, { it.first }))
    // Pilihan disimpan sebagai "override" + fallback ke opsi pertama, sehingga otomatis
    // ikut berubah saat data server selesai dimuat (tidak ada nilai basi).
    var durationOverride by remember(type?.id) { mutableStateOf<Pair<Int, String>?>(null) }
    val duration = durationOverride?.takeIf { it in durations } ?: durations.firstOrNull()
    val priceOptions = typePrices.filter { it.durationValue == duration?.first && it.durationUnit == duration?.second }
    var priceOverride by remember(type?.id, duration) { mutableStateOf<RemoteRoomTypePrice?>(null) }
    val price = priceOverride?.takeIf { it in priceOptions } ?: priceOptions.firstOrNull()
    val availableRooms = locationRooms.filter { it.status == "vacant" && it.roomTypeId == type?.id }
    var roomOverride by remember(type?.id) { mutableStateOf<RemoteRoom?>(null) }
    val room = roomOverride?.takeIf { it in availableRooms } ?: availableRooms.firstOrNull()
    // Masa sewa (tanggal masuk default hari ini, keluar mengikuti durasi)
    var moveIn by remember { mutableStateOf(LocalDate.now()) }
    val moveOut = duration?.let { (v, u) -> addDuration(moveIn, v, u) }
    // Biaya & pembayaran
    val subtotal = price?.amount ?: 0
    var discountType by remember { mutableStateOf("amount") }
    var discountText by remember { mutableStateOf("") }
    val discountRaw = discountText.toIntOrNull() ?: 0
    val discount = when {
        subtotal <= 0 -> 0
        discountType == "percent" -> subtotal * discountRaw / 100
        else -> discountRaw
    }.coerceIn(0, subtotal)
    val total = (subtotal - discount).coerceAtLeast(0)
    var lunas by remember { mutableStateOf(true) }
    var amountText by remember { mutableStateOf("") }
    val amountPaid = (amountText.toIntOrNull() ?: 0).coerceIn(0, total)
    // Metode & kategori pemasukan untuk baris pembayaran awal (pola sama dengan dialog pembayaran).
    val methodOptions = listOf("Tunai" to "cash", "Transfer" to "transfer", "Lainnya" to "other")
    var method by remember { mutableStateOf("cash") }
    val categoryOptions: List<String?> = remember(categories) { buildList { add(null); addAll(categories) } }
    var category by remember(categories) { mutableStateOf(categories.firstOrNull { it.equals("Sewa", true) }) }
    // Foto
    var photo by remember { mutableStateOf<File?>(null) }
    // Error & fokus
    var nameError by remember { mutableStateOf<String?>(null) }
    var placementError by remember { mutableStateOf<String?>(null) }
    var amountError by remember { mutableStateOf<String?>(null) }
    val nameRequester = remember { BringIntoViewRequester() }
    val placementRequester = remember { BringIntoViewRequester() }
    val amountRequester = remember { BringIntoViewRequester() }
    val nameFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { nameFocus.requestFocus() } }

    fun attemptSave() {
        nameError = if (name.isBlank()) "Nama penghuni wajib diisi." else null
        placementError = when {
            location == null -> "Pilih lokasi kos."
            type == null -> "Pilih tipe kamar."
            typePrices.isEmpty() -> "Belum ada harga untuk tipe kamar ini."
            price == null -> "Pilih harga sewa."
            room == null -> if (availableRooms.isEmpty()) "Belum ada nomor kamar tersedia untuk tipe ini. Buat dulu: tipe kamar → harga → nomor kamar." else "Pilih nomor kamar."
            else -> null
        }
        amountError = when {
            lunas -> null
            amountPaid > total -> "Jumlah bayar melebihi total (${rupiah(total)})."
            else -> null
        }
        when {
            nameError != null -> scope.launch { nameRequester.bringIntoView() }
            placementError != null -> scope.launch { placementRequester.bringIntoView() }
            amountError != null -> scope.launch { amountRequester.bringIntoView() }
        }
        val l = location; val t = type; val r = room; val p = price
        if (nameError == null && placementError == null && amountError == null && l != null && t != null && r != null && p != null) {
            val paid = if (lunas) 0 else amountPaid
            val status = if (lunas) "paid" else if (amountPaid > 0) "partial" else "pending"
            onSave(
                name.trim(), phone.trim(), l.id, t.id, r.id, p.id,
                "${moveIn} 00:00:00", "${addDuration(moveIn, p.durationValue, p.durationUnit)} 00:00:00",
                p.amount, paid, status,
                if (discount > 0) discountType else null, if (discount > 0) discountRaw else 0, photo,
                method, category
            )
        }
    }

    Scaffold(
        topBar = { AppTopBar("Tambah penghuni", onBack = onBack) }
    ) { pad ->
        Column(
            Modifier.padding(pad).verticalScroll(rememberScrollState()).padding(horizontal = Screen.edge, vertical = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            FormSection("Data penghuni")
            val nameErr = nameError
            AppTextField(
                value = name,
                onValueChange = { name = it; nameError = null },
                label = "Nama lengkap *",
                modifier = Modifier.fillMaxWidth().bringIntoViewRequester(nameRequester).focusRequester(nameFocus),
                isError = nameErr != null,
                supportingText = nameErr
            )
            AppTextField(
                value = phone,
                onValueChange = { phone = it },
                label = "Nomor HP",
                modifier = Modifier.fillMaxWidth(),
                keyboardType = KeyboardType.Phone
            )

            FormSection("Penempatan")
            Column(Modifier.bringIntoViewRequester(placementRequester)) {
                // Lokasi kos selalu berupa dropdown. Bila hanya ada satu lokasi, kolom
                // dinonaktifkan agar tetap tampil rapi tanpa bisa diubah.
                if (locations.isEmpty()) {
                    Text("Belum ada lokasi kos.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                } else {
                    ChoiceDropdown(
                        "Lokasi kos *",
                        location,
                        locations,
                        { it.name },
                        enabled = locations.size > 1
                    ) { selected ->
                        location = selected
                        type = types.firstOrNull { t -> t.propertyId == selected.id }
                        roomOverride = null
                        placementError = null
                    }
                }
                Spacer(Modifier.height(Spacing.sm))
                if (locationTypes.isEmpty()) {
                    Text(
                        "Belum ada tipe kamar yang lengkap di lokasi ini. Tipe kamar wajib sudah punya harga dan nomor kamar sebelum bisa dipakai menambah penghuni.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    ChoiceDropdown("Tipe kamar *", type, locationTypes, { it.name }) { selected ->
                        type = selected; roomOverride = null; placementError = null
                    }
                }
            }
            FormSection("Sewa")
            Spacer(Modifier.height(Spacing.xs))
            when {
                type != null && pricesLoading -> Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    Text("Memuat durasi & harga…", style = MaterialTheme.typography.bodySmall)
                }
                type != null && typePrices.isEmpty() -> Text("Belum ada harga untuk tipe kamar ini.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                else -> {
                    ChoiceDropdown("Durasi *", duration, durations, { durationSummary(it.first, it.second) }) { durationOverride = it; placementError = null }
                    ChoiceDropdown("Harga sewa *", price, priceOptions, { "${rupiah(it.amount)} • ${it.occupancy} orang" }) { priceOverride = it; placementError = null }
                }
            }
            when {
                type == null -> Unit // belum memilih tipe kamar
                // Bila harga belum ada, pesan "Belum ada harga…" sudah tampil di blok harga di atas.
                typePrices.isEmpty() -> Unit
                roomsLoading -> Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    Text("Memuat nomor kamar…", style = MaterialTheme.typography.bodySmall)
                }
                availableRooms.isEmpty() -> Text("Belum ada nomor kamar tersedia untuk tipe ini. Buat dulu: tipe kamar → harga → nomor kamar.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                else -> ChoiceDropdown("Nomor kamar *", room, availableRooms, { it.name }) { roomOverride = it; placementError = null }
            }
            placementError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

            FormSection("Masa sewa & pembayaran")
            DateField("Tanggal masuk", moveIn, { moveIn = it })
            Text(
                if (moveOut == null) "Tanggal keluar: pilih durasi sewa"
                else "Tanggal keluar: ${formatDate(moveOut)}",
                style = MaterialTheme.typography.bodyMedium
            )
            if (subtotal > 0) {
                Text("Total sewa: ${rupiah(subtotal)}", style = MaterialTheme.typography.bodyMedium)
                val discountTypes = listOf("Nominal (Rp)" to "amount", "Persen (%)" to "percent")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    Box(Modifier.weight(1f)) {
                        ChoiceDropdown("Diskon", discountTypes.firstOrNull { it.second == discountType }, discountTypes, { it.first }) { discountType = it.second; discountText = "" }
                    }
                    Box(Modifier.weight(1f)) {
                        AppTextField(
                            value = discountText,
                            onValueChange = { discountText = it.filter(Char::isDigit) },
                            label = if (discountType == "percent") "Diskon (%)" else "Diskon (Rp)",
                            modifier = Modifier.fillMaxWidth(),
                            keyboardType = KeyboardType.Number
                        )
                    }
                }
                Text("Total setelah diskon: ${rupiah(total)}", style = MaterialTheme.typography.titleMedium)
            }
            Text("Pembayaran", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                FilterChip(selected = lunas, onClick = { lunas = true; amountText = ""; amountError = null }, label = { Text("Lunas") })
                FilterChip(selected = !lunas, onClick = { lunas = false; amountError = null }, label = { Text("Belum lunas") })
            }
            if (lunas) {
                Text("Lunas — dibayar penuh ${rupiah(total)}.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            } else {
                Text("Jumlah yang harus dibayar: ${rupiah(total)}", style = MaterialTheme.typography.bodyMedium)
                val amountErr = amountError
                AppTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter(Char::isDigit); amountError = null },
                    label = "Jumlah dibayar sekarang",
                    modifier = Modifier.fillMaxWidth().bringIntoViewRequester(amountRequester),
                    keyboardType = KeyboardType.Number,
                    prefix = { Text("Rp ") },
                    isError = amountErr != null,
                    supportingText = amountErr
                )
                Text("Sisa: ${rupiah((total - amountPaid).coerceAtLeast(0))}", style = MaterialTheme.typography.bodyMedium)
            }
            ChoiceDropdown("Metode pembayaran", methodOptions.firstOrNull { it.second == method }, methodOptions, { it.first }) { method = it.second }
            ChoiceDropdown("Kategori", category, categoryOptions, { it ?: "— Tanpa kategori —" }) { category = it }

            FormSection("Foto (opsional)")
            PhotoField("Foto penghuni", onFile = { photo = it })

            SaveButton(
                label = "Simpan penghuni",
                onClick = ::attemptSave,
                saving = saving,
                modifier = Modifier.fillMaxWidth().padding(top = Spacing.xs)
            )
        }
    }
}
