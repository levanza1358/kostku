package com.miraya.kostku.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Token jarak (spacing) tunggal untuk seluruh aplikasi.
 *
 * Semua layar memakai token ini alih-alih angka `.dp` lepas, sehingga padding dan
 * jarak antar elemen seragam di setiap halaman (Material 3).
 */
object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
}

/** Jarak standar tingkat layar. */
object Screen {
    /** Padding tepi kiri/kanan & atas/bawah untuk isi layar. */
    val edge = 16.dp

    /** Jarak antar blok/section utama di dalam layar. */
    val gap = 12.dp

    /** Ruang bawah daftar agar tidak tertutup FAB. */
    val fab = 96.dp

    /** Ruang bawah daftar biasa (tanpa FAB). */
    val bottom = 24.dp
}

/** Ukuran elemen bersama. */
object Dimens {
    /** Tinggi seragam tombol aksi utama. */
    val ButtonHeight = 52.dp
}

/** Sudut seragam untuk seluruh bidang isian (text field). */
val FieldShape = RoundedCornerShape(14.dp)
