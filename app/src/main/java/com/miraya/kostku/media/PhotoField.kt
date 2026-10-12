package com.miraya.kostku.media

import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.miraya.kostku.diag.AppLog
import com.miraya.kostku.ui.theme.Spacing
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

@Composable
fun PhotoField(label: String, currentUrl: String? = null, onFile: (File?) -> Unit, onRemove: () -> Unit = {}) {
    val context = LocalContext.current
    var sourceSheet by remember { mutableStateOf(false) }
    var actionSheet by remember { mutableStateOf(false) }
    var viewing by remember { mutableStateOf(false) }
    var cameraFile by remember { mutableStateOf<File?>(null) }
    var sourceFile by remember { mutableStateOf<File?>(null) }
    var pending by remember { mutableStateOf<File?>(null) }
    var preview by remember { mutableStateOf<Bitmap?>(null) }
    var remoteState by remember(currentUrl) { mutableStateOf<StoredPhotoState>(StoredPhotoState.Loading) }
    var processing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    fun cleanup() {
        MediaPipeline.cleanup(sourceFile, pending)
        sourceFile = null
        pending = null
        preview = null
        processing = false
    }
    // Kompresi dijalankan di thread background agar UI tidak dibekukan dan proses tidak dibunuh sistem.
    fun processOutput(input: File, output: File) {
        AppLog.mediaPipeline("PhotoField: mulai proses crop/kompres", "input=${runCatching { input.length() }.getOrDefault(0L)} byte")
        processing = true
        scope.launch {
            val result = withContext(Dispatchers.Default) {
                runCatching {
                    MediaPipeline.compress(output, context)
                }
            }
            MediaPipeline.cleanup(input, output, pending)
            sourceFile = null
            processing = false
            result
                .onSuccess { file ->
                    AppLog.mediaPipeline("PhotoField: proses selesai", "output=${runCatching { file.length() }.getOrDefault(0L)} byte")
                    pending = file
                    preview = withContext(Dispatchers.Default) { MediaPipeline.previewBitmap(file) }
                    onFile(file)
                }
                .onFailure {
                    AppLog.mediaFailure("PhotoField: proses crop/kompres", it.message)
                    pending = null
                    preview = null
                }
        }
    }
    val crop = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val input = sourceFile
        val output: File? = result.data?.let { UCrop.getOutput(it)?.path }?.let { path -> File(path) }
        if (result.resultCode == Activity.RESULT_OK && input != null && output != null) {
            AppLog.mediaPipeline("PhotoField: crop selesai (OK)", "output=${runCatching { output.length() }.getOrDefault(0L)} byte")
            processOutput(input, output)
        } else {
            AppLog.mediaPipeline("PhotoField: crop dibatalkan/gagal", "resultCode=${result.resultCode}")
            MediaPipeline.cleanup(input, output)
            sourceFile = null
        }
    }
    fun startCrop(input: File) {
        AppLog.mediaPipeline("PhotoField: crop mulai", "input=${runCatching { input.length() }.getOrDefault(0L)} byte")
        sourceFile = input
        val output = File(context.cacheDir, "photo_crop_${UUID.randomUUID()}.jpg")
        val options = UCrop.Options().apply { setFreeStyleCropEnabled(false); setHideBottomControls(false); setCompressionFormat(Bitmap.CompressFormat.JPEG); setCompressionQuality(85) }
        val intent = UCrop.of(Uri.fromFile(input), Uri.fromFile(output)).apply {
            withAspectRatio(1f, 1f); withMaxResultSize(1280, 1280); withOptions(options)
        }.getIntent(context)
        crop.launch(intent)
    }
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri -> uri?.let { runCatching { MediaPipeline.copyToCache(context, it) }.onSuccess(::startCrop).onFailure { e -> AppLog.mediaFailure("PhotoField: copyToCache dari galeri", e.message) } } }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok -> val f = cameraFile; cameraFile = null; if (ok && f != null) startCrop(f) else { AppLog.mediaPipeline("PhotoField: kamera dibatalkan/gagal"); MediaPipeline.cleanup(f) } }
    val imageBitmap = preview?.asImageBitmap()
    val hasStoredPhoto = !currentUrl.isNullOrBlank() && remoteState is StoredPhotoState.Ready
    val hasAnyPhoto = hasStoredPhoto || pending != null
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs), modifier = Modifier.fillMaxWidth()) {
        if (label.isNotBlank()) Text(label, style = MaterialTheme.typography.labelLarge)
        Box(Modifier.fillMaxWidth()) {
            PhotoFramePreview(
                local = imageBitmap,
                remoteUrl = if (pending == null) currentUrl else null,
                onRemoteStateChanged = { remoteState = it },
                // Ketuk foto untuk pratinjau layar penuh; bila belum ada foto, buka pemilih sumber.
                onClick = { if (hasAnyPhoto) viewing = true else sourceSheet = true }
            )
            // Tombol ⋮ membuka opsi Ganti/Hapus foto (tanpa menutup aksi ketuk-untuk-preview).
            if (hasAnyPhoto) Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x88000000))
            ) {
                IconButton(onClick = { actionSheet = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Opsi foto", tint = Color.White)
                }
            }
        }
        if (processing) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                Text("Memproses foto…", style = MaterialTheme.typography.bodySmall)
            }
        } else if (pending != null) {
            TextButton(::cleanup) { Text("Ambil ulang") }
        }
    }
    if (viewing) ImagePreviewDialog(
        onDismiss = { viewing = false },
        url = if (pending == null) currentUrl else null,
        bitmap = imageBitmap,
        contentDescription = label
    )
    if (sourceSheet) PhotoSourceSheet(
        onGallery = { sourceSheet = false; gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
        onCamera = {
            sourceSheet = false
            val f = File(context.cacheDir, "photo_camera_${UUID.randomUUID()}.jpg")
            cameraFile = f
            camera.launch(FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", f))
        },
        onDismiss = { sourceSheet = false }
    )
    if (actionSheet) PhotoActionSheet(
        hasStoredPhoto = hasStoredPhoto,
        onReplace = { actionSheet = false; cleanup(); sourceSheet = true },
        onDelete = {
            actionSheet = false
            cleanup()
            onRemove()
        },
        onDismiss = { actionSheet = false }
    )
}
