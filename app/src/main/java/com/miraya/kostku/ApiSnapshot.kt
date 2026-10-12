package com.miraya.kostku

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

// Grup API untuk ringkasan (dashboard) dan snapshot tunggal ber-cache.

internal suspend fun KostkuApi.dashboard()=runCatching{val o=obj(request("dashboard/summary"));RemoteDashboard(o.optInt("properties"),o.optInt("rooms"),o.optInt("tenants"),o.optInt("current_payments"))}
/** Snapshot terakhir dari cache lokal (untuk tampilan instan / offline). */
internal suspend fun KostkuApi.cachedSnapshot():RemoteSnapshot?=withContext(Dispatchers.IO){ cache.readJson()?.let{ runCatching{ parseSnapshot(data(it) as? JSONObject ?: JSONObject()) }.getOrNull() } }
/** Hapus cache snapshot (dipakai saat logout/sesi dicabut). */
internal fun KostkuApi.clearCache()=cache.clear()
/**
 * Ambil snapshot dalam **satu permintaan** `GET /snapshot`, dengan validasi `If-None-Match`:
 * server menjawab **304** (tanpa isi) bila tidak ada perubahan — jadi pemuatan ulang
 * berikutnya nyaris tanpa kuota, tetapi tetap selalu sinkron dengan server.
 */
