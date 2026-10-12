package com.miraya.kostku

import com.miraya.kostku.diag.AppLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException

// Grup API untuk unggah/hapus media (foto & logo).

internal suspend fun KostkuApi.uploadPhoto(type: String, entityId: String, file: File): JSONObject = withContext(Dispatchers.IO) {
    executeMultipartPhoto(type, entityId, file, allowRefresh = true)
}

private fun KostkuApi.executeMultipartPhoto(
    type: String,
    entityId: String,
    file: File,
    allowRefresh: Boolean
): JSONObject {
    val path = "media/$type/$entityId"
    if (!file.exists() || file.length() <= 0L) {
        AppLog.mediaFailure("uploadPhoto", "file tidak ada atau kosong: ${file.absolutePath}")
        throw IOException("Foto tidak tersedia. Silakan pilih foto lagi.")
    }
    AppLog.i("[HTTP] -> POST $path (multipart, ukuran=${file.length()} byte, token: ${if (!tokens.accessToken.isNullOrBlank()) "ada" else "tidak ada"})")
    val body = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart(
            "file",
            file.name,
            file.readBytes().toRequestBody("image/jpeg".toMediaType())
        )
        .build()
    val request = Request.Builder()
        .url(KostkuApi.BASE_URL + path)
        .header("Accept", "application/json")
        .apply { tokens.accessToken?.let { header("Authorization", "Bearer $it") } }
        .post(body)
        .build()
    val response = try { client.newCall(request).execute() } catch (e: IOException) {
        AppLog.e("[HTTP] koneksi gagal POST $path - ${e.message}", e)
        throw TransientFailure(e)
    }
    response.use { r ->
        val text = r.body?.string().orEmpty()
        AppLog.httpResponse("POST", path, r.code, if (r.isSuccessful) null else AppLog.safeJsonSnippet(text))
        if (r.code == 401) {
            if (allowRefresh) { handleUnauthorized(path); return executeMultipartPhoto(type, entityId, file, allowRefresh = false) }
            throw UnauthenticatedException("Sesi login telah berakhir. Silakan masuk kembali.")
        }
        if (transientCodes.contains(r.code)) { AppLog.w("[HTTP] status sementara ${r.code} pada POST $path"); throw TransientFailure() }
        if (!r.isSuccessful) failWith(r.code, "POST", path, text)
        return obj(text)
    }
}
internal suspend fun KostkuApi.deletePhoto(type:String, entityId:String)=runCatching{request("media/$type/$entityId","DELETE")}
