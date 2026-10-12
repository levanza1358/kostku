package com.miraya.kostku.diag

import android.util.Log
import org.json.JSONObject

/**
 * Helper logging terpusat untuk aplikasi Kostku.
 *
 * Semua log memakai satu TAG yang sama yaitu "Kostku" agar mudah difilter di Logcat:
 *     adb logcat -s Kostku
 *
 * Aturan keamanan (WAJIB dipatuhi):
 *  - JANGAN pernah mencatat token, password, secret, atau header Authorization penuh.
 *  - JANGAN pernah mencatat isi byte foto.
 * Yang boleh dicatat: nama method, path, status HTTP, potongan kecil body respons,
 * ID entitas, ukuran byte, dan keputusan yang dilihat pengguna.
 *
 * Semua pemformatan yang berisiko dibungkus [runCatching] supaya logging tidak pernah
 * melempar exception atau mengubah perilaku aplikasi.
 */
object AppLog {

    /** Tag tunggal untuk semua log. Filter di Logcat dengan tag ini. */
    const val TAG = "Kostku"

    /** Panjang maksimum potongan body respons yang boleh dicatat. */
    private const val MAX_SNIPPET = 300

    // ---- Level dasar -------------------------------------------------------------------

    fun d(message: String) {
        runCatching { Log.d(TAG, message) }
    }

    fun i(message: String) {
        runCatching { Log.i(TAG, message) }
    }

    fun w(message: String) {
        runCatching { Log.w(TAG, message) }
    }

    fun e(message: String, throwable: Throwable? = null) {
        runCatching { if (throwable != null) Log.e(TAG, message, throwable) else Log.e(TAG, message) }
    }

    // ---- Helper terstruktur ------------------------------------------------------------

    /** Mencatat awal sebuah aksi dengan nama aksi dan detail opsional. */
    fun actionStart(action: String, detail: String? = null) {
        i(prefix("MULAI", action) + detailPart(detail))
    }

    /** Mencatat aksi berhasil. */
    fun actionSuccess(action: String, detail: String? = null) {
        i(prefix("SUKSES", action) + detailPart(detail))
    }

    /** Mencatat aksi gagal beserta pesan aslinya (verbatim). */
    fun actionFailure(action: String, message: String?) {
        e(prefix("GAGAL", action) + detailPart(message ?: "tanpa pesan"))
    }

    /** Mencatat langkah individual dalam sebuah alur. */
    fun step(action: String, step: String, detail: String? = null) {
        d("[$action] langkah: $step" + detailPart(detail))
    }

    /** Mencatat pesan snackbar yang benar-benar ditampilkan ke pengguna. */
    fun snackbar(message: String) {
        i("[Snackbar] ditampilkan: $message")
    }

    /** Mencatat keputusan yang berdampak ke pengguna (mis. tombol aktif/nonaktif). */
    fun decision(screen: String, decision: String, detail: String? = null) {
        d("[$screen] keputusan: $decision" + detailPart(detail))
    }

    // ---- HTTP --------------------------------------------------------------------------

    /** Mencatat request keluar. TIDAK pernah mencatat token; hanya status ada/tidak. */
    fun httpRequest(method: String, path: String, tokenPresent: Boolean, hasPayload: Boolean) {
        i(
            "[HTTP] -> ${method.uppercase()} $path " +
                "(token: ${if (tokenPresent) "ada" else "tidak ada"}, body: ${if (hasPayload) "ada" else "tidak ada"})"
        )
    }

    /** Mencatat status code respons dan potongan kecil body. */
    fun httpResponse(method: String, path: String, status: Int, bodySnippet: String? = null) {
        // 304 = "tidak berubah, pakai cache" — hasil yang normal, bukan error.
        val level = when { status in 200..299 -> "OK"; status == 304 -> "SAMA"; else -> "ERROR" }
        i("[HTTP] <- ${method.uppercase()} $path status=$status ($level)" + snippetPart(bodySnippet))
    }

    /** Mencatat percobaan refresh token / redirect. Tidak mencatat token itu sendiri. */
    fun httpRefresh(path: String, success: Boolean) {
        i("[HTTP] refresh token untuk $path: ${if (success) "berhasil" else "gagal"}")
    }

    /** Mencatat pesan error hasil parsing yang akan ditampilkan ke pengguna. */
    fun httpParsedError(method: String, path: String, status: Int, userMessage: String, rawSnippet: String? = null) {
        e(
            "[HTTP] parsed-error ${method.uppercase()} $path status=$status -> \"$userMessage\"" +
                snippetPart(rawSnippet)
        )
    }

    // ---- Media -------------------------------------------------------------------------

    fun mediaPipeline(stage: String, detail: String? = null) {
        d("[MediaPipeline] $stage" + detailPart(detail))
    }

    fun mediaFailure(stage: String, message: String?) {
        w("[MediaPipeline] GAGAL $stage: ${message ?: "tanpa pesan"}")
    }

    // ---- Internal ----------------------------------------------------------------------

    private fun prefix(kind: String, action: String) = "[Aksi][$kind] $action"

    private fun detailPart(detail: String?) = if (detail.isNullOrBlank()) "" else " - $detail"

    private fun snippetPart(snippet: String?): String {
        val safe = runCatching { snippet?.trim()?.replace(Regex("\\s+"), " ") }.getOrNull()
        if (safe.isNullOrBlank()) return ""
        val truncated = if (safe.length > MAX_SNIPPET) safe.take(MAX_SNIPPET) + "…" else safe
        return " body=\"$truncated\""
    }

    /**
     * Membuat potongan kecil dari body JSON respons untuk keperluan log.
     * Menghapus field sensitif (token/password/secret) bila ada.
     * Selalu aman: mengembalikan null bila gagal.
     */
    fun safeJsonSnippet(raw: String?): String? = runCatching {
        if (raw.isNullOrBlank()) return null
        val trimmed = raw.trim()
        if (!trimmed.startsWith("{")) return trimmed
        val json = JSONObject(trimmed)
        listOf("access_token", "refresh_token", "password", "secret", "token", "id_token").forEach { json.remove(it) }
        json.optJSONObject("data")?.let { d -> listOf("access_token", "refresh_token").forEach { d.remove(it) } }
        json.toString()
    }.getOrNull()
}
