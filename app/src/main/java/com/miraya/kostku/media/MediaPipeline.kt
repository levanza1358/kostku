package com.miraya.kostku.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.miraya.kostku.diag.AppLog
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/** Local media pipeline: foto asli beresolusi tinggi tidak pernah disimpan atau di-upload. */
object MediaPipeline {
    private const val MAX_SIDE = 1280
    private const val PREVIEW_SIDE = 640
    private const val MAX_BYTES = 500 * 1024
    private const val LOGO_SIDE = 512

    fun copyToCache(context: Context, uri: Uri): File {
        AppLog.mediaPipeline("copyToCache mulai")
        val out = File(context.cacheDir, "photo_${UUID.randomUUID()}.input")
        val input = context.contentResolver.openInputStream(uri)
            ?: run {
                AppLog.mediaFailure("copyToCache", "input stream null untuk uri")
                error("Foto tidak dapat dibaca.")
            }
        input.use { source -> out.outputStream().use { source.copyTo(it) } }
        AppLog.mediaPipeline("copyToCache selesai", "ukuran=${runCatching { out.length() }.getOrDefault(0L)} byte")
        return out
    }

    /** Kompres file yang sudah dipotong uCrop. */
    fun compress(input: File, context: Context): File {
        AppLog.mediaPipeline("compress mulai", "input=${runCatching { input.length() }.getOrDefault(0L)} byte")
        val bitmap = decodeSampled(input, MAX_SIDE) ?: run {
            AppLog.mediaFailure("compress", "format foto tidak didukung")
            error("Format foto tidak didukung.")
        }
        val result = encode(bitmap, input, context)
        AppLog.mediaPipeline("compress selesai", "output=${runCatching { result.length() }.getOrDefault(0L)} byte")
        return result
    }

    /**
     * Kompres logo lokasi: sisi terpanjang dibatasi [LOGO_SIDE] px dan kualitas dinaikkan
     * (tanpa pemotongan) supaya logo tampil tajam namun tetap kecil saat diunggah.
     */
    fun compressLogo(input: File, context: Context): File {
        AppLog.mediaPipeline("compressLogo mulai", "input=${runCatching { input.length() }.getOrDefault(0L)} byte")
        val bitmap = decodeSampled(input, LOGO_SIDE) ?: run {
            AppLog.mediaFailure("compressLogo", "format gambar tidak didukung")
            error("Format gambar tidak didukung.")
        }
        val result = encode(bitmap, input, context, maxSide = LOGO_SIDE, startQuality = 90)
        AppLog.mediaPipeline("compressLogo selesai", "output=${runCatching { result.length() }.getOrDefault(0L)} byte")
        return result
    }

    /** Bitmap kecil untuk pratinjau supaya tidak kehabisan memori. */
    fun previewBitmap(file: File): Bitmap? = decodeSampled(file, PREVIEW_SIDE)

    private fun decodeSampled(file: File, maxSide: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.path, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        var sample = 1
        var longest = maxOf(bounds.outWidth, bounds.outHeight)
        while (longest / (sample * 2) >= maxSide) sample *= 2
        val options = BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        return BitmapFactory.decodeFile(file.path, options)
    }

    private fun encode(original: Bitmap, source: File, context: Context, maxSide: Int = MAX_SIDE, startQuality: Int = 75): File {
        val exif = runCatching { ExifInterface(source.path) }.getOrNull()
        val oriented = Bitmap.createBitmap(
            original, 0, 0, original.width, original.height, exifMatrix(exif), true
        )
        if (oriented !== original) original.recycle()

        val scale = minOf(1f, maxSide.toFloat() / maxOf(oriented.width, oriented.height))
        val resized = if (scale < 1f) {
            Bitmap.createScaledBitmap(
                oriented,
                (oriented.width * scale).toInt().coerceAtLeast(1),
                (oriented.height * scale).toInt().coerceAtLeast(1),
                true
            )
        } else oriented
        if (resized !== oriented) oriented.recycle()

        val output = File(context.cacheDir, "photo_${UUID.randomUUID()}.jpg")
        var quality = startQuality
        do {
            FileOutputStream(output).use { resized.compress(Bitmap.CompressFormat.JPEG, quality, it) }
            quality -= 5
        } while (output.length() > MAX_BYTES && quality >= 45)
        resized.recycle()

        require(output.length() <= MAX_BYTES) {
            "Ukuran gambar terlalu besar. Silakan pilih gambar lain."
        }
        AppLog.mediaPipeline("encode selesai", "output=${output.length()} byte")
        return output
    }

    private fun exifMatrix(exif: ExifInterface?): Matrix = Matrix().apply {
        when (exif?.rotationDegrees ?: 0) {
            90 -> postRotate(90f)
            180 -> postRotate(180f)
            270 -> postRotate(270f)
        }
    }

    fun cleanup(vararg files: File?) {
        files.filterNotNull().forEach { runCatching { it.delete() } }
    }
}

data class PickedPhoto(val file: File, val displayName: String = "Foto")
