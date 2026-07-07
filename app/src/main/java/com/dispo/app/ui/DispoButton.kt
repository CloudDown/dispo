package com.dispo.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dispo.app.ui.theme.CircusRed
import com.dispo.app.ui.theme.DispoGreen
import com.dispo.app.ui.theme.DispoGreenDark
import com.dispo.app.ui.theme.InkBrown
import com.dispo.app.ui.theme.LedOff
import com.dispo.app.ui.theme.SunYellow
import kotlin.math.cos
import kotlin.math.sin

/**
 * Le gros bouton central : anneau d'ampoules de fête foraine qui
 * tournent (chenillard), bouton vert cerclé façon cartoon,
 * emoji 😒 quand indispo, 😀 quand dispo.
 * Les grands cercles Looney Tunes sont dessinés plein écran par [HomePanel].
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

    val pulse by infinite.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )

    // Bounce cartoon au changement d'état
    val bounce = remember { Animatable(1f) }
    LaunchedEffect(dispo) {
        bounce.snapTo(1.35f)
        bounce.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = 0.35f, stiffness = 300f),
        )
    }

    val buttonColor by animateColorAsState(
        targetValue = if (dispo) DispoGreen else DispoGreenDark,
        animationSpec = tween(300),
        label = "buttonColor",
    )

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        // Anneau d'ampoules style enseigne de cirque
        Canvas(modifier = Modifier.size(size).scale(pulse)) {
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
                // les deux suivantes restent un peu allumées (traînée)
                val distance = ((i - litIndex) + bulbCount) % bulbCount
                val lit = when (distance) {
                    0 -> 1f
                    1 -> 0.55f
                    2 -> 0.3f
                    else -> 0f
                }
                val bulbColor = androidx.compose.ui.graphics.lerp(LedOff, SunYellow, lit)

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

        // Bouton vert central, gros contour cartoon
        Box(
            modifier = Modifier
                .size(size * 0.52f)
                .scale(bounce.value)
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
            Text(
                text = if (dispo) "😀" else "😒",
                fontSize = 56.sp,
                color = Color.Unspecified,
            )
        }
    }
}
