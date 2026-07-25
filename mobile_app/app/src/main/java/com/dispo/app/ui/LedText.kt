package com.dispo.app.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import com.dispo.app.ui.theme.InkBrown
import com.dispo.app.ui.theme.LedAmber
import com.dispo.app.ui.theme.LedFamily

/** Texte secondaire style barre LED (sans panneau sombre). */
@Composable
fun LedCaption(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 20.sp,
    color: Color = InkBrown.copy(alpha = 0.65f),
    textAlign: TextAlign = TextAlign.Start,
) {
    Text(
        text = text,
        modifier = modifier,
        fontFamily = LedFamily,
        fontSize = fontSize,
        color = color,
        letterSpacing = 1.sp,
        textAlign = textAlign,
    )
}

/**
 * Panneau "barre LED" : fond sombre, texte dot-matrix ambre avec halo,
 * comme les enseignes lumineuses de fête foraine.
 */
@Composable
fun LedPanel(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 20.sp,
    color: Color = LedAmber,
    blinking: Boolean = false,
) {
    val alpha = if (blinking) {
        val transition = rememberInfiniteTransition(label = "ledBlink")
        val value by transition.animateFloat(
            initialValue = 1f,
            targetValue = 0.25f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "blinkAlpha",
        )
        value
    } else 1f

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF1A120B))
            .border(2.dp, InkBrown, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontFamily = LedFamily,
                fontSize = fontSize,
                color = color,
                letterSpacing = 3.sp,
                textAlign = TextAlign.Center,
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = color.copy(alpha = 0.85f),
                    blurRadius = 14f,
                ),
            ),
            modifier = Modifier.graphicsLayer { this.alpha = alpha },
        )
    }
}

/**
 * Bandeau LED défilant (ticker) : le texte glisse en boucle
 * de droite à gauche comme sur un afficheur de guichet.
 */
@Composable
fun LedTicker(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 20.sp,
    color: Color = LedAmber,
    durationMillis: Int = 7000,
) {
    var containerWidth by remember { mutableIntStateOf(0) }
    var textWidth by remember { mutableIntStateOf(0) }

    val transition = rememberInfiniteTransition(label = "ledTicker")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
        ),
        label = "tickerProgress",
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF1A120B))
            .border(2.dp, InkBrown, RoundedCornerShape(10.dp))
            .padding(vertical = 6.dp)
            .onGloballyPositioned { containerWidth = it.size.width },
        contentAlignment = Alignment.CenterStart,
    ) {
        val travel = (containerWidth + textWidth).toFloat()
        Text(
            text = text,
            maxLines = 1,
            softWrap = false,
            style = TextStyle(
                fontFamily = LedFamily,
                fontSize = fontSize,
                color = color,
                letterSpacing = 3.sp,
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = color.copy(alpha = 0.85f),
                    blurRadius = 14f,
                ),
            ),
            modifier = Modifier
                .onGloballyPositioned { textWidth = it.size.width }
                .graphicsLayer {
                    translationX = containerWidth - progress * travel
                },
        )
    }
}
