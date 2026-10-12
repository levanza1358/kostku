package com.miraya.kostku

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.miraya.kostku.features.location.*
import com.miraya.kostku.features.payment.Payments
import com.miraya.kostku.features.receipt.ReceiptScreen
import com.miraya.kostku.ui.settings.SettingsTab
import com.miraya.kostku.ui.settings.ThemeScreen
import com.miraya.kostku.ui.settings.InfoScreen
import com.miraya.kostku.ui.settings.CategorySettingsScreen
import com.miraya.kostku.ui.settings.MoveOutHistoryScreen
import com.miraya.kostku.features.room.*
import com.miraya.kostku.features.roomtype.*
import com.miraya.kostku.features.tenant.*
import com.miraya.kostku.diag.AppLog
import com.miraya.kostku.media.MediaPipeline
import com.miraya.kostku.ui.common.*
import com.miraya.kostku.ui.theme.Screen
import com.miraya.kostku.ui.theme.Spacing
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun App(token: String?, login: () -> Unit, mode: String, accent: String, setMode: (String) -> Unit, setAccent: (String) -> Unit) {
    val context = LocalContext.current
    val api = remember { KostkuApi(context) }
    val scope = rememberCoroutineScope()
    var profile by remember { mutableStateOf<UserProfile?>(null) }
    var data by remember { mutableStateOf<RemoteSnapshot?>(null) }
    var loading by remember { mutableStateOf(true) }
    var reloading by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    // Error khusus aksi simpan: ditahan sampai aksi berikutnya supaya pengguna benar-benar
    // sempat membacanya (berbeda dari Toast yang hilang sendiri).
    var actionError by remember { mutableStateOf<String?>(null) }
    var destination by remember { mutableIntStateOf(0) }
    var tenantId by remember { mutableStateOf<String?>(null) }
    var locationId by remember { mutableStateOf<String?>(null) }
    var typeLocationId by remember { mutableStateOf<String?>(null) }
    var typeId by remember { mutableStateOf<String?>(null) }
    var batchTypeId by remember { mutableStateOf<String?>(null) }
    var batchLocationId by remember { mutableStateOf<String?>(null) }
    var addTenantLocationId by remember { mutableStateOf<String?>(null) }
    var tenantActionPage by remember { mutableStateOf<String?>(null) }
    var roomEditId by remember { mutableStateOf<String?>(null) }
    var roomEditCompact by remember { mutableStateOf(false) }
    var locationSettingsId by remember { mutableStateOf<String?>(null) }
    var roomNumberLocationId by remember { mutableStateOf<String?>(null) }
    var roomDetailId by remember { mutableStateOf<String?>(null) }
    var locationEditId by remember { mutableStateOf<String?>(null) }
    var locationAdd by remember { mutableStateOf(false) }
    var receiptId by remember { mutableStateOf<String?>(null) }
    var dialog by remember { mutableStateOf<String?>(null) }
    var settingsPage by remember { mutableStateOf<String?>(null) }

    // Sub-tab/pencarian/filter pada layar DALAM (detail entitas) dipertahankan oleh
    // SaveableStateProvider per `screenKey` (lihat bawah) — di-key dengan id entitas sehingga
    // tidak bocor antar-data. Untuk TAB UTAMA, state-nya di-*hoist* di sini karena layar tab
    // utama tidak dibungkus holder (selalu ada selama di root).
    var tabPembayaran by rememberSaveable { mutableIntStateOf(0) }
    var sortPenghuni by rememberSaveable { mutableStateOf("name_az") }
    var filterPenghuniLokasi by rememberSaveable { mutableStateOf<String?>(null) }
    var filterPenghuniTipe by rememberSaveable { mutableStateOf<String?>(null) }
    var queryPenghuni by rememberSaveable { mutableStateOf("") }
    val appContext = context.applicationContext
    fun reload() {
        scope.launch {
            AppLog.i("[reload] mulai memuat ulang snapshot")
            // Tampilkan dulu snapshot dari cache lokal (instan, tanpa kuota) supaya layar
            // tidak kosong saat pertama dibuka atau saat offline — lalu validasi ke server.
            if (data == null) api.cachedSnapshot()?.let { data = it; loading = false }
            if (data == null) loading = true else reloading = true
            api.snapshot()
                .onSuccess { data = it; error = null; AppLog.i("[reload] selesai - lokasi=${it.locations.size}, kamar=${it.rooms.size}, tipe=${it.roomTypes.size}, harga=${it.prices.size}, penghuni=${it.tenants.size}, pembayaran=${it.payments.size}") }
                .onFailure { if (data == null) error = it.message ?: "Data tidak dapat dimuat."; AppLog.e("[reload] gagal - pesan=${it.message ?: "tanpa pesan"}", it) }
            loading = false
            reloading = false
        }
    }
    // Toast tampil sebagai overlay sistem sehingga selalu muncul di layar mana pun
    // (halaman depan maupun halaman dalam), tidak seperti Snackbar yang terikat pada Scaffold.
    fun notify(message: String) {
        AppLog.snackbar(message)
        scope.launch { Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show() }
    }
    fun mutate(successMessage: String, action: suspend () -> Unit) {
        if (saving) {
            AppLog.w("[mutate] diabaikan karena sedang menyimpan - aksi=$successMessage")
            return
        }
        scope.launch {
            AppLog.actionStart(successMessage)
            saving = true
            error = null
            actionError = null
            runCatching { action() }
                .onSuccess { AppLog.actionSuccess(successMessage); notify(successMessage) }
                .onFailure {
                    AppLog.actionFailure(successMessage, it.message)
                    actionError = it.message ?: "Operasi gagal. Silakan coba lagi."
                    notify(actionError!!)
                }
            saving = false
            reload()
        }
    }
    LaunchedEffect(Unit) {
        if (!api.hasSession()) { loading = false; return@LaunchedEffect }
        // Render instan dari cache lokal (profil + snapshot) tanpa menunggu jaringan —
        // pemuatan ulang berikutnya tidak perlu mengunduh apa pun bila tak ada perubahan.
        api.cachedUser()?.let { profile = it }
        api.cachedSnapshot()?.let { data = it }
        if (profile != null) loading = false
        // Lalu validasi sesi ke server. Hanya 401 yang memaksa logout.
        api.me()
            .onSuccess { profile = it; reload() }
            .onFailure { e ->
                when (e) {
                    is CachedProfile -> { profile = e.profile; reload() }
                    is UnauthenticatedException -> { api.clearSession(); profile = null; data = null; loading = false }
                    else -> { if (profile == null) { error = e.message ?: "Gagal memuat sesi."; loading = false } }
                }
            }
    }
    LaunchedEffect(token) { token?.let { scope.launch { api.authGoogle(it).onSuccess { profile = it; reload() }.onFailure { error = it.message } } } }
    if (loading) return Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(Spacing.md))
        Text("Memuat data…", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    if (profile == null) return LoginScreen(error, login)
    if (settingsPage == "theme") { ThemeScreen(mode, accent, setMode, setAccent) { settingsPage = null }; return }
    if (settingsPage == "categories") { CategorySettingsScreen(data?.categories.orEmpty(), saving, api, ::mutate) { settingsPage = null }; return }
    if (settingsPage == "movedOut") { MoveOutHistoryScreen(api) { settingsPage = null }; return }
    if (settingsPage != null) { InfoScreen(settingsPage!!) { settingsPage = null }; return }
    val snapshot = data
    if (snapshot == null) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            if (error != null) Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
                TextButton(::reload) { Text("Coba lagi") }
            } else CircularProgressIndicator()
        }
        return
    }
    val tenant = snapshot.tenants.firstOrNull { it.id == tenantId }
    val location = snapshot.locations.firstOrNull { it.id == locationId }
    val typeLocation = snapshot.locations.firstOrNull { it.id == typeLocationId }
    val selectedType = snapshot.roomTypes.firstOrNull { it.id == typeId }
    val batchType = snapshot.roomTypes.firstOrNull { it.id == batchTypeId }
    val batchLocation = snapshot.locations.firstOrNull { it.id == batchLocationId }
    val addLocation = snapshot.locations.firstOrNull { it.id == addTenantLocationId }
    val room = snapshot.rooms.firstOrNull { it.id == roomEditId }
    val locationSettings = snapshot.locations.firstOrNull { it.id == locationSettingsId }
    val roomNumberLocation = snapshot.locations.firstOrNull { it.id == roomNumberLocationId }
    val roomDetail = snapshot.rooms.firstOrNull { it.id == roomDetailId }
    val locationEdit = snapshot.locations.firstOrNull { it.id == locationEditId }
    val receipt = snapshot.payments.firstOrNull { it.id == receiptId }
    // `screenKey` mengidentifikasi layar dalam yang sedang tampil; dipakai SaveableStateHolder
    // agar kembali dari layar yang lebih dalam memulihkan sub-tab/pencarian/filter layar induk.
    val screenKey: String = when {
        batchLocation != null && batchType == null -> "batchRoom:${batchLocation.id}"
        typeLocation != null && selectedType == null && batchType == null -> "roomTypes:${typeLocation.id}"
        typeLocation != null && selectedType != null -> "prices:${selectedType.id}"
        typeLocation != null && batchType != null -> "batchRoomType:${batchType.id}"
        addLocation != null -> "addTenant:${addLocation.id}"
        room != null -> "roomEdit:${room.id}"
        roomDetail != null -> "roomDetail:${roomDetail.id}"
        roomNumberLocation != null -> "roomNumber:${roomNumberLocation.id}"
        locationEdit != null -> "locationEdit:${locationEdit.id}"
        locationAdd -> "locationAdd"
        locationSettings != null -> "locationSettings:${locationSettings.id}"
        receipt != null -> "receipt:${receipt.id}"
        tenantActionPage != null && tenant != null -> "tenantAction:${tenant.id}:$tenantActionPage"
        tenant != null -> "tenantDetail:${tenant.id}"
        location != null -> "locationDetail:${location.id}"
        else -> "root:$destination"
    }
    val stateHolder = rememberSaveableStateHolder()
    // Tombol kembali fisik: keluar dari layar dalam (setara ikon "Kembali"). Di tab utama
    // (tidak ada layar dalam aktif) nilai null sehingga back bawaan keluar dari aplikasi.
    val backAction: (() -> Unit)? = when {
        batchLocation != null && batchType == null -> { { batchLocationId = null } }
        typeLocation != null && selectedType == null && batchType == null -> { { typeLocationId = null } }
        typeLocation != null && selectedType != null -> { { typeId = null } }
        typeLocation != null && batchType != null -> { { batchTypeId = null } }
        addLocation != null -> { { addTenantLocationId = null } }
        room != null -> { { roomEditId = null } }
        roomDetail != null -> { { roomDetailId = null } }
        roomNumberLocation != null -> { { roomNumberLocationId = null } }
        locationEdit != null -> { { locationEditId = null } }
        locationAdd -> { { locationAdd = false } }
        locationSettings != null -> { { locationSettingsId = null } }
        receipt != null -> { { receiptId = null } }
        tenantActionPage != null && tenant != null -> { { tenantActionPage = null } }
        tenant != null -> { { tenantId = null } }
        location != null -> { { locationId = null } }
        else -> null
    }
    BackHandler(enabled = backAction != null) { backAction?.invoke() }
    // Semua layar (termasuk detail & halaman aksi) dirender lewat `when` ini supaya banner
    // error/reload bisa ditampilkan di atas layar mana pun, bukan hanya tab utama.
    val currentScreen: @Composable () -> Unit = when {
        batchLocation != null && batchType == null -> {
            {
                // Hanya tipe kamar yang sudah punya harga yang dapat dibuatkan nomor kamar
                // (alur: lokasi → tipe kamar → harga → nomor kamar).
                BatchRoomScreen(batchLocation, snapshot.roomTypes.filter { it.propertyId == batchLocation.id && snapshot.prices.any { p -> p.roomTypeId == it.id } }, null, saving, null, { batchLocationId = null }) { selectedType, names ->
                    mutate("Kamar berhasil ditambahkan.") {
                        api.createRoomsBatch(batchLocation.id, selectedType.id, "vacant", names).getOrThrow()
                        batchLocationId = null
                    }
                }
            }
        }
        typeLocation != null && selectedType == null && batchType == null -> {
            { RoomTypeScreen(typeLocation, snapshot.roomTypes.filter { it.propertyId == typeLocation.id }, saving, { typeLocationId = null }, { typeId = it.id }, { old, name -> mutate("Tipe kamar berhasil disimpan.") { if (old == null) api.createRoomType(typeLocation.id, name).getOrThrow() else api.updateRoomType(old.id, name).getOrThrow() } }, { mutate("Tipe kamar berhasil dihapus.") { api.deleteRoomType(it.id).getOrThrow() } }) }
        }
        typeLocation != null && selectedType != null -> {
            { RoomTypePriceScreen(typeLocation, selectedType, snapshot.prices.filter { it.roomTypeId == selectedType.id }, saving, { typeId = null }, { batchTypeId = selectedType.id }, { old,o,d,u,a -> mutate("Harga berhasil disimpan.") { if (old == null) api.createRoomTypePrice(selectedType.id,o,d,u,a).getOrThrow() else api.updateRoomTypePrice(old.id,o,d,u,a).getOrThrow() } }, { mutate("Harga berhasil dihapus.") { api.deleteRoomTypePrice(it.id).getOrThrow() } }) }
        }
        typeLocation != null && batchType != null -> {
            { BatchRoomScreen(typeLocation, snapshot.roomTypes.filter { it.propertyId == typeLocation.id }, batchType, saving, null, { batchTypeId = null }) { t,n -> mutate("Kamar berhasil ditambahkan.") { api.createRoomsBatch(typeLocation.id,t.id,"vacant",n).getOrThrow(); batchTypeId = null } } }
        }
        addLocation != null -> {
            {
                AddTenantScreen(addLocation, snapshot.locations, snapshot.rooms, snapshot.roomTypes.filter { t -> snapshot.prices.any { it.roomTypeId == t.id } && snapshot.rooms.any { it.roomTypeId == t.id } }, snapshot.categories.filter { it.kind == "income" }.map { it.name }, api, saving, { addTenantLocationId = null }) { n,p,pi,ti,ri,pri,mi,mo,r,paid,ps,dt,dv,file,method,category -> mutate("Penghuni berhasil disimpan.") { val id = api.createTenantPlacement(n,p,pi,ti,ri,pri,mi,mo,r,paid,ps,dt,dv,method,category).getOrThrow().optString("id")
                    AppLog.step("Simpan penghuni", "createTenantPlacement sukses", "tenantId=$id")
                    if (file != null && id.isNotBlank()) {
                        AppLog.step("Simpan penghuni", "uploadPhoto", "tenantId=$id, ukuran=${file.length()} byte")
                        runCatching { api.uploadPhoto("tenant", id, file) }
                            .onSuccess { AppLog.step("Simpan penghuni", "uploadPhoto sukses"); notify("Foto penghuni berhasil diunggah.") }
                            .onFailure { AppLog.actionFailure("Upload foto penghuni", it.message); notify("Penghuni tersimpan, tetapi foto gagal diunggah.") }
                        MediaPipeline.cleanup(file)
                    }
                    addTenantLocationId = null } }
            }
        }
        room != null -> {
            {
                RoomEditScreen(room, snapshot.roomTypes.filter { it.propertyId == room.propertyId }, saving, { roomEditId = null }, { n,t,s,token,pdam,f,remove ->
                    mutate("Perubahan kamar berhasil disimpan.") {
                        AppLog.step("Simpan kamar", "1. updateRoom", "roomId=${room.id}, roomTypeId=$t, status=$s")
                        api.updateRoom(room.id,n,t,s,token,pdam).getOrThrow()
                        AppLog.step("Simpan kamar", "1. updateRoom sukses")
                        if (remove) {
                            AppLog.step("Simpan kamar", "2. deletePhoto", "roomId=${room.id}")
                            api.deletePhoto("room",room.id).getOrThrow()
                            AppLog.step("Simpan kamar", "2. deletePhoto sukses")
                        }
                        if (f != null) {
                            AppLog.step("Simpan kamar", "3. uploadPhoto", "roomId=${room.id}, ukuran=${f.length()} byte")
                            runCatching { api.uploadPhoto("room", room.id, f) }
                                .onSuccess { AppLog.step("Simpan kamar", "3. uploadPhoto sukses"); notify("Foto kamar berhasil diunggah.") }
                                .onFailure { AppLog.actionFailure("Upload foto kamar", it.message); notify("Data kamar tersimpan, tetapi foto gagal diunggah.") }
                            MediaPipeline.cleanup(f)
                        }
                        roomEditId = null
                    }
                }, { mutate("Kamar berhasil dihapus.") { api.deleteRoom(room.id).getOrThrow(); roomEditId = null } }, compact = roomEditCompact)
            }
        }
        roomDetail != null -> {
            {
                val loc = snapshot.locations.firstOrNull { it.id == roomDetail.propertyId }
                val occupant = snapshot.tenants.firstOrNull { it.roomId == roomDetail.id && it.active }
                RoomDetailScreen(roomDetail, occupant, loc, { roomDetailId = null }) { roomEditCompact = false; roomEditId = roomDetail.id; roomDetailId = null }
            }
        }
        roomNumberLocation != null -> {
            {
                RoomNumberScreen(roomNumberLocation, snapshot.rooms.filter { it.propertyId == roomNumberLocation.id }, { roomNumberLocationId = null }, { roomEditCompact = true; roomEditId = it }, { batchLocationId = roomNumberLocation.id; roomNumberLocationId = null })
            }
        }
        locationEdit != null -> {
            {
                LocationEditScreen(locationEdit, saving, { locationEditId = null }) { name, branch, address, description, token, pdam, logo, removeLogo ->
                    mutate("Lokasi berhasil diperbarui.") {
                        api.updateProperty(locationEdit.id, name, branch, address, description, token, pdam).getOrThrow()
                        if (removeLogo) runCatching { api.deletePhoto("property", locationEdit.id) }
                        if (logo != null) runCatching { api.uploadPhoto("property", locationEdit.id, logo) }
                        MediaPipeline.cleanup(logo)
                        locationEditId = null
                    }
                }
            }
        }
        locationAdd -> {
            {
                LocationEditScreen(null, saving, { locationAdd = false }) { name, branch, address, description, token, pdam, logo, removeLogo ->
                    mutate("Lokasi berhasil ditambahkan.") {
                        val id = api.createProperty(name, branch, address, description).getOrThrow().optString("id")
                        if (token != null || pdam != null) runCatching { api.updateProperty(id, name, branch, address, description, token, pdam) }
                        if (logo != null && id.isNotBlank()) runCatching { api.uploadPhoto("property", id, logo) }
                        MediaPipeline.cleanup(logo)
                        locationAdd = false
                    }
                }
            }
        }
        locationSettings != null -> {
            {
                LocationSettingsScreen(locationSettings, snapshot, { locationSettingsId = null }) { action -> when {
                    action.startsWith("location-edit-") -> locationEditId = action.removePrefix("location-edit-")
                    action.startsWith("roomtype-manage-") -> typeLocationId = action.removePrefix("roomtype-manage-")
                    action.startsWith("rooms-") -> roomNumberLocationId = action.removePrefix("rooms-")
                    else -> dialog = action } }
                MainDialogs(dialog, snapshot, saving, ::mutate, { dialog = null }, api)
            }
        }
        receipt != null -> {
            {
                val rTenant = snapshot.tenants.firstOrNull { it.id == receipt.tenantId }
                val rRoom = snapshot.rooms.firstOrNull { it.id == rTenant?.roomId }
                val rLocation = snapshot.locations.firstOrNull { it.id == rRoom?.propertyId }
                ReceiptScreen(receipt, rTenant, rRoom, rLocation, { receiptId = null })
            }
        }
        tenantActionPage != null && tenant != null -> {
            { TenantActionPage(tenantActionPage!!, tenant, snapshot, saving, api, { tenantActionPage = null }, { tenantActionPage = null }, ::mutate) }
        }
        tenant != null -> {
            {
                val tenantRoom = snapshot.rooms.firstOrNull { it.id == tenant.roomId }
                val tenantLocation = snapshot.locations.firstOrNull { it.id == tenantRoom?.propertyId }
                TenantDetailScreen(tenant, tenantRoom, tenantLocation, snapshot.payments.filter { it.tenantId == tenant.id }, api, { tenantId = null }, { tenantActionPage = it }) { receiptId = it }
            }
        }
        location != null -> {
            {
                LocationDetailScreen(location, snapshot, saving, { locationId = null }) { action -> when {
                    action.startsWith("room-detail-") -> roomDetailId = action.removePrefix("room-detail-")
                    action.startsWith("room-edit-") -> { roomEditCompact = false; roomEditId = action.removePrefix("room-edit-") }
                    action.startsWith("location-pengaturan-") -> locationSettingsId = action.removePrefix("location-pengaturan-")
                    action.startsWith("location-edit-") -> locationEditId = action.removePrefix("location-edit-")
                    action.startsWith("add-tenant-") -> addTenantLocationId = action.removePrefix("add-tenant-")
                    action == "payment" -> dialog = "payment"
                    else -> dialog = action } }
                MainDialogs(dialog, snapshot, saving, ::mutate, { dialog = null }, api)
            }
        }
        else -> {
            {
                Scaffold(
                    // Insets atas ditangani `AppTopBar` tiap tab, jadi Scaffold tidak boleh
                    // menambahkannya lagi (kalau tidak, muncul jarak ganda di bawah status bar).
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    bottomBar = { NavigationBar { listOf("Penghuni" to Icons.Default.People, "Lokasi" to Icons.Default.LocationOn, "Pembayaran" to Icons.Default.AccountBalanceWallet, "Pengaturan" to Icons.Default.Settings).forEachIndexed { i, item -> NavigationBarItem(destination == i, { destination = i }, { Icon(item.second, item.first) }, label = { Text(item.first) }) } } }
                ) { pad -> Column(Modifier.fillMaxSize().padding(pad)) { when (destination) { 0 -> TenantList(snapshot, queryPenghuni, { queryPenghuni = it }, sortPenghuni, { sortPenghuni = it }, filterPenghuniLokasi, { filterPenghuniLokasi = it }, filterPenghuniTipe, { filterPenghuniTipe = it }, { addTenantLocationId = snapshot.locations.firstOrNull()?.id }) { tenantId = it }; 1 -> LocationTab(snapshot, saving) { if (it.startsWith("location-open-")) locationId = it.removePrefix("location-open-") else if (it.startsWith("location-edit-")) locationEditId = it.removePrefix("location-edit-") else if (it == "location-add") locationAdd = true else dialog = it }; 2 -> Payments(snapshot, saving, tabPembayaran, { tabPembayaran = it }) { action -> if (action.startsWith("receipt-")) receiptId = action.removePrefix("receipt-") else dialog = action }; else -> SettingsTab(profile!!, snapshot, mode, accent, { settingsPage = it }) { scope.launch { api.logout(); profile = null; data = null; destination = 0 } } } } }
                MainDialogs(dialog, snapshot, saving, ::mutate, { dialog = null }, api)
            }
        }
    }
    Box(Modifier.fillMaxSize()) {
        // Layar dalam dipulihkan per `screenKey` (sub-tab/pencarian/filter tetap sama saat
        // kembali). Tab utama tidak dibungkus holder (state-nya di-hoist di atas).
        stateHolder.SaveableStateProvider(screenKey) { currentScreen() }
        if (reloading) LinearProgressIndicator(Modifier.fillMaxWidth().align(Alignment.TopCenter))
        val bannerMessage = error ?: actionError
        bannerMessage?.let { message ->
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shadowElevation = 6.dp,
                modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth()
            ) {
                Row(Modifier.padding(horizontal = Screen.edge, vertical = Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(Spacing.sm))
                    Text(message, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onErrorContainer)
                    TextButton(onClick = { error = null; actionError = null }) { Text("Tutup") }
                    if (error != null) TextButton(::reload) { Text("Coba lagi") }
                }
            }
        }
    }
}
