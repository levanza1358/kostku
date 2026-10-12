package com.miraya.kostku

import okhttp3.Dns
import java.io.IOException
import java.net.Inet4Address
import java.net.InetAddress

/** Gangguan sesaat (koneksi terputus / 5xx dari proxy/router) yang layak dicoba ulang. */
internal class TransientFailure(cause: Throwable? = null) : IOException("transient", cause)

/** Berapa kali percobaan ulang maksimum untuk gangguan sesaat. */
internal const val MAX_RETRIES = 3

/** Metode yang aman diulang otomatis (tanpa efek samping ganda). POST/PATCH tidak termasuk. */
internal val idempotentMethods = setOf("GET", "HEAD", "OPTIONS", "DELETE")

/** Kode respons dari lapisan proxy/server yang biasanya sementara. */
internal val transientCodes = setOf(408, 429, 502, 503, 504)

/** Jeda backoff (ms) naik dua kali tiap percobaan, maksimum 2 dtk. */
internal fun backoffMs(attempt: Int): Long = (300L shl (attempt - 1)).coerceAtMost(2000L)

/**
 * DNS yang mengutamakan IPv4. Server (Cloudflare) punya record A dan AAAA; sebagian jaringan
 * mengiklankan IPv6 tetapi tidak bisa merutekannya, sehingga percobaan IPv6 menggantung sampai
 * berlaku timeout. Dengan memakai IPv4 lebih dulu, permintaan tidak lagi "nyangkut".
 */
internal val ipv4FirstDns = object : Dns {
    override fun lookup(hostname: String): List<InetAddress> {
        val all = Dns.SYSTEM.lookup(hostname)
        val v4 = all.filterIsInstance<Inet4Address>()
        return v4.ifEmpty { all }
    }
}

/** Sesi ditolak server (401) — pengguna memang perlu masuk lagi (hanya dipakai untuk itu). */
internal class UnauthenticatedException(message: String) : IOException(message)

/** Profil diambil dari cache lokal karena server tidak terjangkau (offline) — sesi tetap sah. */
internal class CachedProfile(val profile: UserProfile) : IOException("offline")

/** Respons HTTP mentah (kode, isi, ETag) untuk jalur ber-cache seperti snapshot. */
internal class RawResponse(val code:Int, val body:String, val etag:String?)
