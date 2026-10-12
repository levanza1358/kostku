package com.miraya.kostku

import android.content.Context
import java.io.File

/** Simpan token akses/refresh + profil dasar di SharedPreferences. */
internal class TokenStore(context:Context){private val p=context.getSharedPreferences("kostku_auth",0);var accessToken:String? get()=p.getString("access_token",null);set(v){p.edit().putString("access_token",v).apply()};var refreshToken:String? get()=p.getString("refresh_token",null);set(v){p.edit().putString("refresh_token",v).apply()};var profileJson:String? get()=p.getString("profile_json",null);set(v){p.edit().putString("profile_json",v).apply()};fun clear(){p.edit().clear().apply()}}

/**
 * Cache lokal snapshot terakhir beserta ETag-nya. Isi disimpan sebagai berkas (bukan
 * SharedPreferences) karena bisa cukup besar; ETag kecil disimpan di preferensi. Inilah
 * yang membuat aplikasi tetap "fully sinkron" tanpa mengunduh ulang tiap kali dibuka:
 * snapshot ditampilkan dari cache lebih dulu, lalu divalidasi ke server lewat `If-None-Match`
 * (server menjawab 304 tanpa isi bila tak ada perubahan).
 */
internal class SnapshotCache(private val context:Context){
    private val file=File(context.filesDir,"kostku_snapshot.json")
    private val prefs=context.getSharedPreferences("kostku_cache",0)
    fun save(json:String, etag:String?){ runCatching{ file.writeText(json) }; prefs.edit().putString("snapshot_etag",etag).apply() }
    fun readJson():String? = runCatching{ file.takeIf { it.isFile }?.readText() }.getOrNull()?.takeIf { it.isNotBlank() }
    fun etag():String? = prefs.getString("snapshot_etag",null)
    fun clear(){ runCatching{ file.delete() }; prefs.edit().remove("snapshot_etag").apply() }
}
