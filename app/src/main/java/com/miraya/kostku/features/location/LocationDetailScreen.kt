package com.miraya.kostku.features.location

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.miraya.kostku.*
import com.miraya.kostku.ui.common.*
import com.miraya.kostku.ui.theme.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LocationDetailScreen(location: RemoteLocation, data: RemoteSnapshot, saving: Boolean, onBack: () -> Unit, open: (String) -> Unit) {
    // rememberSaveable: sub-tab & pencarian dipertahankan oleh SaveableStateProvider App
    // (di-key per `locationId`) sehingga kembali dari Detail kamar/Pengaturan tetap di tab ini.
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var roomQuery by rememberSaveable { mutableStateOf("") }
    var paymentFilter by rememberSaveable { mutableIntStateOf(0) }
    val rooms = data.rooms.filter { it.propertyId == location.id }
    val roomIds = rooms.map { it.id }.toSet()
    val tenants = data.tenants.filter { it.roomId in roomIds }
    val tenantIds = tenants.map { it.id }.toSet()
    val payments = data.payments.filter { it.tenantId in tenantIds && it.status != "cancelled" }
    val expenses = data.expenses.filter { it.propertyId == location.id }
    Scaffold(
        topBar = {
            AppTopBar(location.name, onBack = onBack, actions = {
                var menu by remember { mutableStateOf(false) }
                Box {
                    IconButton({ menu = true }) { Icon(Icons.Default.MoreVert, "Opsi lokasi") }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        DropdownMenuItem(text = { Text("Setelan") }, onClick = { menu = false; open("location-pengaturan-${location.id}") })
                        DropdownMenuItem(text = { Text("Edit info lokasi") }, onClick = { menu = false; open("location-edit-${location.id}") })
                    }
                }
            })
        }
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad)) {
            AppTabRow(
                listOf(
                    AppTab("Dashboard"),
                    AppTab("Kamar"),
                    AppTab("Pembayaran")
                ),
                tab
            ) { tab = it }
            TabContent(tab, Modifier.weight(1f)) { index ->
                when (index) {
                    0 -> LocationDashboard(location, rooms, tenants, payments, expenses, open)
                    1 -> RoomListTab(rooms, tenants.filter { it.active }.associate { it.roomId to it.name }, roomQuery, { roomQuery = it }, { open("room-detail-$it") }, { open("room-edit-$it") })
                    else -> LocationPaymentsTab(payments, expenses, data, paymentFilter, { paymentFilter = it })
                }
            }
        }
    }
}
