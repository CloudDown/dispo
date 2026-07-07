package com.dispo.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dispo.app.ui.theme.CircusRed
import com.dispo.app.ui.theme.DispoGreen
import com.dispo.app.ui.theme.DispoGreenDark
import com.dispo.app.ui.theme.InkBrown
import com.dispo.app.ui.theme.LedOff
import com.dispo.app.ui.theme.SunYellow
import kotlin.math.cos
import kotlin.math.sin

private val FaceDark = Color(0xFF0B4A2A)
private val FaceHighlight = Color(0xFF7FE8AC)

/**
 * Le gros bouton central : anneau d'ampoules de fête foraine (chenillard),
 * bouton vert cerclé façon cartoon avec un visage gravé dans le vert
 * (pas d'emoji système) : blasé quand indispo, grand sourire quand dispo.
 * Aucun élément ne bouge au clic — seuls la couleur et le visage changent.
 */
@Composable
fun DispoButton(
    dispo: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 240.dp,
) {
    val infinite = rememberInfiniteTransition(label = "bulbs")

    // Chenillard : position de l'ampoule allumée (plus rapide quand dispo)
    val chase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (dispo) 900 else 2600, easing = LinearEasing),
        ),
        label = "chase",
    )

    val buttonColor by animateColorAsState(
        targetValue = if (dispo) DispoGreen else DispoGreenDark,
        animationSpec = tween(300),
        label = "buttonColor",
    )

    // Transition du visage : 0 = blasé, 1 = grand sourire
    val face by animateFloatAsState(
        targetValue = if (dispo) 1f else 0f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "face",
    )

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        // Anneau d'ampoules style enseigne de cirque (position fixe)
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val ringRadius = this.size.minDimension / 2f * 0.86f
            val bulbRadius = this.size.minDimension * 0.030f
            val bulbCount = 16

            // Piste sombre derrière les ampoules
            drawCircle(
                color = InkBrown,
                radius = ringRadius,
                center = center,
                style = Stroke(width = bulbRadius * 3.4f),
            )
            drawCircle(
                color = CircusRed,
                radius = ringRadius,
                center = center,
                style = Stroke(width = bulbRadius * 2.6f),
            )

            val litIndex = (chase * bulbCount).toInt() % bulbCount
            for (i in 0 until bulbCount) {
                val angle = Math.toRadians(i * 360.0 / bulbCount - 90.0)
                val pos = center + Offset(
                    (cos(angle) * ringRadius).toFloat(),
                    (sin(angle) * ringRadius).toFloat(),
                )
                // Distance au chenillard : l'ampoule courante brille,
                // les deux suivantes gardent une traînée
                val distance = ((i - litIndex) + bulbCount) % bulbCount
                val lit = when (distance) {
                    0 -> 1f
                    1 -> 0.55f
                    2 -> 0.3f
                    else -> 0f
                }
                val bulbColor = lerp(LedOff, SunYellow, lit)

                if (lit > 0.5f) {
                    drawCircle(
                        color = SunYellow.copy(alpha = 0.35f * lit),
                        radius = bulbRadius * 2.4f,
                        center = pos,
                    )
                }
                drawCircle(color = bulbColor, radius = bulbRadius, center = pos)
                drawCircle(
                    color = InkBrown,
                    radius = bulbRadius,
                    center = pos,
                    style = Stroke(width = bulbRadius * 0.28f),
                )
            }
        }

        // Bouton vert central, immobile, visage gravé dedans
        Box(
            modifier = Modifier
                .size(size * 0.52f)
                .shadow(elevation = 12.dp, shape = CircleShape)
                .background(buttonColor, CircleShape)
                .border(5.dp, InkBrown, CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onToggle,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.size(size * 0.52f)) {
                // Effet gravé : trait clair légèrement décalé sous le trait sombre
                drawFace(face, FaceHighlight, offsetPx = this.size.minDimension * 0.014f)
                drawFace(face, FaceDark, offsetPx = 0f)
            }
        }
    }
}

/**
 * Dessine le visage gravé. [progress] interpole entre le visage blasé (0)
 * et le grand sourire (1) : les yeux passent de fentes à ronds, la bouche
 * d'un trait plat tombant à un large sourire.
 */
private fun DrawScope.drawFace(progress: Float, color: Color, offsetPx: Float) {
    val s = size.minDimension
    val stroke = s * 0.045f
    val shift = Offset(offsetPx, offsetPx)

    // Yeux : ovales dont la hauteur grandit avec le sourire
    val eyeY = s * 0.40f
    val eyeWidth = s * (0.15f - 0.04f * progress)
    val eyeHeight = s * (0.035f + 0.09f * progress)
    listOf(s * 0.34f, s * 0.66f).forEach { eyeX ->
        drawOval(
            color = color,
            topLeft = Offset(eyeX - eyeWidth / 2f, eyeY - eyeHeight / 2f) + shift,
            size = androidx.compose.ui.geometry.Size(eyeWidth, eyeHeight),
        )
    }

    // Bouche : courbe de Bézier, du trait blasé au grand sourire
    val mouthStartY = s * (0.63f + 0.01f * progress)
    val mouthEndY = s * (0.60f + 0.04f * progress)
    val controlY = s * (0.58f + 0.24f * progress)
    val path = Path().apply {
        moveTo(s * 0.32f + shift.x, mouthStartY + shift.y)
        quadraticTo(
            s * 0.50f + shift.x, controlY + shift.y,
            s * 0.68f + shift.x, mouthEndY + shift.y,
        )
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = stroke, cap = StrokeCap.Round),
    )
}
