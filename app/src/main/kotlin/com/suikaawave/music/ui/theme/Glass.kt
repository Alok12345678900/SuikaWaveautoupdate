/**
 * SuikaaWave Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.suikaawave.music.ui.theme

import androidx.compose.foundation.background
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Tema kaca (glassmorphism) untuk layar pemutar: latar album blur + tombol putih tembus pandang.
 * Ubah ke false untuk kembali ke tema lama (ikut pengaturan "Latar pemutar").
 */
const val GLASS_PLAYER_THEME = true

/** Isi kaca putih tembus pandang. */
val GlassFill = Color.White.copy(alpha = 0.18f)

/** Permukaan kaca: gradasi putih transparan + garis tepi terang seperti pantulan cahaya. */
fun Modifier.glassSurface(shape: Shape): Modifier =
    this
        .background(
            Brush.verticalGradient(
                listOf(Color.White.copy(alpha = 0.32f), Color.White.copy(alpha = 0.12f)),
            ),
            shape,
        ).border(
            1.dp,
            Brush.linearGradient(
                listOf(
                    Color.White.copy(alpha = 0.70f),
                    Color.White.copy(alpha = 0.12f),
                    Color.White.copy(alpha = 0.40f),
                ),
            ),
            shape,
        )

/** Kaca tipis untuk tombol tidak aktif. */
fun Modifier.glassOutline(shape: Shape): Modifier =
    this
        .background(Color.White.copy(alpha = 0.07f), shape)
        .border(1.dp, Color.White.copy(alpha = 0.35f), shape)

/**
 * Liquid glass untuk SELURUH app: semua layar dan semua pilihan warna tema.
 * Ubah ke false untuk kembali ke tema Material biasa.
 */
const val GLASS_APP_THEME = true

/** Satu-satunya palet app: biru langit, lavender, pink di atas latar gelap kebiruan. */
val GlassBaseScheme: ColorScheme =
    androidx.compose.material3.darkColorScheme(
        primary = Color(0xFF9CC0FF),
        onPrimary = Color(0xFF06214F),
        primaryContainer = Color(0xFF2B4C8C),
        onPrimaryContainer = Color(0xFFDCE8FF),
        secondary = Color(0xFFC9A8FF),
        onSecondary = Color(0xFF2A0F55),
        secondaryContainer = Color(0xFF4A3380),
        onSecondaryContainer = Color(0xFFEBDDFF),
        tertiary = Color(0xFFFFA8CF),
        onTertiary = Color(0xFF560B33),
        tertiaryContainer = Color(0xFF7A2A57),
        onTertiaryContainer = Color(0xFFFFD9EA),
    )

/**
 * Ubah skema warna jadi kaca: latar & permukaan transparan, kartu putih tembus pandang,
 * container aktif (chip, indikator nav) jadi kaca berwarna, menu/dialog kaca gelap pekat, teks putih.
 */
fun ColorScheme.liquidGlass(): ColorScheme {
    val ink = lerp(Color(0xFF0A0F2B), primary, 0.12f)
    val white = Color.White
    return copy(
        background = Color.Transparent,
        onBackground = white.copy(alpha = 0.96f),
        surface = Color.Transparent,
        onSurface = white.copy(alpha = 0.96f),
        surfaceVariant = white.copy(alpha = 0.16f),
        onSurfaceVariant = white.copy(alpha = 0.74f),
        primaryContainer = primary.copy(alpha = 0.30f),
        onPrimaryContainer = white,
        secondaryContainer = white.copy(alpha = 0.22f),
        onSecondaryContainer = white,
        tertiaryContainer = tertiary.copy(alpha = 0.30f),
        onTertiaryContainer = white,
        surfaceDim = Color.Transparent,
        surfaceBright = white.copy(alpha = 0.24f),
        surfaceContainerLowest = Color.Black.copy(alpha = 0.22f),
        surfaceContainerLow = ink.copy(alpha = 0.86f),
        surfaceContainer = ink.copy(alpha = 0.80f),
        surfaceContainerHigh = ink.copy(alpha = 0.90f),
        surfaceContainerHighest = white.copy(alpha = 0.18f),
        outline = white.copy(alpha = 0.42f),
        outlineVariant = white.copy(alpha = 0.22f),
        inverseSurface = white.copy(alpha = 0.92f),
        inverseOnSurface = ink,
        scrim = Color.Black,
    )
}

/** Latar liquid glass: gradasi biru-ungu-magenta + pendar cahaya, tampak di balik semua kaca. */
fun Modifier.liquidBackdrop(@Suppress("UNUSED_PARAMETER") scheme: ColorScheme = GlassBaseScheme): Modifier =
    this.drawBehind {
        drawRect(
            Brush.linearGradient(
                listOf(Color(0xFF17266E), Color(0xFF4B2A8F), Color(0xFF8E2F7F)),
                start = Offset(0f, 0f),
                end = Offset(size.width, size.height),
            ),
        )
        val r = size.maxDimension * 0.75f
        fun glow(
            color: Color,
            cx: Float,
            cy: Float,
            alpha: Float,
        ) {
            val c = Offset(size.width * cx, size.height * cy)
            drawCircle(
                Brush.radialGradient(listOf(color.copy(alpha = alpha), Color.Transparent), center = c, radius = r),
                radius = r,
                center = c,
            )
        }
        glow(Color(0xFF4FC3F7), 0.08f, 0.08f, 0.60f)
        glow(Color(0xFFFF7EB6), 0.98f, 0.52f, 0.50f)
        glow(Color(0xFF7C4DFF), 0.30f, 1.00f, 0.55f)
        glow(Color.White, 0.55f, -0.05f, 0.16f)
    }

/** Di tema kaca, sheet pemutar harus pekat supaya halaman di belakangnya tidak tembus. */
fun Color.opaqueIfGlass(): Color = if (GLASS_APP_THEME) copy(alpha = 1f) else this
