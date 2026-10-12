package com.miraya.kostku.features.tenant

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.miraya.kostku.*
import com.miraya.kostku.media.ClickableRemotePhoto
import com.miraya.kostku.ui.common.*
import com.miraya.kostku.ui.theme.Screen
import com.miraya.kostku.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TenantList(
    data: RemoteSnapshot?,
    query: String,
    onQuery: (String) -> Unit,
    sort: String,
    onSort: (String) -> Unit,
    locationFilter: String?,
    onLocationFilter: (String?) -> Unit,
    typeFilter: String?,
    onTypeFilter: (String?) -> Unit,
    add: () -> Unit,
    click: (String) -> Unit
) {
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    val rooms = data?.rooms.orEmpty()
    val roomById = rooms.associateBy { it.id }
    val all = data?.tenants.orEmpty()
    val filtered = all.filter { t ->
        val room = roomById[t.roomId]
        (locationFilter == null || room?.propertyId == locationFilter) &&
            (typeFilter == null || room?.roomTypeId == typeFilter) &&
            (query.isBlank() || t.name.contains(query, ignoreCase = true) || room?.name?.contains(query, ignoreCase = true) == true)
    }
    val shown = when (sort) {
        "name_za" -> filtered.sortedByDescending { it.name.lowercase() }
        "days_asc" -> filtered.sortedBy { tenantDaysLeft(it) ?: Long.MAX_VALUE }
        "days_desc" -> filtered.sortedByDescending { tenantDaysLeft(it) ?: Long.MIN_VALUE }
        else -> filtered.sortedBy { it.name.lowercase() }
    }
    val activeFilterCount = (if (locationFilter != null) 1 else 0) + (if (typeFilter != null) 1 else 0)

    // Alur wajib: lokasi → tipe kamar → harga → nomor kamar → penghuni. Tambah penghuni
    // hanya bisa bila ada minimal satu tipe kamar yang LENGKAP (punya harga DAN nomor kamar).
    val allTypes = data?.roomTypes.orEmpty()
    val allPrices = data?.prices.orEmpty()
    val canAddTenant = allTypes.any { t ->
        allPrices.any { it.roomTypeId == t.id } && rooms.any { it.roomTypeId == t.id }
    }

    Column(Modifier.fillMaxSize()) {
        AppTopBar("Penghuni") {
            FilledTonalButton(
                onClick = { showSheet = true },
                modifier = Modifier.padding(end = Screen.edge)
            ) {
                Icon(Icons.Default.Tune, null)
                Spacer(Modifier.width(Spacing.xs))
                Text(if (activeFilterCount > 0) "Filter ($activeFilterCount)" else "Filter")
            }
        }
        // Pencarian nama penghuni atau nomor kamar (mock 1 & 3).
        AppTextField(
            value = query,
            onValueChange = onQuery,
            label = "Cari nama atau kamar…",
            leadingIcon = { Icon(Icons.Default.Search, null) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = Screen.edge, vertical = Spacing.sm)
        )
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().padding(horizontal = Screen.edge, vertical = Spacing.md)) {
                when {
                !canAddTenant -> EmptyState(
                    "Belum bisa tambah penghuni",
                    "Lengkapi dulu: buat lokasi, tipe kamar, harga, lalu nomor kamar. Penghuni baru bisa ditambahkan setelah ada tipe kamar yang punya harga dan sudah memiliki nomor kamar.",
                    Icons.Default.People
                )
                all.isEmpty() -> EmptyState(
                    "Belum ada penghuni",
                    "Tekan tombol + untuk menambah penghuni pertama.",
                    Icons.Default.People,
                    actionLabel = "Tambah penghuni",
                    action = add
                )
                shown.isEmpty() -> EmptyState(
                    "Tidak ada hasil",
                    "Tidak ada penghuni yang cocok dengan filter ini.",
                    Icons.Default.SearchOff
                )
                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                    contentPadding = PaddingValues(bottom = Screen.fab)
                ) {
                    items(shown, key = { it.id }) { t ->
                        val room = roomById[t.roomId]
                        val typeName = data?.roomTypes?.firstOrNull { it.id == room?.roomTypeId }?.name
                        Card({ click(t.id) }, Modifier.fillMaxWidth()) {
                            ListItem(
                                headlineContent = { Text(t.name, style = MaterialTheme.typography.titleMedium) },
                                supportingContent = {
                                    val place = listOfNotNull(room?.name?.let { "Kamar $it" }, typeName).joinToString(" • ")
                                    Text(
                                        listOfNotNull(t.phone.ifBlank { null }, place.ifBlank { null })
                                            .joinToString(" • ").ifBlank { "Belum ada detail" }
                                    )
                                },
                                leadingContent = { ClickableRemotePhoto(t.photoUrl, Modifier.size(52.dp), "Foto ${t.name}") },
                                trailingContent = { DayLeftChip(daysUntil(t.moveOut)) }
                            )
                        }
                    }
                }
                }
            }
            FloatingActionButton(
                onClick = add,
                modifier = Modifier.align(Alignment.BottomEnd).padding(Screen.edge)
            ) { Icon(Icons.Default.Add, if (canAddTenant) "Tambah penghuni" else "Lengkapi lokasi, tipe, harga, dan kamar dulu") }
        }
    }

    if (showSheet) {
        TenantFilterSheet(
            locations = data?.locations.orEmpty(),
            types = data?.roomTypes.orEmpty(),
            locationFilter = locationFilter,
            typeFilter = typeFilter,
            sort = sort,
            onLocation = { onLocationFilter(it) },
            onType = { onTypeFilter(it) },
            onSort = { onSort(it) },
            onReset = { onLocationFilter(null); onTypeFilter(null); onSort("name_az") },
            onDismiss = { showSheet = false },
            sheetState = sheetState
        )
    }
}

/** Sisa hari menuju tanggal keluar (negatif bila sudah lewat), atau null bila tidak diketahui. */
private fun tenantDaysLeft(t: RemoteTenant): Long? = daysUntil(t.moveOut)
