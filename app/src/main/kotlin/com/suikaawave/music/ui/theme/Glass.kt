/**
 * SuikaaWave Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.suikaawave.music.ui.theme

import androidx.compose.foundation.background
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
