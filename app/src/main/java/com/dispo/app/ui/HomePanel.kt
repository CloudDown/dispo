package com.dispo.app.ui

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dispo.app.core.DispoUiState
import com.dispo.app.ui.theme.Cream
import com.dispo.app.ui.theme.InkBrown

/** Titre + ticker + hint. Le bouton vert est superposé au centre écran par DispoApp. */
@Composable
fun HomePanel(
    state: DispoUiState,
    modifier: Modifier = Modifier,
) {
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
