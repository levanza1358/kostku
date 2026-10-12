package com.miraya.kostku.media

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import coil3.compose.SubcomposeAsyncImage

/**
 * Pratinjau foto jarak jauh bersama. Pemuatan memakai Coil (cache memori + disk), jadi
 * **setiap foto hanya diunduh sekali** dan tidak lagi diambil ulang tiap layar dirender.
 *
 * Bila [shape] diberikan, gambar yang sudah dimuat di-clip ke bentuk itu dan di-crop agar
 * memenuhi seluruh bentuk (mis. lingkaran avatar); selagi memuat/gagal, teks status tetap
 * memakai [modifier] apa adanya agar terbaca.
 */
@Composable
fun RemotePhotoPreview(
    url: String?,
    modifier: Modifier = Modifier,
    contentDescription: String = "Pratinjau foto",
    shape: Shape? = null
) {
    if (url.isNullOrBlank()) return
    SubcomposeAsyncImage(
        model = url,
        contentDescription = contentDescription,
        modifier = if (shape != null) modifier.clip(shape) else modifier,
        contentScale = if (shape != null) ContentScale.Crop else ContentScale.Fit,
        loading = { Text("Memuat foto…", style = MaterialTheme.typography.bodySmall) },
        error = { Text("Foto tidak dapat dimuat.", style = MaterialTheme.typography.bodySmall) }
    )
}
