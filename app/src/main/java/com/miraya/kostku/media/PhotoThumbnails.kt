package com.miraya.kostku.media

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Foto jarak jauh yang dapat diketuk untuk membuka penampil layar penuh. Bila [url]
 * kosong, hanya merender pratinjau biasa tanpa aksi ketuk.
 *
 * Secara default foto ditampilkan **bundar** (lingkaran) — bentuk tampilan foto yang
 * seragam di seluruh aplikasi; pratinjau layar penuh tetap menampilkan foto utuh.
 * Untuk permukaan yang memang ingin persegi (mis. bukti pembayaran), berikan [shape].
 */
@Composable
fun ClickableRemotePhoto(
    url: String?,
    modifier: Modifier = Modifier,
    contentDescription: String = "Pratinjau foto",
    shape: Shape = CircleShape
) {
    var viewing by remember { mutableStateOf(false) }
    RemotePhotoPreview(
        url = url,
        modifier = modifier.then(if (!url.isNullOrBlank()) Modifier.clickable { viewing = true } else Modifier),
        contentDescription = contentDescription,
        shape = shape
    )
    if (viewing) ImagePreviewDialog(onDismiss = { viewing = false }, url = url, contentDescription = contentDescription)
}

/**
 * Thumbnail baris daftar: menampilkan [fallbackIcon] saat tidak ada foto, atau foto
 * jarak jauh (bundar) yang dapat diketuk untuk pratinjau layar penuh (zoom/geser).
 */
@Composable
fun ListPhotoThumb(
    url: String?,
    contentDescription: String,
    fallbackIcon: ImageVector? = null,
    modifier: Modifier = Modifier,
    size: Dp = 52.dp,
    shape: Shape = CircleShape
) {
    if (url.isNullOrBlank()) {
        if (fallbackIcon != null) Icon(fallbackIcon, contentDescription, Modifier.size(size))
        return
    }
    ClickableRemotePhoto(
        url = url,
        modifier = modifier.size(size),
        contentDescription = contentDescription,
        shape = shape
    )
}
