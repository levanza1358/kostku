package com.miraya.kostku

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

/** Menguji perhitungan tanggal keluar dari durasi harga (bug lama selalu memakai bulan). */
class MoveOutCalculationTest {

    private val start = LocalDate.of(2026, 1, 10)

    @Test
    fun durasi_hari() {
        assertEquals(LocalDate.of(2026, 1, 17), addDuration(start, 7, "day"))
    }

    @Test
    fun durasi_minggu() {
        assertEquals(LocalDate.of(2026, 1, 24), addDuration(start, 2, "week"))
    }

    @Test
    fun durasi_bulan() {
        assertEquals(LocalDate.of(2026, 4, 10), addDuration(start, 3, "month"))
    }

    @Test
    fun durasi_tahun() {
        assertEquals(LocalDate.of(2027, 1, 10), addDuration(start, 1, "year"))
    }

    @Test
    fun satuan_tidak_dikenal_memakai_bulan() {
        assertEquals(LocalDate.of(2026, 2, 10), addDuration(start, 1, "???"))
    }
}
