package com.miraya.kostku.media

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.miraya.kostku.ui.theme.Screen
import com.miraya.kostku.ui.theme.Spacing

/**
 * Bottom sheet shown when no photo exists yet. Offers Galeri or Kamera.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoSourceSheet(onGallery: () -> Unit, onCamera: () -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = Screen.edge).padding(bottom = Spacing.xl)
        ) {
            Text("Pilih sumber foto", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = Spacing.sm))
            Row(
                Modifier.fillMaxWidth().clickable(onClick = onGallery).padding(vertical = Spacing.md, horizontal = Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.lg)
            ) {
                Icon(Icons.Default.Image, "Galeri", tint = MaterialTheme.colorScheme.primary)
                Text("Galeri", style = MaterialTheme.typography.titleMedium)
            }
            HorizontalDivider(Modifier.padding(horizontal = Spacing.xs))
            Row(
                Modifier.fillMaxWidth().clickable(onClick = onCamera).padding(vertical = Spacing.md, horizontal = Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.lg)
            ) {
                Icon(Icons.Default.PhotoCamera, "Kamera", tint = MaterialTheme.colorScheme.primary)
                Text("Kamera", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

/**
 * Bottom sheet shown when a photo exists. Offers replace or delete.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoActionSheet(
    onReplace: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
    hasStoredPhoto: Boolean = true
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = Screen.edge).padding(bottom = Spacing.xxl),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Text("Foto", style = MaterialTheme.typography.titleLarge)
            ListItem(
                headlineContent = { Text("Ganti foto") },
                supportingContent = { Text("Pilih foto pengganti dari galeri atau kamera") },
                leadingContent = { Icon(Icons.Default.SwapHoriz, "Ganti foto") },
                modifier = Modifier.clickable(onClick = onReplace)
            )
            if (hasStoredPhoto) ListItem(
                headlineContent = { Text("Hapus foto") },
                supportingContent = { Text("Foto akan dihapus setelah Anda menekan Simpan") },
                leadingContent = { Icon(Icons.Default.Delete, "Hapus foto") },
                modifier = Modifier.clickable(onClick = onDelete)
            )
        }
    }
}
