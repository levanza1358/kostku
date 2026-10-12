package com.miraya.kostku.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Satu pilihan aksen. [light] dipakai pada mode terang (nada gelap, aman untuk teks putih
 * di atasnya), [dark] dipakai pada mode gelap/hitam (nada terang, terbaca di latar gelap).
 */
data class Accent(
    val id: String,
    val label: String,
    val light: Color,
    val dark: Color
)

/**
 * Sumber tunggal daftar aksen. `id` sengaja dibuat stabil (mis. "Teal") agar pilihan
 * yang sudah tersimpan di preferensi tetap valid. [label] dipakai untuk tampilan.
 */
val AccentList: List<Accent> = listOf(
    Accent("Teal", "Teal", Color(0xFF0F766E), Color(0xFF5EEAD4)),
    Accent("Blue", "Biru", Color(0xFF2563EB), Color(0xFF93C5FD)),
    Accent("Indigo", "Indigo", Color(0xFF4F46E5), Color(0xFFA5B4FC)),
    Accent("Purple", "Ungu", Color(0xFF7C3AED), Color(0xFFC4B5FD)),
    Accent("Pink", "Merah Muda", Color(0xFFBE185D), Color(0xFFF9A8D4)),
    Accent("Red", "Merah", Color(0xFFDC2626), Color(0xFFFCA5A5)),
    Accent("Orange", "Oranye", Color(0xFFC2410C), Color(0xFFFDBA74)),
    Accent("Amber", "Kuning", Color(0xFFB45309), Color(0xFFFCD34D)),
    Accent("Emerald", "Hijau", Color(0xFF047857), Color(0xFF6EE7B7)),
    Accent("Cyan", "Cyan", Color(0xFF0E7490), Color(0xFF67E8F9))
)

private val defaultAccent = AccentList.first()

/** Ambil definisi aksen berdasarkan id; jatuh ke aksen default bila id tidak dikenal. */
fun accent(id: String): Accent = AccentList.firstOrNull { it.id == id } ?: defaultAccent

/** Label tampilan aksen (mis. "Biru"). */
fun accentLabel(id: String): String = accent(id).label
