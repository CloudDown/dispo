package com.dispo.app.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dispo.app.core.DispoUiState
import com.dispo.app.ui.theme.CircusOrange
import com.dispo.app.ui.theme.CircusRed
import com.dispo.app.ui.theme.CircusRedDark
import com.dispo.app.ui.theme.Cream
import com.dispo.app.ui.theme.InkBrown
import com.dispo.app.ui.theme.SunYellow
import kotlin.math.hypot

@Composable
fun HomePanel(
    state: DispoUiState,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Layout à hauteurs fixes : le bouton reste centré même quand
    // le ticker LED change de texte au tap.
    Box(modifier = modifier.fillMaxSize().padding(20.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "DISPO",
                style = MaterialTheme.typography.displayLarge.copy(
                    color = Cream,
                    shadow = Shadow(
                        color = InkBrown,
                        offset = Offset(0f, 8f),
                        blurRadius = 2f,
                    ),
                ),
            )
            Spacer(Modifier.height(10.dp))
            Box(modifier = Modifier.fillMaxWidth(0.9f).height(44.dp)) {
                LedTicker(
                    text = if (state.meDispo) {
                        "★ TU ES DISPO JUSQU'À MINUIT ★"
                    } else {
                        "★ TAPE LE BOUTON SI T'ES CHAUD CE SOIR ★"
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        DispoButton(
            dispo = state.meDispo,
            onToggle = onToggle,
            modifier = Modifier.align(Alignment.Center),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(52.dp)
                .align(Alignment.BottomCenter),
            contentAlignment = Alignment.Center,
        ) {
            if (state.dispoCount == 1) {
                LedPanel(
                    text = "ENCORE 1 POUR LE CHAT",
                    fontSize = 16.sp,
                    blinking = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

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

    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxRadius = hypot(size.width, size.height) / 2f
        val ringWidth = size.minDimension * 0.11f
        val pairWidth = ringWidth * 2f

        drawRect(color = CircusRedDark)

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

        drawCircle(color = CircusOrange, radius = size.minDimension * 0.36f, center = center)
        drawCircle(color = SunYellow, radius = size.minDimension * 0.325f, center = center)
        drawCircle(
            color = CircusOrange,
            radius = size.minDimension * 0.30f,
            center = center,
            style = Stroke(width = size.minDimension * 0.012f),
        )
    }
}
