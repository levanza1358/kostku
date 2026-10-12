package com.miraya.kostku.media

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.SubcomposeAsyncImage

private const val MIN_SCALE = 1f
private const val MAX_SCALE = 6f

/**
 * Penampil foto layar penuh dengan zoom (cubit) & geser (pan) dan tombol tutup.
 * Berikan [url] untuk foto tersimpan di server, atau [bitmap] untuk foto lokal yang
 * belum diunggah. Ketuk dua kali untuk memperbesar/mengembalikan, atau tekan tombol
 * Tutup / tombol kembali untuk menutup.
 */
@Composable
fun ImagePreviewDialog(
    onDismiss: () -> Unit,
    url: String? = null,
    bitmap: ImageBitmap? = null,
    contentDescription: String = "Pratinjau foto"
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            Modifier.fillMaxSize().background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            when {
                bitmap != null -> ZoomableImage(bitmap, contentDescription)
                !url.isNullOrBlank() -> RemoteViewerImage(url, contentDescription)
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .background(Color(0x66000000), CircleShape)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.White)
            }
        }
    }
}

@Composable
private fun RemoteViewerImage(url: String, contentDescription: String) {
    SubcomposeAsyncImage(
        model = url,
        contentDescription = contentDescription,
        contentScale = ContentScale.Fit,
        modifier = Modifier.fillMaxSize(),
        loading = { CircularProgressIndicator(color = Color.White) },
        error = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                Text("Foto tidak dapat dimuat.", color = Color.White, style = MaterialTheme.typography.bodyMedium)
            }
        }
    )
}

@Composable
private fun ZoomableImage(image: ImageBitmap, contentDescription: String) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    fun clamp(value: Float, bound: Float) = value.coerceIn(-bound, bound)
    Image(
        bitmap = image,
        contentDescription = contentDescription,
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { containerSize = it }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val newScale = (scale * zoom).coerceIn(MIN_SCALE, MAX_SCALE)
                    scale = newScale
                    if (newScale > MIN_SCALE) {
                        // Batasi geseran agar gambar tidak hilang keluar layar.
                        val maxX = (containerSize.width * (newScale - 1f)) / 2f
                        val maxY = (containerSize.height * (newScale - 1f)) / 2f
                        offsetX = clamp(offsetX + pan.x, maxX)
                        offsetY = clamp(offsetY + pan.y, maxY)
                    } else {
                        offsetX = 0f; offsetY = 0f
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(onDoubleTap = {
                    if (scale > MIN_SCALE) {
                        scale = MIN_SCALE; offsetX = 0f; offsetY = 0f
                    } else {
                        scale = 2.5f
                    }
                })
            }
            .graphicsLayer {
                scaleX = scale; scaleY = scale
                translationX = offsetX; translationY = offsetY
            }
    )
}
