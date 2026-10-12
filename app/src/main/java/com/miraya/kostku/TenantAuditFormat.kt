package com.miraya.kostku

import org.json.JSONObject

// Perubahan format entri audit penghuni (ringkasan ringkas + nilai ramah-baca).

/** Ringkasan detail entri audit menjadi teks Indonesia yang ramah. */
internal fun auditDetailText(entry: RemoteAuditEntry): String? {
    val raw = entry.details ?: return null
    return runCatching {
        val o = JSONObject(raw)
        when (entry.action) {
            "updated" -> {
                val arr = o.optJSONArray("fields")
                if (arr == null || arr.length() == 0) null else {
                    (0 until arr.length()).joinToString(", ") { auditFieldLabel(arr.getString(it)) }
                }
            }
            "moved_out" -> o.optString("move_out_date").takeIf { it.isNotBlank() && it != "null" }?.let { "Tanggal keluar: $it" }
            "extended" -> {
                val end = o.optString("move_out_date").takeIf { it.isNotBlank() && it != "null" }?.let { formatDate(it) }
                val amt = o.opt("amount")?.toString()?.toDoubleOrNull()?.toInt()?.let { rupiah(it) }
                listOfNotNull(end?.let { "Keluar s/d $it" }, amt?.let { "Bayar $it" }).joinToString(" • ").ifBlank { "Masa sewa diperpanjang" }
            }
            else -> null
        }
    }.getOrNull()
}
