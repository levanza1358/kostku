package com.miraya.kostku.features.payment

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.miraya.kostku.*
import com.miraya.kostku.ui.common.*
import com.miraya.kostku.ui.theme.Screen
import com.miraya.kostku.ui.theme.Spacing

@Composable
internal fun Payments(data: RemoteSnapshot?, saving: Boolean, tab: Int, onTab: (Int) -> Unit, open: (String) -> Unit) {
    var fabOpen by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
        AppTopBar("Pembayaran")
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().padding(horizontal = Screen.edge, vertical = Spacing.md)) {
                val segments = listOf("Semua", "Pemasukan", "Pengeluaran")
                SegmentedPill(segments, segments[tab]) { onTab(segments.indexOf(it)) }
                Spacer(Modifier.height(Spacing.md))
                TabContent(tab, Modifier.weight(1f)) { index ->
                    when (index) {
                        1 -> IncomeTab(data, saving, open)
                        2 -> ExpenseTab(data, saving, open)
                        else -> LedgerTab(data, saving, open)
                    }
                }
            }
            // Ketuk area mana pun untuk menutup menu FAB.
            if (fabOpen) {
                Box(
                    Modifier.fillMaxSize().clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { fabOpen = false }
                )
            }
            Column(
                Modifier.align(Alignment.BottomEnd).padding(Screen.edge),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                AnimatedVisibility(visible = fabOpen) {
                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                        ExtendedFloatingActionButton(
                            onClick = { fabOpen = false; if (!saving) open("payment") },
                            icon = { Icon(Icons.Default.TrendingUp, null) },
                            text = { Text("Pemasukan") }
                        )
                        ExtendedFloatingActionButton(
                            onClick = { fabOpen = false; if (!saving) open("expense") },
                            icon = { Icon(Icons.Default.TrendingDown, null) },
                            text = { Text("Pengeluaran") }
                        )
                    }
                }
                FloatingActionButton(onClick = { if (!saving) fabOpen = !fabOpen }) {
                    Icon(if (fabOpen) Icons.Default.Close else Icons.Default.Add, "Catat pemasukan atau pengeluaran")
                }
            }
        }
    }
}
