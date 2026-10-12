package com.miraya.kostku.media

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.miraya.kostku.ui.theme.Spacing
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter

/**
 * Kartu (KTP/ATM) style preview frame with 85.6:54 aspect ratio and rounded corners.
 * Shows the pending local photo, the stored remote photo, or a placeholder.
 */
/** Whether a stored remote photo is still loading, usable, or unusable. */
sealed interface StoredPhotoState {
    data object Loading : StoredPhotoState
    data object Ready : StoredPhotoState
    data object Error : StoredPhotoState
}

@Composable
fun PhotoFramePreview(
    local: ImageBitmap?,
    remoteUrl: String?,
    modifier: Modifier = Modifier,
    interactiveHint: Boolean = true,
    onRemoteStateChanged: ((StoredPhotoState) -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier
            .fillMaxWidth()
            .aspectRatio(1.585f)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape),
        contentAlignment = Alignment.Center
    ) {
        when {
            local != null -> Image(
                bitmap = local,
                contentDescription = "Pratinjau foto",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            !remoteUrl.isNullOrBlank() -> RemotePhotoFrameContent(remoteUrl, interactiveHint, onRemoteStateChanged)
            else -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Image, null, Modifier.size(36.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    "Belum ada foto",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (interactiveHint) Text(
                    "Rasio kartu 85,6 × 54 mm",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RemotePhotoFrameContent(
    url: String,
    interactiveHint: Boolean = true,
    onStateChanged: ((StoredPhotoState) -> Unit)? = null
) {
    // Coil (cache memori + disk): foto tersimpan hanya diunduh sekali.
    val painter = rememberAsyncImagePainter(model = url)
    val state by painter.state.collectAsState()
    LaunchedEffect(state) {
        onStateChanged?.invoke(
            when (state) {
                is AsyncImagePainter.State.Success -> StoredPhotoState.Ready
                is AsyncImagePainter.State.Error -> StoredPhotoState.Error
                else -> StoredPhotoState.Loading
            }
        )
    }
    when {
        state is AsyncImagePainter.State.Success -> Image(painter, "Pratinjau foto", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        state is AsyncImagePainter.State.Error -> Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            Icon(Icons.Default.Image, null, Modifier.size(32.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Belum ada foto", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (interactiveHint) Text(
                "Ketuk untuk menambahkan foto",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        else -> Text("Memuat foto…", style = MaterialTheme.typography.bodySmall)
    }
}
