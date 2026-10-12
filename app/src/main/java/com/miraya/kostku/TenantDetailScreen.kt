package com.miraya.kostku

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.miraya.kostku.features.tenant.TenantActionSheet
import com.miraya.kostku.ui.common.AppTab
import com.miraya.kostku.ui.common.AppTabRow
import com.miraya.kostku.ui.common.AppTopBar
import com.miraya.kostku.ui.common.TabContent
import com.miraya.kostku.ui.theme.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TenantDetailScreen(
    tenant: RemoteTenant,
    room: RemoteRoom?,
    location: RemoteLocation?,
    payments: List<RemotePayment>,
    api: KostkuApi,
    onBack: () -> Unit,
    onAction: (String) -> Unit,
    onReceipt: (String) -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var showActions by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val tabs = listOf(
        AppTab("Informasi"),
        AppTab("Pembayaran"),
        AppTab("Log")
    )
    Scaffold(
        topBar = { AppTopBar(tenant.name, onBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showActions = true }) {
                Icon(Icons.Default.Settings, contentDescription = "Kelola penghuni")
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            AppTabRow(tabs, selectedTab) { selectedTab = it }
            TabContent(selectedTab, Modifier.weight(1f)) { index ->
                when (index) {
                    0 -> TenantInformation(tenant, room, location)
                    1 -> TenantPaymentHistory(payments, onReceipt)
                    else -> TenantChangeLog(tenant, api)
                }
            }
        }
    }

    if (showActions) {
        TenantActionSheet(
            onAction = onAction,
            onDismiss = { showActions = false },
            sheetState = sheetState
        )
    }
}
