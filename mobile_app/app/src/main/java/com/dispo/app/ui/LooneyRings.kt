package com.dispo.app.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import com.dispo.app.ui.theme.CircusOrange
import com.dispo.app.ui.theme.CircusRed
import com.dispo.app.ui.theme.CircusRedDark
import com.dispo.app.ui.theme.Cream
import com.dispo.app.ui.theme.SunYellow
import kotlin.math.hypot

/** Fond tornade Looney Tunes plein écran (anneaux rouge/bordeaux animés). */
@Composable
fun LooneyRings(modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition(label = "rings")
    val phase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(6000, easing = LinearEasing)),
        label = "ringPhase",
    )

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxRadius = hypot(size.width, size.height) / 2f
        val ringWidth = size.minDimension * 0.11f
        val pairWidth = ringWidth * 2f
        val coreOuter = size.minDimension * LOONEY_CORE_OUTER_FRACTION
        val coreYellow = size.minDimension * LOONEY_CORE_YELLOW_FRACTION

        drawRect(color = Cream)

        val offset = phase * pairWidth
        var radius = maxRadius + pairWidth
        var dark = false
        while (radius - offset > 0f) {
            drawCircle(
                color = if (dark) CircusRedDark else CircusRed,
                radius = radius - offset,
                center = center,
            )
            dark = !dark
            radius -= ringWidth
        }

        // Cœur jaune/orange : le bouton vert vient se poser exactement dessus
        drawCircle(color = CircusOrange, radius = coreOuter, center = center)
        drawCircle(color = SunYellow, radius = coreYellow, center = center)
    }
}
