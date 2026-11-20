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
val GlassFill = Color.White.copy(alpha = 0.12f)

/** Permukaan kaca: gradasi putih transparan + garis tepi terang seperti pantulan cahaya. */
fun Modifier.glassSurface(shape: Shape): Modifier =
    this
        .background(
            Brush.verticalGradient(
                listOf(Color.White.copy(alpha = 0.20f), Color.White.copy(alpha = 0.08f)),
            ),
            shape,
        ).border(
            1.dp,
            Brush.linearGradient(
                listOf(
                    Color.White.copy(alpha = 0.48f),
                    Color.White.copy(alpha = 0.10f),
                    Color.White.copy(alpha = 0.28f),
                ),
            ),
            shape,
        )

/** Kaca tipis untuk tombol tidak aktif. */
fun Modifier.glassOutline(shape: Shape): Modifier =
    this
        .background(Color.White.copy(alpha = 0.05f), shape)
        .border(1.dp, Color.White.copy(alpha = 0.24f), shape)

/**
 * Liquid glass untuk SELURUH app: semua layar dan semua pilihan warna tema.
 * Ubah ke false untuk kembali ke tema Material biasa.
 */
const val GLASS_APP_THEME = true

/** Satu-satunya palet app: biru langit, lavender, pink di atas latar gelap kebiruan. */
val GlassBaseScheme: ColorScheme =
    androidx.compose.material3.darkColorScheme(
        primary = Color(0xFFE2C8D7),
        onPrimary = Color(0xFF30212A),
        primaryContainer = Color(0xFF765B6B),
        onPrimaryContainer = Color(0xFFF6E8F0),
        secondary = Color(0xFFC8B9D7),
        onSecondary = Color(0xFF2D2438),
        secondaryContainer = Color(0xFF66566F),
        onSecondaryContainer = Color(0xFFEDE4F6),
        tertiary = Color(0xFFD8B9AC),
        onTertiary = Color(0xFF39251F),
        tertiaryContainer = Color(0xFF76574A),
        onTertiaryContainer = Color(0xFFF2E0D8),
    )

/**
 * Ubah skema warna jadi kaca: latar & permukaan transparan, kartu putih tembus pandang,
 * container aktif (chip, indikator nav) jadi kaca berwarna, menu/dialog kaca gelap pekat, teks putih.
 */
fun ColorScheme.liquidGlass(): ColorScheme {
    val ink = lerp(Color(0xFF211D24), primary, 0.10f)
    val white = Color.White
    return copy(
        background = Color.Transparent,
        onBackground = white.copy(alpha = 0.96f),
        surface = Color.Transparent,
        onSurface = white.copy(alpha = 0.96f),
        surfaceVariant = white.copy(alpha = 0.10f),
        onSurfaceVariant = white.copy(alpha = 0.74f),
        primaryContainer = primary.copy(alpha = 0.20f),
        onPrimaryContainer = white,
        secondaryContainer = white.copy(alpha = 0.14f),
        onSecondaryContainer = white,
        tertiaryContainer = tertiary.copy(alpha = 0.18f),
        onTertiaryContainer = white,
        surfaceDim = Color.Transparent,
        surfaceBright = white.copy(alpha = 0.16f),
        surfaceContainerLowest = Color.Black.copy(alpha = 0.12f),
        surfaceContainerLow = ink.copy(alpha = 0.24f),
        surfaceContainer = ink.copy(alpha = 0.30f),
        surfaceContainerHigh = ink.copy(alpha = 0.38f),
        surfaceContainerHighest = white.copy(alpha = 0.12f),
        outline = white.copy(alpha = 0.32f),
        outlineVariant = white.copy(alpha = 0.16f),
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
                listOf(Color(0xFF302B36), Color(0xFF55414F), Color(0xFF8A625D)),
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
        glow(Color(0xFFD7B6D0), 0.08f, 0.08f, 0.34f)
        glow(Color(0xFFD89A98), 0.98f, 0.52f, 0.32f)
        glow(Color(0xFF9E88B7), 0.30f, 1.00f, 0.30f)
        glow(Color.White, 0.55f, -0.05f, 0.12f)
    }

/** Di tema kaca, sheet pemutar harus pekat supaya halaman di belakangnya tidak tembus. */
fun Color.opaqueIfGlass(): Color = this
