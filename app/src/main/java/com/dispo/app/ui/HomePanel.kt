package com.dispo.app.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dispo.app.core.DispoUiState
import com.dispo.app.ui.theme.CircusOrange
import com.dispo.app.ui.theme.CircusRed
import com.dispo.app.ui.theme.CircusRedDark
import com.dispo.app.ui.theme.Cream
import com.dispo.app.ui.theme.DispoGreen
import com.dispo.app.ui.theme.InkBrown
import com.dispo.app.ui.theme.SunYellow
import kotlin.math.hypot

/**
 * Contenu de la page d'accueil. Le fond tornade Looney Tunes est dessiné
 * plein écran par [LooneyRings] au niveau de l'activité (derrière les
 * barres système et les onglets).
 */
@Composable
fun HomePanel(
    state: DispoUiState,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        // Titre façon carton d'intro
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
            LedTicker(
                text = if (state.meDispo) {
                    "★ TU ES DISPO JUSQU'À MINUIT ★ TES POTES SONT PRÉVENUS ★"
                } else {
                    "★ TAPE LE BOUTON SI T'ES CHAUD CE SOIR ★"
                },
                modifier = Modifier.fillMaxWidth(0.9f),
            )
        }

        DispoButton(dispo = state.meDispo, onToggle = onToggle)

        // Tableau des dispos sur carton crème
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(Cream, RoundedCornerShape(18.dp))
                .border(3.dp, InkBrown, RoundedCornerShape(18.dp))
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "QUI EST CHAUD ?",
                style = MaterialTheme.typography.titleLarge,
                color = CircusRed,
            )
            Spacer(Modifier.height(8.dp))

            val dispoFriends = state.friends.filter { it.dispo }
            if (dispoFriends.isEmpty() && !state.meDispo) {
                Text(
                    "Personne pour l'instant…",
                    color = InkBrown,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                )
            } else {
                if (state.meDispo) FriendRow(name = "Moi")
                dispoFriends.forEach { FriendRow(name = it.name) }
            }

            if (state.dispoCount == 1) {
                Spacer(Modifier.height(10.dp))
                LedPanel(
                    text = "ENCORE 1 POUR LE CHAT",
                    fontSize = 16.sp,
                    blinking = true,
                )
            }
        }
    }
}

/**
 * Fond plein écran façon intro Looney Tunes : anneaux concentriques
 * rouge/bordeaux qui se dilatent lentement depuis le centre,
 * avec un cœur jaune/orangé derrière le bouton.
 */
@Composable
fun LooneyRings(modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition(label = "rings")
    val phase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
        ),
        label = "ringPhase",
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxRadius = hypot(size.width, size.height) / 2f
        val ringWidth = size.minDimension * 0.11f
        val pairWidth = ringWidth * 2f

        drawRect(color = CircusRedDark)

        // Anneaux qui se dilatent : le motif se répète toutes les 2 bandes,
        // le décalage de phase crée l'effet de zoom hypnotique.
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

        // Cœur jaune/orangé derrière le bouton
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

@Composable
private fun FriendRow(name: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp),
    ) {
        Box(Modifier.size(10.dp).background(DispoGreen, CircleShape))
        Spacer(Modifier.width(8.dp))
        Text("$name est dispo 😀", fontSize = 16.sp, color = InkBrown)
    }
}
