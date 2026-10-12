package com.miraya.kostku

import org.junit.Assert.assertEquals
import org.junit.Test

/** Menguji pemformatan angka dan label yang dipakai di seluruh UI. */
class UiFormatTest {

    @Test
    fun rupiah_memakai_pemisah_ribuan_titik() {
        assertEquals("Rp 0", rupiah(0))
        assertEquals("Rp 1.500", rupiah(1500))
        assertEquals("Rp 1.234.567", rupiah(1234567))
    }

    @Test
    fun label_status_kamar() {
        assertEquals("Tersedia", roomStatusLabel("vacant"))
        assertEquals("Terisi", roomStatusLabel("occupied"))
        assertEquals("Maintenance", roomStatusLabel("maintenance"))
        assertEquals("Kotor", roomStatusLabel("dirty"))
        assertEquals("Status tidak dikenal", roomStatusLabel("???"))

    }

    @Test
    fun label_status_pembayaran() {
        assertEquals("Lunas", paymentStatusLabel("paid"))
        assertEquals("Belum", paymentStatusLabel("pending"))
        assertEquals("Sebagian", paymentStatusLabel("partial"))
        assertEquals("Terlambat", paymentStatusLabel("late"))
        assertEquals("Dibatalkan", paymentStatusLabel("cancelled"))
        assertEquals("Lunas", paymentStatusLabel("PAID"))
    }

    @Test
    fun label_satuan_durasi() {
        assertEquals("hari", durationUnitLabel("day"))
        assertEquals("minggu", durationUnitLabel("week"))
        assertEquals("bulan", durationUnitLabel("month"))
        assertEquals("tahun", durationUnitLabel("year"))
        assertEquals("3 bulan", durationSummary(3, "month"))
    }

    @Test
    fun label_aksi_log_perubahan() {
        assertEquals("Penghuni dibuat", auditActionLabel("created"))
        assertEquals("Data diperbarui", auditActionLabel("updated"))
        assertEquals("Pindah kamar", auditActionLabel("moved"))
        assertEquals("Ditandai keluar", auditActionLabel("moved_out"))
        assertEquals("Diarsipkan", auditActionLabel("archived"))
        assertEquals("Hapus permanen", auditActionLabel("hapus_permanen"))
    }

    @Test
    fun label_field_log_perubahan() {
        assertEquals("Nama", auditFieldLabel("full_name"))
        assertEquals("Nomor HP", auditFieldLabel("phone"))
        assertEquals("Harga sewa", auditFieldLabel("monthly_rent"))
        assertEquals("Tanggal keluar", auditFieldLabel("move_out_date"))
        assertEquals("Jatuh tempo", auditFieldLabel("due_day"))
        assertEquals("Kamar", auditFieldLabel("room_id"))
    }

    @Test
    fun waktu_log_diformat_rapi() {
        assertEquals("10 Okt 2026 • 10:32", auditTimestamp("2026-10-10 10:32:24"))
        // Nilai tak terduga dikembalikan apa adanya (tidak crash).
        assertEquals("bukan-waktu", auditTimestamp("bukan-waktu"))
    }

    @Test
    fun tanggal_diformat_dd_mmm_yyyy() {
        assertEquals("01 Jan 2026", formatDate(java.time.LocalDate.of(2026, 1, 1)))
        assertEquals("10 Okt 2026", formatDate(java.time.LocalDate.of(2026, 10, 10)))
        assertEquals("10 Okt 2026", formatDate("2026-10-10"))
        assertEquals("10 Okt 2026", formatDate("2026-10-10 00:00:00"))
        assertEquals("-", formatDate(null as String?))
        assertEquals("bukan-tanggal", formatDate("bukan-tanggal"))
    }

    @Test
    fun sisa_hari_dihitung_dari_hari_ini() {
        assertEquals(null, daysUntil(null))
        assertEquals(null, daysUntil(""))
        assertEquals(null, daysUntil("bukan-tanggal"))
        assertEquals(5L, daysUntil(java.time.LocalDate.now().plusDays(5).toString()))
        assertEquals(-3L, daysUntil(java.time.LocalDate.now().minusDays(3).toString()))
    }

    @Test
    fun label_sisa_hari_untuk_chip() {
        assertEquals("Sisa 31 hari", dayLeftLabel(31))
        assertEquals("Sisa 0 hari", dayLeftLabel(0))
        assertEquals("Lewat 4 hari", dayLeftLabel(-4))
        assertEquals("Tanpa tanggal keluar", dayLeftLabel(null))
    }

    @Test
    fun teks_utilitas_kosong_menjadi_tidak_tersedia() {
        assertEquals("Tidak tersedia", utilityText(null))
        assertEquals("Tidak tersedia", utilityText(""))
        assertEquals("Tidak tersedia", utilityText("   "))
        assertEquals("1234 5678", utilityText("1234 5678"))
    }

    @Test
    fun label_cap_kwitansi() {
        assertEquals("LUNAS", receiptStatusLabel("paid"))
        assertEquals("LUNAS", receiptStatusLabel("PAID"))
        assertEquals("SEBAGIAN", receiptStatusLabel("partial"))
        assertEquals("BELUM LUNAS", receiptStatusLabel("pending"))
        assertEquals("BELUM LUNAS", receiptStatusLabel("late"))
        assertEquals("DIBATALKAN", receiptStatusLabel("cancelled"))
    }
}
