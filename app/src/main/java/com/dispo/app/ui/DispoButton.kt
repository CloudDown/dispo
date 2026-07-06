package com.dispo.app.ui

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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dispo.app.ui.theme.CircusOrange
import com.dispo.app.ui.theme.CircusRed
import com.dispo.app.ui.theme.DispoGreen
import com.dispo.app.ui.theme.DispoGreenDark
import com.dispo.app.ui.theme.SunYellow
import kotlin.math.cos
import kotlin.math.sin

/**
 * Le gros bouton central : cercles concentriques rouge/jaune qui pulsent
 * et tournent (façon iris d'intro Looney Tunes), bouton vert au centre,
 * emoji 😒 quand indispo, 😀 quand dispo.
 */
@Composable
fun DispoButton(
    dispo: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 280.dp,
) {
    val infinite = rememberInfiniteTransition(label = "circles")

    val pulse by infinite.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )

    val rotation by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(24_000, easing = LinearEasing),
        ),
        label = "rotation",
    )

    // Bounce cartoon au changement d'état : le bouton gonfle puis rebondit
    val emojiScale = remember { androidx.compose.animation.core.Animatable(1f) }
    androidx.compose.runtime.LaunchedEffect(dispo) {
        emojiScale.snapTo(1.35f)
        emojiScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = 0.35f, stiffness = 300f),
        )
    }

    val buttonColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (dispo) DispoGreen else DispoGreenDark,
        animationSpec = tween(300),
        label = "buttonColor",
    )

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        // Cercles concentriques animés
        Canvas(
            modifier = Modifier
                .size(size)
                .scale(pulse)
                .rotate(rotation)
        ) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val maxRadius = this.size.minDimension / 2f

            val rings = listOf(
                CircusRed to 1.00f,
                SunYellow to 0.88f,
                CircusRed to 0.76f,
                CircusOrange to 0.64f,
                SunYellow to 0.52f,
            )
            rings.forEach { (color, ratio) ->
                drawCircle(
                    color = color,
                    radius = maxRadius * ratio,
                    center = center,
                    style = Stroke(width = maxRadius * 0.055f),
                )
            }

            // Petits rayons de cirque entre les deux cercles extérieurs
            rotate(degrees = rotation * -2f, pivot = center) {
                val rayCount = 12
                for (i in 0 until rayCount) {
                    val angle = Math.toRadians((i * 360.0 / rayCount))
                    val inner = maxRadius * 0.90f
                    val outer = maxRadius * 0.98f
                    drawLine(
                        color = SunYellow,
                        start = center + Offset(
                            (cos(angle) * inner).toFloat(),
                            (sin(angle) * inner).toFloat(),
                        ),
                        end = center + Offset(
                            (cos(angle) * outer).toFloat(),
                            (sin(angle) * outer).toFloat(),
                        ),
                        strokeWidth = maxRadius * 0.02f,
                    )
                }
            }
        }

        // Bouton vert central
        Box(
            modifier = Modifier
                .size(size * 0.42f)
                .scale(emojiScale.value)
                .shadow(elevation = 10.dp, shape = CircleShape)
                .background(buttonColor, CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onToggle,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (dispo) "😀" else "😒",
                fontSize = 52.sp,
                color = Color.Unspecified,
            )
        }
    }
}
