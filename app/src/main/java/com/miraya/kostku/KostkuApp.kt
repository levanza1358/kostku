package com.miraya.kostku

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.memory.MemoryCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Aplikasi Kostku.
 *
 * Memasang satu [ImageLoader] Coil yang dipakai seluruh aplikasi (lewat `asyncPainter`),
 * sehingga **setiap foto hanya diunduh sekali** lalu dilayani dari cache memori + disk —
 * bukan diunduh ulang tiap layar dirender ulang. Ini penghemat kuota terbesar, terutama
 * pada jaringan bergerak. Klien OkHttp bersama juga menghormati header `Cache-Control`/
 * `ETag` dari media CDN.
 */
class KostkuApp : Application(), SingletonImageLoader.Factory {
    override fun newImageLoader(context: PlatformContext): ImageLoader {
        // Klien bersama (memakai DNS IPv4-dulu + timeout seperti dipakai API) agar unduhan
        // gambar tidak "nyangkut" dan ikut memanfaatkan cache HTTP berbasis header server.
        val okHttp = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
        return ImageLoader.Builder(context)
            .components { add(OkHttpNetworkFetcherFactory(callFactory = { okHttp })) }
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.20)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(File(cacheDir, "image_cache").absoluteFile)
                    .maxSizeBytes(128L * 1024 * 1024)
                    .build()
            }
            .crossfade(true)
            .build()
    }
}
