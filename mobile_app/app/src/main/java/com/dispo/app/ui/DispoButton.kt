package com.dispo.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dispo.app.ui.theme.DispoGreen
import com.dispo.app.ui.theme.DispoGreenDark
import com.dispo.app.ui.theme.InkBrown

private val FaceDark = Color(0xFF0B4A2A)
private val FaceHighlight = Color(0xFF7FE8AC)

/**
 * Bouton vert calé sur le cœur jaune de la tornade Looney Tunes.
 * Taille imposée par le parent (≈ 52 % de la plus petite dimension écran).
 * Pas d'ombre : il doit sembler gravé dans l'animation derrière.
 */
@Composable
fun DispoButton(
    dispo: Boolean,
    onToggle: () -> Unit,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    val buttonColor by animateColorAsState(
        targetValue = if (dispo) DispoGreen else DispoGreenDark,
        animationSpec = tween(300),
        label = "buttonColor",
    )

    val face by animateFloatAsState(
        targetValue = if (dispo) 1f else 0f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "face",
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .background(buttonColor, CircleShape)
                .border((size.value * 0.012f).coerceIn(2f, 4f).dp, InkBrown, CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onToggle,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.size(size * 0.72f)) {
                drawFace(face, FaceHighlight, offsetPx = this.size.minDimension * 0.014f)
                drawFace(face, FaceDark, offsetPx = 0f)
            }
        }
    }
}

private fun DrawScope.drawFace(progress: Float, color: Color, offsetPx: Float) {
    val s = size.minDimension
    val stroke = s * 0.045f
    val shift = Offset(offsetPx, offsetPx)

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
