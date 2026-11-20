/**
 * SuikaaWave Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.suikaawave.music.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import kotlinx.coroutines.launch

/**
 * Efek tekan ala kaca: elemen mengecil halus saat disentuh (langsung, tanpa jeda) lalu memantul
 * balik dengan spring saat dilepas. Makin besar elemennya, makin kecil perubahan skalanya, jadi
 * kartu/baris lebar tidak terlihat "melompat".
 */
object GlassIndication : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode = GlassIndicationNode(interactionSource)

    override fun equals(other: Any?): Boolean = other === GlassIndication

    override fun hashCode(): Int = -1
}

private class GlassIndicationNode(
    private val interactionSource: InteractionSource,
) : Modifier.Node(),
    DrawModifierNode {
    private val progress = Animatable(0f)

    override fun onAttach() {
        coroutineScope.launch {
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is PressInteraction.Press ->
                        launch {
                            progress.animateTo(
                                1f,
                                spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessHigh),
                            )
                        }

                    is PressInteraction.Release, is PressInteraction.Cancel ->
                        launch {
                            progress.animateTo(
                                0f,
                                spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMedium),
                            )
                        }
                }
            }
        }
    }

    override fun ContentDrawScope.draw() {
        val p = progress.value
        if (p == 0f) {
            drawContent()
            return
        }
        val minDp = size.minDimension / density
        val maxShrink =
            when {
                minDp <= 64f -> 0.10f
                minDp <= 140f -> 0.06f
                minDp <= 300f -> 0.035f
                else -> 0.012f
            }
        val s = 1f - p * maxShrink
        withTransform({ scale(s, s, center) }) {
            this@draw.drawContent()
        }
    }
}