internal suspend fun KostkuApi.snapshot()=runCatching{ withContext(Dispatchers.IO){
    try {
        val r=rawGet("snapshot", buildMap<String,String> { cache.etag()?.let { put("If-None-Match", it) } })
        when {
            r.code==304 -> cache.readJson()?.let{ parseSnapshot(data(it) as? JSONObject ?: JSONObject()) }
                ?: throw IOException("Data tidak dapat dimuat.")
            else -> {
                val root=data(r.body) as? JSONObject ?: JSONObject()
                cache.save(r.body, r.etag)
                parseSnapshot(root)
            }
        }
    } catch (e: IOException) {
        // Backend lama (belum punya /snapshot) menjawab 404 → pakai jalur lama agar tidak
        // putus; begitu backend diperbarui, jalur /snapshot langsung dipakai kembali.
        if (e.message?.contains("tidak ditemukan")==true) legacySnapshot() else throw e
    }
} }
/** Jalur kompatibilitas: ambil tiap bagian dengan beberapa permintaan (backend lama). */
private suspend fun KostkuApi.legacySnapshot():RemoteSnapshot{
    val ps=arr(request("properties"));val ls=mutableListOf<RemoteLocation>();val rs=mutableListOf<RemoteRoom>();val rts=mutableListOf<RemoteRoomType>();val prs=mutableListOf<RemoteRoomTypePrice>()
    for(i in 0 until ps.length()){val p=ps.getJSONObject(i);val id=p.optString("id");ls+=locationFrom(p);val types=arr(request("properties/$id/room-types"));for(j in 0 until types.length()){val t=types.getJSONObject(j);val tid=t.optString("id");rts+=RemoteRoomType(tid,id,t.optString("name"));val pra=arr(request("room-types/$tid/prices"));for(k in 0 until pra.length()){val pp=pra.getJSONObject(k);prs+=RemoteRoomTypePrice(pp.optString("id"),tid,pp.optInt("occupancy"),pp.optInt("duration_value",1),pp.optString("duration_unit","month"),pp.optDouble("amount").toInt())}};val ra=arr(request("properties/$id/rooms"));for(j in 0 until ra.length()){rs+=roomFrom(ra.getJSONObject(j),id)}}
    val ta=arr(request("tenants"));val ts=(0 until ta.length()).map{val t=ta.getJSONObject(it);RemoteTenant(t.optString("id"),t.optString("full_name"),t.optString("room_id"),t.optString("phone"),t.optString("move_in_date"),t.optDouble("monthly_rent").toInt(),t.optString("due_day"),t.optString("status")=="active",t.optText("photo_url"),t.optText("move_out_at") ?: t.optText("move_out_date"))}
    val pa=arr(request("payments"));val pays=(0 until pa.length()).map{val p=pa.getJSONObject(it);RemotePayment(p.optString("id"),p.optString("tenant_id"),p.optDouble("amount").toInt(),p.optString("period_month"),p.optString("method"),p.optString("status"),p.optText("category"),p.optText("photo_url"),p.optText("paid_at"),p.optText("created_at"))}
    val ea=runCatching{arr(request("expenses"))}.getOrNull();val exps=if(ea==null)emptyList() else (0 until ea.length()).map{val e=ea.getJSONObject(it);RemoteExpense(e.optString("id"),e.optString("kind","operational"),e.optText("category"),e.optText("tenant_id"),e.optText("property_id"),e.optDouble("amount").toInt(),e.optString("spent_at"),e.optString("method","cash"),e.optText("notes"),e.optText("photo_url"))}
    val ca=runCatching{arr(request("categories"))}.getOrNull();val cats=if(ca==null)emptyList() else (0 until ca.length()).map{val c=ca.getJSONObject(it);RemoteCategory(c.optString("id"),c.optString("kind","expense"),c.optString("name"))}
    return RemoteSnapshot(ls,rs,rts,ts,pays,prs,exps,cats)
}
private fun JSONObject.optArray(key:String):JSONArray=optJSONArray(key)?:JSONArray()
private fun parseSnapshot(root:JSONObject):RemoteSnapshot{
    val pa=root.optArray("properties"); val ls=(0 until pa.length()).map{locationFrom(pa.getJSONObject(it))}
    val ta2=root.optArray("room_types"); val rts=(0 until ta2.length()).map{val t=ta2.getJSONObject(it);RemoteRoomType(t.optString("id"),t.optString("property_id"),t.optString("name"))}
    val pda=root.optArray("room_type_prices"); val prs=(0 until pda.length()).map{val p=pda.getJSONObject(it);RemoteRoomTypePrice(p.optString("id"),p.optString("room_type_id"),p.optInt("occupancy"),p.optInt("duration_value",1),p.optString("duration_unit","month"),p.optDouble("amount").toInt())}
    val ra=root.optArray("rooms"); val rs=(0 until ra.length()).map{roomFrom(ra.getJSONObject(it))}
    val tra=root.optArray("tenants"); val ts=(0 until tra.length()).map{val t=tra.getJSONObject(it);RemoteTenant(t.optString("id"),t.optString("full_name"),t.optString("room_id"),t.optString("phone"),t.optString("move_in_date"),t.optDouble("monthly_rent").toInt(),t.optString("due_day"),t.optString("status")=="active",t.optText("photo_url"),t.optText("move_out_at") ?: t.optText("move_out_date"))}
    val pma=root.optArray("payments"); val pays=(0 until pma.length()).map{val p=pma.getJSONObject(it);RemotePayment(p.optString("id"),p.optString("tenant_id"),p.optDouble("amount").toInt(),p.optString("period_month"),p.optString("method"),p.optString("status"),p.optText("category"),p.optText("photo_url"),p.optText("paid_at"),p.optText("created_at"))}
    val ea=root.optArray("expenses"); val exps=(0 until ea.length()).map{val e=ea.getJSONObject(it);RemoteExpense(e.optString("id"),e.optString("kind","operational"),e.optText("category"),e.optText("tenant_id"),e.optText("property_id"),e.optDouble("amount").toInt(),e.optString("spent_at"),e.optString("method","cash"),e.optText("notes"),e.optText("photo_url"))}
    val ca=root.optArray("categories"); val cats=(0 until ca.length()).map{val c=ca.getJSONObject(it);RemoteCategory(c.optString("id"),c.optString("kind","expense"),c.optString("name"))}
    return RemoteSnapshot(ls,rs,rts,ts,pays,prs,exps,cats)
}
