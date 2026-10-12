package com.miraya.kostku.media

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.miraya.kostku.diag.AppLog
import com.miraya.kostku.ui.theme.Spacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/**
 * Bidang logo lokasi: kotak 92 dp di tengah. Menampilkan logo tersimpan atau placeholder
 * kamera, dan menyediakan pemilih Galeri/Kamera di baliknya. Berbeda dari [PhotoField]
 * (kartu 85,6:54), logo **tidak** dipotong (crop) — pengguna dapat memilih logo apa pun
 * dan memakai "Ganti"/"Hapus" untuk mengubahnya.
 *
 * [onFile] menerima berkas logo yang sudah dikompres (sisi panjang ≤ 512 px). Bila pengguna
 * menghapus logo, [onRemove] dipanggil dan [onFile] dipanggil dengan null.
 */
@Composable
fun LogoField(
    currentUrl: String? = null,
    onFile: (File?) -> Unit,
    onRemove: () -> Unit = {},
    contentDescription: String = "Logo lokasi"
) {
    val context = LocalContext.current
    var sourceSheet by remember { mutableStateOf(false) }
    var viewing by remember { mutableStateOf(false) }
    var cameraFile by remember { mutableStateOf<File?>(null) }
    var pending by remember { mutableStateOf<File?>(null) }
    var preview by remember { mutableStateOf<Bitmap?>(null) }
    var processing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    fun reset() { MediaPipeline.cleanup(cameraFile, pending); cameraFile = null; pending = null; preview = null; processing = false; onFile(null); onRemove() }
    fun process(input: File) {
        processing = true
        scope.launch {
            val result = withContext(Dispatchers.Default) { runCatching { MediaPipeline.compressLogo(input, context) } }
            MediaPipeline.cleanup(input)
            processing = false
            result.onSuccess { file ->
                pending = file
                preview = withContext(Dispatchers.Default) { MediaPipeline.previewBitmap(file) }
                onFile(file)
            }.onFailure { pending = null; preview = null }
        }
    }
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { runCatching { MediaPipeline.copyToCache(context, it) }.onSuccess(::process).onFailure { e -> AppLog.mediaFailure("LogoField: copyToCache dari galeri", e.message) } }
    }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok -> val f = cameraFile; cameraFile = null; if (ok && f != null) process(f) else MediaPipeline.cleanup(f) }
    val imageBitmap = preview?.asImageBitmap()
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Box(
            Modifier
                .size(92.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(22.dp))
                .clickable {
                    val has = imageBitmap != null || !currentUrl.isNullOrBlank()
                    if (has) viewing = true else sourceSheet = true
                },
            contentAlignment = Alignment.Center
        ) {
            when {
                imageBitmap != null -> Image(imageBitmap, contentDescription, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                !currentUrl.isNullOrBlank() && pending == null -> RemotePhotoPreview(currentUrl, Modifier.fillMaxSize(), contentDescription, shape = RoundedCornerShape(22.dp))
                else -> Icon(Icons.Default.PhotoCamera, "Pilih logo", Modifier.size(28.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            // Lencana kamera kecil di sudut agar jelas logo dapat ditekan untuk mengganti.
            Box(
                Modifier.align(Alignment.BottomEnd).padding(5.dp).size(26.dp)
                    .clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.PhotoCamera, null, Modifier.size(15.dp), tint = MaterialTheme.colorScheme.onPrimary) }
        }
        if (processing) Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
            Text("Memproses logo…", style = MaterialTheme.typography.bodySmall)
        } else {
            val has = imageBitmap != null || !currentUrl.isNullOrBlank()
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                OutlinedButton({ sourceSheet = true }, shape = MaterialTheme.shapes.large) { Text(if (has) "Ganti logo" else "Pilih logo") }
                if (has) TextButton({ reset() }, shape = MaterialTheme.shapes.large) { Text("Hapus", color = MaterialTheme.colorScheme.error) }
            }
            Text(
                "Logo tampil di kwitansi. Kosongkan untuk pakai ikon rumah.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
    if (viewing) ImagePreviewDialog(onDismiss = { viewing = false }, url = if (pending == null) currentUrl else null, bitmap = imageBitmap, contentDescription = contentDescription)
    if (sourceSheet) PhotoSourceSheet(
        onGallery = { sourceSheet = false; gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
        onCamera = {
            sourceSheet = false
            val f = File(context.cacheDir, "logo_camera_${UUID.randomUUID()}.jpg")
            cameraFile = f
            camera.launch(FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", f))
        },
        onDismiss = { sourceSheet = false }
    )
}
