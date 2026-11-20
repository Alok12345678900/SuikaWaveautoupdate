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

/**
 * Ubah skema warna apa pun (Material You / palet tetap / warna sampul) jadi kaca:
 * latar & permukaan transparan, kartu putih tembus pandang, menu/dialog kaca gelap pekat,
 * teks putih. Warna aksen (primary, secondary, tertiary) tetap dari tema yang dipilih.
 */
fun ColorScheme.liquidGlass(): ColorScheme {
    val ink = lerp(Color(0xFF080612), primary, 0.10f)
    val white = Color.White
    return copy(
        background = Color.Transparent,
        onBackground = white.copy(alpha = 0.96f),
        surface = Color.Transparent,
        onSurface = white.copy(alpha = 0.96f),
        surfaceVariant = white.copy(alpha = 0.14f),
        onSurfaceVariant = white.copy(alpha = 0.72f),
        surfaceDim = Color.Transparent,
        surfaceBright = white.copy(alpha = 0.22f),
        surfaceContainerLowest = Color.Black.copy(alpha = 0.25f),
        surfaceContainerLow = ink.copy(alpha = 0.86f),
        surfaceContainer = ink.copy(alpha = 0.80f),
        surfaceContainerHigh = ink.copy(alpha = 0.90f),
        surfaceContainerHighest = white.copy(alpha = 0.16f),
        outline = white.copy(alpha = 0.40f),
        outlineVariant = white.copy(alpha = 0.20f),
        inverseSurface = white.copy(alpha = 0.92f),
        inverseOnSurface = ink,
        scrim = Color.Black,
    )
}

/** Latar belakang liquid glass: gradasi gelap + pendar warna aksen yang tampak di balik semua kaca. */
fun Modifier.liquidBackdrop(scheme: ColorScheme): Modifier =
    this.drawBehind {
        val ink = lerp(Color(0xFF080612), scheme.primary, 0.10f)
        drawRect(
            Brush.verticalGradient(
                listOf(lerp(ink, scheme.primary, 0.22f), ink, lerp(ink, scheme.tertiary, 0.18f)),
            ),
        )
        val r = size.maxDimension * 0.7f
        drawCircle(
            Brush.radialGradient(
                listOf(scheme.primary.copy(alpha = 0.50f), Color.Transparent),
                center = Offset(size.width * 0.10f, size.height * 0.10f),
                radius = r,
            ),
            radius = r,
            center = Offset(size.width * 0.10f, size.height * 0.10f),
        )
        drawCircle(
            Brush.radialGradient(
                listOf(scheme.tertiary.copy(alpha = 0.40f), Color.Transparent),
                center = Offset(size.width * 0.95f, size.height * 0.55f),
                radius = r,
            ),
            radius = r,
            center = Offset(size.width * 0.95f, size.height * 0.55f),
        )
        drawCircle(
            Brush.radialGradient(
                listOf(scheme.secondary.copy(alpha = 0.35f), Color.Transparent),
                center = Offset(size.width * 0.25f, size.height * 0.95f),
                radius = r,
            ),
            radius = r,
            center = Offset(size.width * 0.25f, size.height * 0.95f),
        )
    }

/** Di tema kaca, sheet pemutar harus pekat supaya halaman di belakangnya tidak tembus. */
fun Color.opaqueIfGlass(): Color = if (GLASS_APP_THEME) copy(alpha = 1f) else this
