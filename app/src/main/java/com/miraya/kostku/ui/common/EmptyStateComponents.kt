package com.miraya.kostku.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.miraya.kostku.ui.theme.Spacing

@Composable
fun EmptyState(
    title: String,
    message: String,
    icon: ImageVector,
    actionLabel: String? = null,
    action: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 44.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            // Kotak ikon redup (mock: kotak 84 dp, ikon 40 dp warna muted).
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(84.dp)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            if (action != null) {
                FilledTonalButton(onClick = action, shape = MaterialTheme.shapes.large) {
                    Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(Spacing.sm))
                    Text(actionLabel ?: "Tambah sekarang")
                }
            }
        }
    }
}

@Composable
fun SummaryCard(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier, iconColor: Color? = null) {
    Card(modifier) {
        Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                val tint = iconColor ?: MaterialTheme.colorScheme.primary
                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = tint)
                Text(label, style = MaterialTheme.typography.labelMedium, color = tint)
            }
            Text(value, style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
fun SettingsSection(title: String, subtitle: String? = null, icon: ImageVector, onClick: (() -> Unit)? = null) {
    Card(onClick = onClick ?: {}, modifier = Modifier.fillMaxWidth()) {
        ListItem(
            leadingContent = { Icon(icon, contentDescription = null) },
            headlineContent = { Text(title) },
            supportingContent = subtitle?.let { { Text(it) } },
            trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) }
        )
    }
}
