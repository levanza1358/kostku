package com.miraya.kostku.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

/** True bila mode tema aplikasi menghasilkan skema gelap. */
fun isDarkTheme(mode: String): Boolean = mode == "dark" || mode == "black"

/** Bentuk sudut seragam untuk seluruh aplikasi. */
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

private val LightBackground = Color(0xFFF7F8FB)
private val DarkBackground = Color(0xFF121316)
private val BlackBackground = Color.Black

/** Putih atau hitam, mana pun yang lebih kontras di atas [background]. */
fun readableOn(background: Color): Color =
    if (background.luminance() > 0.5f) Color.Black else Color.White

/** Campur [color] dengan [other] sebanyak [fraction] (0 = color, 1 = other). */
private fun blend(color: Color, other: Color, fraction: Float): Color = Color(
    red = color.red + (other.red - color.red) * fraction,
    green = color.green + (other.green - color.green) * fraction,
    blue = color.blue + (other.blue - color.blue) * fraction
)

/**
 * Bangun skema warna Material 3 untuk [mode] ("light"/"dark"/"black") dan [accent] (id aksen).
 *
 * Aksen diturunkan per-mode: pada mode terang aksen bernada lebih gelap dengan `onPrimary`
 * putih, pada mode gelap/hitam aksen bernada lebih terang dengan `onPrimary` hitam — sehingga
 * teks aksen tidak pernah gelap-di-atas-gelap. Nilai container/tint lainnya diturunkan dari
 * aksen yang sama agar seluruh peran M3 konsisten.
 */
fun kostkuColorScheme(mode: String, accent: String): ColorScheme {
    val dark = isDarkTheme(mode)
    val black = mode == "black"
    val a = accent(accent)
    val base = if (dark) a.dark else a.light
    return if (dark) {
        val background = if (black) BlackBackground else DarkBackground
        val on = readableOn(base)
        val container = blend(background, base, 0.32f)
        darkColorScheme(
            primary = base,
            onPrimary = on,
            primaryContainer = container,
            onPrimaryContainer = blend(base, Color.White, 0.55f),
            secondary = blend(base, Color.White, 0.45f),
            onSecondary = on,
            secondaryContainer = container,
            onSecondaryContainer = blend(base, Color.White, 0.50f),
            tertiary = blend(base, Color.White, 0.25f),
            background = background,
            surface = background,
            surfaceContainerLowest = if (black) Color(0xFF0A0A0A) else Color(0xFF0E0F12),
            surfaceContainer = if (black) Color(0xFF141414) else Color(0xFF1B1D21),
            surfaceContainerHigh = if (black) Color(0xFF1B1B1B) else Color(0xFF232529),
            surfaceVariant = if (black) Color(0xFF1E1E1E) else Color(0xFF272A2F),
            outlineVariant = if (black) Color(0xFF3A3A3A) else Color(0xFF3A3E45)
        )
    } else {
        val background = LightBackground
        val container = blend(base, Color.White, 0.84f)
        lightColorScheme(
            primary = base,
            onPrimary = readableOn(base),
            primaryContainer = container,
            onPrimaryContainer = blend(base, Color.Black, 0.35f),
            secondary = base,
            onSecondary = readableOn(base),
            secondaryContainer = container,
            onSecondaryContainer = blend(base, Color.Black, 0.35f),
            tertiary = blend(base, Color.Black, 0.20f),
            background = background,
            surface = Color.White,
            surfaceContainerLowest = Color.White,
            surfaceContainer = Color(0xFFF1F3F8),
            surfaceContainerHigh = Color(0xFFE9EDF4),
            surfaceVariant = Color(0xFFE7EAF1),
            outlineVariant = Color(0xFFCBD1DB)
        )
    }
}

@Composable
fun KostkuTheme(mode: String = "light", accent: String = "Teal", content: @Composable () -> Unit) {
    val scheme = kostkuColorScheme(mode, accent)
    val dark = isDarkTheme(mode)

    // Selaraskan ikon status bar & navigation bar dengan tema aplikasi yang dipilih.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? android.app.Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
            window.statusBarColor = scheme.background.toArgb()
            window.navigationBarColor = scheme.background.toArgb()
        }
    }

    MaterialTheme(colorScheme = scheme, typography = Typography(), shapes = AppShapes, content = content)
}
