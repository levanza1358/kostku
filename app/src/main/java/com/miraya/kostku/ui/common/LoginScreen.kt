package com.miraya.kostku.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.miraya.kostku.ui.theme.Dimens
import com.miraya.kostku.ui.theme.Spacing

@Composable
fun LoginScreen(error: String?, login: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(Spacing.xxl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(76.dp)
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.LocationOn, null, Modifier.size(38.dp), tint = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(Modifier.height(Spacing.xl))
        Text("Kostku", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(Spacing.xs))
        Text(
            "Kelola kos Anda lebih mudah",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        error?.let {
            Spacer(Modifier.height(Spacing.lg))
            Text(it, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        }
        Spacer(Modifier.height(Spacing.xxl))
        Button(onClick = login, modifier = Modifier.fillMaxWidth().height(Dimens.ButtonHeight), shape = MaterialTheme.shapes.large) {
            Icon(Icons.Default.People, null, Modifier.size(18.dp))
            Spacer(Modifier.width(Spacing.sm))
            Text("Masuk dengan Google", style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(Spacing.sm))
        Text("Dengan masuk, Anda menyetujui Ketentuan & Kebijakan Privasi.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}
