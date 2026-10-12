package com.miraya.kostku.features.tenant

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.miraya.kostku.ui.common.AppTopBar
import com.miraya.kostku.ui.common.SaveButton
import com.miraya.kostku.ui.theme.Screen
import com.miraya.kostku.ui.theme.Spacing

/** Kerangka halaman aksi penghuni: app bar + kartu formulir + tombol simpan. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TenantActionScaffold(
    title: String,
    onBack: () -> Unit,
    saveLabel: String,
    saving: Boolean,
    onSave: () -> Unit,
    ctaIcon: ImageVector? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        topBar = { AppTopBar(title, onBack = onBack) }
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad)) {
            Column(
                Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = Screen.edge, vertical = Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) { content() }
            Surface(tonalElevation = 3.dp) {
                SaveButton(
                    label = saveLabel,
                    onClick = onSave,
                    saving = saving,
                    icon = ctaIcon,
                    modifier = Modifier.fillMaxWidth().padding(Screen.edge)
                )
            }
        }
    }
}
