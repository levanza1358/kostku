package com.miraya.kostku

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Format mata uang Rupiah tanpa desimal, pemisah ribuan titik. */
fun rupiah(value: Int) = "Rp " + java.lang.String.format(java.util.Locale.US, "%,d", value).replace(',', '.')

/**
 * Sisa hari menuju tanggal keluar dari teks ISO (negatif bila sudah lewat), atau null
 * bila tanggal tidak diketahui / tidak dapat diurai. Dihitung terhadap tanggal hari ini.
 */
fun daysUntil(raw: String?): Long? = raw?.take(10)?.takeIf { it.isNotBlank() }?.let {
    runCatching { ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(it)) }.getOrNull()
}

/** Teks utilitas (token listrik / PDAM): nilai apa adanya, atau "Tidak tersedia" bila kosong. */
fun utilityText(value: String?): String = value?.takeIf { it.isNotBlank() } ?: "Tidak tersedia"

/** Label jarak hari untuk chip daftar penghuni: "Sisa N hari", "Lewat N hari", atau tanpa tanggal. */
fun dayLeftLabel(days: Long?): String = when {
    days == null -> "Tanpa tanggal keluar"
    days >= 0 -> "Sisa $days hari"
    else -> "Lewat ${-days} hari"
}

/** Label status kamar dalam Bahasa Indonesia. */
fun roomStatusLabel(status: String) = when (status) {
    "vacant" -> "Tersedia"
    "occupied" -> "Terisi"
    "maintenance" -> "Maintenance"
    "dirty" -> "Kotor"
    else -> "Status tidak dikenal"
}

/** Label status pembayaran dalam Bahasa Indonesia. */
fun paymentStatusLabel(status: String) = when (status.lowercase()) {
    "paid" -> "Lunas"
    "partial" -> "Sebagian"
    "pending" -> "Belum"
    "late" -> "Terlambat"
    "cancelled" -> "Dibatalkan"
    else -> "Status pembayaran tidak dikenal"
}

/** Label jenis pengeluaran: operasional (biaya kos) atau refund (kembali ke penghuni). */
fun expenseKindLabel(kind: String) = when (kind.lowercase()) {
    "refund" -> "Refund"
    "operational" -> "Operasional"
    else -> kind.replace('_', ' ').replaceFirstChar { it.uppercase() }
}

/** Cap kwitansi: "LUNAS" hanya bila pembayaran benar-benar `paid`. */
fun receiptStatusLabel(status: String) = when (status.lowercase()) {
    "paid" -> "LUNAS"
    "partial" -> "SEBAGIAN"
    "cancelled" -> "DIBATALKAN"
    else -> "BELUM LUNAS"
}

/** Nomor kwitansi turunan (deterministik) dari catatan pembayaran: "KW-YYYY-NNNN". */
fun receiptNumber(id: String, createdAt: String?): String {
    val year = createdAt?.take(4)?.takeIf { it.length == 4 && it.all(Char::isDigit) } ?: LocalDate.now().year.toString()
    val serial = (id.filter(Char::isLetterOrDigit).hashCode() and 0x7fffffff) % 9000 + 1000
    return "KW-$year-%04d".format(serial)
}

/** Label metode pembayaran/pengeluaran. */
fun paymentMethodLabel(method: String) = when (method.lowercase()) {
    "cash" -> "Tunai"
    "transfer" -> "Transfer"
    "other" -> "Lainnya"
    else -> method
}

/** Label satuan durasi harga: day/week/month/year → hari/minggu/bulan/tahun. */
fun durationUnitLabel(unit: String) = when (unit.lowercase()) {
    "day" -> "hari"
    "week" -> "minggu"
    "year" -> "tahun"
    else -> "bulan"
}

/** Ringkasan durasi, mis. "3 bulan". */
fun durationSummary(value: Int, unit: String) = "$value ${durationUnitLabel(unit)}"

/**
 * Tanggal keluar dihitung dari durasi harga. Menghormati satuan hari/minggu/bulan/tahun,
 * bukan hanya bulan seperti versi sebelumnya.
 */
fun addDuration(start: LocalDate, value: Int, unit: String): LocalDate = when (unit.lowercase()) {
    "day" -> start.plusDays(value.toLong())
    "week" -> start.plusWeeks(value.toLong())
    "year" -> start.plusYears(value.toLong())
    else -> start.plusMonths(value.toLong())
}

/** Judul aksi pada log perubahan data penghuni. */
fun auditActionLabel(action: String) = when (action) {
    "created" -> "Penghuni dibuat"
    "updated" -> "Data diperbarui"
    "moved" -> "Pindah kamar"
    "moved_out" -> "Ditandai keluar"
    "extended" -> "Diperpanjang"
    "archived" -> "Diarsipkan"
    else -> action.replace('_', ' ').replaceFirstChar { it.uppercase() }
}

private val bulanSingkat = arrayOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des")

/** Format tanggal Indonesia: LocalDate(2026-10-10) → "10 Okt 2026". */
fun formatDate(d: LocalDate): String = "%02d %s %d".format(d.dayOfMonth, bulanSingkat[d.monthValue - 1], d.year)

/** Label bulan-tahun Indonesia: (2026, 10) → "Okt 2026". */
fun monthLabel(year: Int, month: Int): String = "%s %d".format(bulanSingkat[(month - 1).coerceIn(0, 11)], year)

private val bulanPanjang = arrayOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")

/** Label bulan-tahun panjang: (2026, 10) → "Oktober 2026". */
fun monthLabelLong(year: Int, month: Int): String = "%s %d".format(bulanPanjang[(month - 1).coerceIn(0, 11)], year)

/**
 * Format tanggal dari teks ISO untuk tampilan: "2026-10-10" atau "2026-10-10 00:00:00"
 * → "10 Okt 2026". Mengembalikan teks apa adanya bila gagal diurai.
 */
fun formatDate(raw: String?): String {
    if (raw.isNullOrBlank()) return "-"
    return runCatching { formatDate(LocalDate.parse(raw.take(10))) }.getOrDefault(raw)
}

/** Format waktu log: "2026-10-10 10:32:24" → "10 Okt 2026 • 10:32". Aman bila gagal diurai. */
fun auditTimestamp(raw: String): String = runCatching {
    val dt = java.time.LocalDateTime.parse(raw.take(19).replace(' ', 'T'))
    "%s • %02d:%02d".format(formatDate(dt.toLocalDate()), dt.hour, dt.minute)
}.getOrDefault(raw)

/** Label field pada log perubahan (dipakai pada aksi "updated"). */
fun auditFieldLabel(field: String) = when (field) {
    "full_name" -> "Nama"
    "phone" -> "Nomor HP"
    "email" -> "Email"
    "identity_number" -> "No. identitas"
    "notes" -> "Catatan"
    "monthly_rent" -> "Harga sewa"
    "due_day" -> "Jatuh tempo"
    "move_in_date" -> "Tanggal masuk"
    "move_out_date" -> "Tanggal keluar"
    "room_id" -> "Kamar"
    else -> field.replace('_', ' ').replaceFirstChar { it.uppercase() }
}
