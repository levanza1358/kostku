package com.miraya.kostku

import android.content.Context
import com.miraya.kostku.diag.AppLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.io.IOException

/**
 * Klien HTTP inti: membangun permintaan, menyisipkan token, menangani percobaan ulang
 * untuk gangguan sesaat, dan menyegarkan token saat 401. Metode grup API (auth, properti,
 * penghuni, pembayaran, media) berada di berkas `Api*.kt` sebagai fungsi ekstensi
 * `KostkuApi.*` yang memakai mesin di kelas ini.
 */
internal class KostkuApi(context:Context){
 companion object{const val BASE_URL="https://kostku.miraya.my.id/v1/"}
 internal val tokens=TokenStore(context);internal val cache=SnapshotCache(context);internal val client=OkHttpClient.Builder().dns(ipv4FirstDns).connectTimeout(10,java.util.concurrent.TimeUnit.SECONDS).readTimeout(60,java.util.concurrent.TimeUnit.SECONDS).writeTimeout(60,java.util.concurrent.TimeUnit.SECONDS).retryOnConnectionFailure(true).build()
 internal suspend fun request(path: String, method: String = "GET", payload: JSONObject? = null, auth: Boolean = true): String = withContext(Dispatchers.IO) {
     // Hanya permintaan tanpa efek samping ganda yang diulang otomatis. POST/PATCH tidak
     // pernah diulang otomatis (mis. pembayaran) agar tidak menggandakan data; kegagalan
     // sesaat pada POST/PATCH ditampilkan ke pengguna untuk dicoba ulang manual.
     val retryable = idempotentMethods.contains(method)
     var attempt = 0
     while (true) {
         try {
             return@withContext executeRequest(path, method, payload, auth, allowRefresh = true)
         } catch (e: TransientFailure) {
             attempt++
             if (!retryable || attempt >= MAX_RETRIES) throw friendly(e)
             AppLog.w("[HTTP] gangguan sesaat pada $method $path — coba ulang ke-$attempt")
             delay(backoffMs(attempt))
         }
     }
     @Suppress("UNREACHABLE_CODE") throw IOException("Koneksi ke server gagal. Silakan coba lagi.")
 }
 /**
  * Permintaan GET ber-header tambahan ([extraHeaders], mis. `If-None-Match`) yang
  * mengembalikan **respons mentah** — termasuk **304 Not Modified** sebagai hasil sah
  * (bukan error), supaya pemanggil dapat memakai cache lokal tanpa mengunduh ulang isi.
  * Percobaan ulang & alur refresh-token sama seperti [request].
  */
 internal suspend fun rawGet(path: String, extraHeaders: Map<String,String>): RawResponse = withContext(Dispatchers.IO) {
     var attempt = 0
     while (true) {
         try {
             return@withContext executeRawGet(path, extraHeaders, allowRefresh = true)
         } catch (e: TransientFailure) {
             attempt++
             if (attempt >= MAX_RETRIES) throw friendly(e)
             AppLog.w("[HTTP] gangguan sesaat pada GET(cache) $path — coba ulang ke-$attempt")
             delay(backoffMs(attempt))
         }
     }
     @Suppress("UNREACHABLE_CODE") throw IOException("Koneksi ke server gagal. Silakan coba lagi.")
 }
 private suspend fun executeRawGet(path: String, extraHeaders: Map<String,String>, allowRefresh: Boolean): RawResponse {
     val builder = Request.Builder().url(BASE_URL + path).header("Accept", "application/json")
     val tokenPresent = !tokens.accessToken.isNullOrBlank()
     tokens.accessToken?.let { builder.header("Authorization", "Bearer $it") }
     extraHeaders.forEach { (k, v) -> builder.header(k, v) }
     AppLog.httpRequest("GET", path, tokenPresent, false)
     val response = try { client.newCall(builder.build()).execute() } catch (e: IOException) {
         AppLog.e("[HTTP] koneksi gagal GET(cache) $path - ${e.message}", e)
         throw TransientFailure(e)
     }
     response.use { r ->
         val body = r.body?.string().orEmpty()
         AppLog.httpResponse("GET", path, r.code, if (r.isSuccessful) null else AppLog.safeJsonSnippet(body))
         if (r.code == 401) {
             if (allowRefresh) { handleUnauthorized(path); return executeRawGet(path, extraHeaders, allowRefresh = false) }
             throw UnauthenticatedException("Sesi login telah berakhir. Silakan masuk kembali.")
         }
         if (transientCodes.contains(r.code)) { AppLog.w("[HTTP] status sementara ${r.code} pada GET(cache) $path"); throw TransientFailure() }
         if (!r.isSuccessful && r.code != 304) failWith(r.code, "GET", path, body)
         return RawResponse(r.code, body, r.header("ETag"))
     }
 }
 private suspend fun executeRequest(path: String, method: String, payload: JSONObject?, auth: Boolean, allowRefresh: Boolean): String {
     val builder = Request.Builder().url(BASE_URL + path).header("Accept", "application/json")
     val tokenPresent = auth && !tokens.accessToken.isNullOrBlank()
     if (auth) tokens.accessToken?.let { builder.header("Authorization", "Bearer $it") }
     if (payload != null) builder.header("Content-Type", "application/json").method(method, payload.toString().toRequestBody("application/json".toMediaType()))
     else builder.method(method, null)
     AppLog.httpRequest(method, path, tokenPresent, payload != null)
     val response = try { client.newCall(builder.build()).execute() } catch (e: IOException) {
         AppLog.e("[HTTP] koneksi gagal $method $path - ${e.message}", e)
         throw TransientFailure(e)
     }
     response.use { r ->
         val body = r.body?.string().orEmpty()
         AppLog.httpResponse(method, path, r.code, if (r.isSuccessful) null else AppLog.safeJsonSnippet(body))
         if (r.code == 401 && auth) {
             if (allowRefresh) { handleUnauthorized(path); return executeRequest(path, method, payload, auth, allowRefresh = false) }
             throw UnauthenticatedException("Sesi login telah berakhir. Silakan masuk kembali.")
         }
         if (transientCodes.contains(r.code)) { AppLog.w("[HTTP] status sementara ${r.code} pada $method $path"); throw TransientFailure() }
         if (!r.isSuccessful) failWith(r.code, method, path, body)
         return body
     }
 }
 /**
  * Segarkan token setelah 401. Berhasil → lanjut; sesi ditolak server → sesi dibersihkan &
  * [UnauthenticatedException]; gangguan jaringan → [TransientFailure] (sesi TIDAK dihapus).
  */
 internal fun handleUnauthorized(path: String) {
     if (!tokens.refreshToken.isNullOrBlank()) {
         AppLog.httpRefresh(path, false)
         when (refreshAccessToken()) {
             true -> { AppLog.httpRefresh(path, true); return }
             false -> { tokens.clear(); throw UnauthenticatedException("Sesi login telah berakhir. Silakan masuk kembali.") }
             null -> throw TransientFailure()
         }
     }
     throw UnauthenticatedException("Sesi login telah berakhir. Silakan masuk kembali.")
 }
 internal fun failWith(code: Int, method: String, path: String, body: String): Nothing {
     val userMessage = "${statusMessage(code)} ${errorMessage(body)}"
     AppLog.httpParsedError(method, path, code, userMessage.trim(), AppLog.safeJsonSnippet(body))
     throw IOException(userMessage)
 }
 /** Terjemahkan kegagalan jaringan teknis menjadi pesan yang ramah untuk pengguna. */
 private fun friendly(e: TransientFailure): IOException {
     val c = e.cause
     val msg = when {
         c is java.net.UnknownHostException -> "Tidak dapat terhubung ke server. Periksa koneksi internet Anda."
         c is java.net.SocketTimeoutException -> "Koneksi ke server terlalu lama. Silakan coba lagi."
         c is java.net.ConnectException -> "Server sedang tidak dapat dihubungi. Silakan coba lagi sebentar."
         else -> "Koneksi ke server gagal. Silakan coba lagi."
     }
     AppLog.e("[HTTP] kegagalan jaringan akhir - ${c?.message ?: "tanpa detail"}", c)
     return IOException(msg, c)
 }
 /**
  * Menyegarkan token. Mengembalikan true bila berhasil, false bila server menolak sesi,
  * dan null bila hanya gangguan jaringan (sesi tidak boleh dianggap berakhir).
  */
 private fun refreshAccessToken(): Boolean? {
     val refresh = tokens.refreshToken ?: return false
     AppLog.i("[HTTP] -> POST auth/refresh (token refresh: ada)")
     val payload = JSONObject().put("refresh_token", refresh)
     val builder = Request.Builder()
         .url(BASE_URL + "auth/refresh")
         .header("Accept", "application/json")
         .header("Content-Type", "application/json")
         .post(payload.toString().toRequestBody("application/json".toMediaType()))
     // Koneksi bermasalah → null (tidak menyalahkan sesi); server menolak → false.
     val response = try { client.newCall(builder.build()).execute() } catch (e: IOException) {
         AppLog.e("[HTTP] koneksi gagal saat refresh token - ${e.message}", e); return null
     }
     response.use { r ->
         val body = r.body?.string().orEmpty()
         AppLog.httpResponse("POST", "auth/refresh", r.code)
         if (r.code in transientCodes) return null
         if (!r.isSuccessful) return false
         val root = JSONTokener(body).nextValue() as? JSONObject ?: return false
         val data = root.optJSONObject("data") ?: root
         val access = data.optString("access_token").takeIf { it.isNotBlank() } ?: return false
         tokens.accessToken = access
         data.optString("refresh_token").takeIf { it.isNotBlank() }?.let { tokens.refreshToken = it }
         return true
     }
 }
 private fun statusMessage(code: Int): String = when (code) {
     400 -> "Data tidak valid."
     401 -> "Sesi login tidak valid."
     403 -> "Anda tidak memiliki izin untuk melakukan tindakan ini."
     404 -> "Data atau alamat layanan tidak ditemukan."
     409 -> "Data bertentangan dengan data yang sudah ada."
     500 -> "Terjadi kesalahan pada server."
     else -> "Permintaan ke server gagal. Silakan coba lagi."
 }
 private fun errorMessage(response: String): String {
     val serverMessage = runCatching { JSONObject(response).optJSONObject("error")?.optString("message") }
         .getOrNull().orEmpty()
     AppLog.d("[HTTP] pesan error server (verbatim): ${serverMessage.ifBlank { "(kosong)" }}")
     return when {
         serverMessage.contains("Price entry already exists", ignoreCase = true) -> "Harga untuk jumlah penghuni dan durasi tersebut sudah terdaftar. Silakan gunakan kombinasi lain."
         serverMessage.contains("capacity required", ignoreCase = true) -> "Data tipe kamar perlu diperbarui. Silakan coba lagi."
         serverMessage.contains("room type is referenced", ignoreCase = true) -> "Tipe kamar tidak dapat dihapus karena masih digunakan oleh kamar."
         serverMessage.contains("referenced", ignoreCase = true) -> "Data tidak dapat dihapus karena masih digunakan oleh data lain."
         serverMessage.contains("not found", ignoreCase = true) -> "Data yang diminta tidak ditemukan."
         serverMessage.contains("unauthorized", ignoreCase = true) -> "Sesi login telah berakhir. Silakan masuk kembali."
         serverMessage.contains("unavailable", ignoreCase = true) -> "Data tujuan tidak tersedia atau sedang digunakan."
         serverMessage.contains("network request failed", ignoreCase = true) -> "Koneksi ke server gagal. Periksa internet Anda."
         else -> "Permintaan ke server gagal. Silakan coba lagi."
     }
 }
 /** Ambil bagian `data` dari respons (bila objek) atau nilai apa adanya. */
 internal fun data(s:String):Any{val v=JSONTokener(s).nextValue();return if(v is JSONObject)v.opt("data")?:v else v}
 internal fun obj(s:String)=data(s) as JSONObject
 internal fun arr(s:String)=data(s) as? JSONArray?:JSONArray()
}
