package com.dispo.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import com.dispo.app.ui.theme.Cream
import com.dispo.app.ui.theme.DispoGreen
import com.dispo.app.ui.theme.DispoGreenDark
import com.dispo.app.ui.theme.InkBrown
import com.dispo.app.ui.theme.SunYellow
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlinx.coroutines.launch

private val FaceDark = Color(0xFF0B4A2A)
private val FaceHighlight = Color(0xFF7FE8AC)

private const val STAR_COUNT = 10

/** Durée du burst d’étoiles (ms) — le swipe vers le chat attend la fin. */
const val STAR_BURST_MS = 800

/**
 * Bouton vert calé sur le cœur jaune de la tornade Looney Tunes.
 * Face droite ; la profondeur part en biais (oblique).
 * Burst d’étoiles au clic qui active dispo.
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

    val burst = remember { Animatable(0f) }
    var burstVisible by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun playBurst() {
        scope.launch {
            try {
                burstVisible = true
                burst.snapTo(0f)
                burst.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(STAR_BURST_MS, easing = LinearOutSlowInEasing),
                )
            } finally {
                burstVisible = false
                burst.snapTo(0f)
            }
        }
    }

    val burstProgress = burst.value
    // Zone élargie pour que les étoiles ne soient pas clipées
    val canvasSize = size * 1.55f

    Box(
        modifier = modifier.size(canvasSize),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        val activating = !dispo
                        onToggle()
                        if (activating) playBurst()
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.size(size)) {
                drawDispoPuck(buttonColor, raised = face, faceProgress = face)
            }
        }
        // progress > 0.02 : évite une étoile empilée au centre au premier frame
        if (burstVisible && burstProgress > 0.02f) {
            val puckDp = size
            Canvas(modifier = Modifier.size(canvasSize)) {
                val puckPx = puckDp.toPx()
                val origin = Offset(this.size.width / 2f, this.size.height / 2f)
                // Même géométrie que le puck : face légèrement décalée
                val rTop = puckPx * 0.42f
                val depth = rTop * (0.16f + 0.05f * face)
                val dirLen = hypot(0.55f, 0.84f)
                val dx = depth * 0.55f / dirLen
                val dy = depth * 0.84f / dirLen
                val faceCenter = Offset(origin.x - dx, origin.y - dy)
                drawStarBurst(
                    progress = burstProgress,
                    origin = faceCenter,
                    maxRadius = puckPx * 0.72f,
                )
            }
        }
    }
}

private fun DrawScope.drawStarBurst(progress: Float, origin: Offset, maxRadius: Float) {
    val t = progress.coerceIn(0f, 1f)
    // Fade après la moitié du parcours
    val alpha = when {
        t < 0.55f -> 1f
        else -> (1f - (t - 0.55f) / 0.45f).coerceIn(0f, 1f)
    }
    if (alpha <= 0f) return

    val easeOut = 1f - (1f - t) * (1f - t)
    val jump = sin(t * PI.toFloat()) * maxRadius * 0.28f
    val baseSize = maxRadius * 0.11f

    for (i in 0 until STAR_COUNT) {
        val angle = (-PI.toFloat() / 2f) + i * (2f * PI.toFloat() / STAR_COUNT) +
            (i % 3 - 1) * 0.12f
        val distScale = 0.75f + (i % 4) * 0.08f
        val dist = easeOut * maxRadius * distScale
        val pos = Offset(
            x = origin.x + cos(angle) * dist,
            y = origin.y + sin(angle) * dist - jump,
        )
        val starSize = baseSize * (1f - 0.35f * t) * (0.85f + (i % 3) * 0.1f)
        val color = if (i % 2 == 0) {
            SunYellow.copy(alpha = alpha)
        } else {
            Cream.copy(alpha = alpha * 0.95f)
        }
        drawPath(
            path = starPath(pos, outer = starSize, inner = starSize * 0.42f),
            color = color,
        )
    }
}

private fun starPath(center: Offset, outer: Float, inner: Float): Path {
    val path = Path()
    val points = 5
    for (i in 0 until points * 2) {
        val radius = if (i % 2 == 0) outer else inner
        val angle = -PI / 2.0 + i * PI / points
        val x = center.x + cos(angle).toFloat() * radius
        val y = center.y + sin(angle).toFloat() * radius
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

private fun DrawScope.drawDispoPuck(base: Color, raised: Float, faceProgress: Float) {
    val d = size.minDimension
    // Face du dessus
    val rTop = d * 0.42f
    // Tranche / base vert foncé un peu plus large que la face
    val rBase = rTop * 1.08f

    // Profondeur moyenne, direction oblique (bas-droite)
    val depth = rTop * (0.16f + 0.05f * raised)
    val dirX = 0.55f
    val dirY = 0.84f
    val len = hypot(dirX, dirY)
    val dx = depth * dirX / len
    val dy = depth * dirY / len

    // Base vert foncé centrée (cœur jaune) ; la face part en biais opposé
    val bottom = Offset(size.width / 2f, size.height / 2f)
    val top = Offset(bottom.x - dx, bottom.y - dy)

    val topLight = base.lighten(0.26f)
    val topFlat = base.lighten(0.05f)
    val topShade = base.darken(0.06f)
    val wallHi = base.darken(0.18f)
    val wallMid = base.darken(0.34f)
    val wallDeep = base.darken(0.50f)

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.Black.copy(alpha = 0.24f),
                Color.Black.copy(alpha = 0.08f),
                Color.Transparent,
            ),
            center = Offset(bottom.x + dx * 0.3f, bottom.y + dy * 0.3f),
            radius = rBase * 1.2f,
        ),
        radius = rBase * 1.12f,
        center = Offset(bottom.x + dx * 0.25f, bottom.y + dy * 0.25f),
    )

    val angle = atan2(dy, dx)
    val nx = -sin(angle)
    val ny = cos(angle)

    val leftTop = Offset(top.x + nx * rTop, top.y + ny * rTop)
    val leftBottom = Offset(bottom.x + nx * rBase, bottom.y + ny * rBase)
    val rightTop = Offset(top.x - nx * rTop, top.y - ny * rTop)
    val rightBottom = Offset(bottom.x - nx * rBase, bottom.y - ny * rBase)

    val wallPath = Path().apply {
        moveTo(leftTop.x, leftTop.y)
        lineTo(leftBottom.x, leftBottom.y)
        lineTo(rightBottom.x, rightBottom.y)
        lineTo(rightTop.x, rightTop.y)
        close()
    }
    drawPath(
        path = wallPath,
        brush = Brush.linearGradient(
            colors = listOf(wallDeep, wallHi, wallMid),
            start = leftTop,
            end = rightBottom,
        ),
    )
    drawCircle(
        color = wallDeep,
        radius = rBase,
        center = bottom,
    )

    drawCircle(
        brush = Brush.verticalGradient(
            colors = listOf(topLight, topFlat, topShade),
            startY = top.y - rTop,
            endY = top.y + rTop,
        ),
        radius = rTop,
        center = top,
    )

    drawArc(
        color = Color.White.copy(alpha = 0.40f),
        startAngle = 205f,
        sweepAngle = 120f,
        useCenter = false,
        topLeft = Offset(top.x - rTop * 0.90f, top.y - rTop * 0.90f),
        size = androidx.compose.ui.geometry.Size(rTop * 1.80f, rTop * 1.80f),
        style = Stroke(width = rTop * 0.035f, cap = StrokeCap.Round),
    )

    drawCircle(
        color = InkBrown,
        radius = rTop,
        center = top,
        style = Stroke(width = (d * 0.012f).coerceIn(2f, 4f)),
    )

    // Visage droit, centré sur la face du dessus
    val faceSize = rTop * 2f * 0.72f
    drawFaceAt(top, faceSize, faceProgress, FaceHighlight, offsetPx = faceSize * 0.014f)
    drawFaceAt(top, faceSize, faceProgress, FaceDark, offsetPx = 0f)
}

private fun DrawScope.drawFaceAt(
    center: Offset,
    faceSize: Float,
    progress: Float,
    color: Color,
    offsetPx: Float,
) {
    val s = faceSize
    val stroke = s * 0.045f
    val origin = Offset(center.x - s / 2f, center.y - s / 2f)
    val shift = Offset(offsetPx, offsetPx)

    val eyeY = s * 0.40f
    val eyeWidth = s * (0.15f - 0.04f * progress)
    val eyeHeight = s * (0.035f + 0.09f * progress)
    listOf(s * 0.34f, s * 0.66f).forEach { eyeX ->
        drawOval(
            color = color,
            topLeft = origin + Offset(eyeX - eyeWidth / 2f, eyeY - eyeHeight / 2f) + shift,
            size = androidx.compose.ui.geometry.Size(eyeWidth, eyeHeight),
        )
    }

    val mouthStartY = s * (0.63f + 0.01f * progress)
    val mouthEndY = s * (0.60f + 0.04f * progress)
    val controlY = s * (0.58f + 0.24f * progress)
    val path = Path().apply {
        moveTo(origin.x + s * 0.32f + shift.x, origin.y + mouthStartY + shift.y)
        quadraticTo(
            origin.x + s * 0.50f + shift.x, origin.y + controlY + shift.y,
            origin.x + s * 0.68f + shift.x, origin.y + mouthEndY + shift.y,
        )
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = stroke, cap = StrokeCap.Round),
    )
}

private fun Color.lighten(amount: Float): Color {
    val t = amount.coerceIn(0f, 1f)
    return Color(
        red = red + (1f - red) * t,
        green = green + (1f - green) * t,
        blue = blue + (1f - blue) * t,
        alpha = alpha,
    )
}

private fun Color.darken(amount: Float): Color {
    val t = (1f - amount.coerceIn(0f, 1f))
    return Color(
        red = red * t,
        green = green * t,
        blue = blue * t,
        alpha = alpha,
    )
}
