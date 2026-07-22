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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dispo.app.core.DispoUiState
import com.dispo.app.ui.theme.Cream
import com.dispo.app.ui.theme.InkBrown
import com.dispo.app.ui.theme.SunYellow

/** Titre + hint. Le bouton vert est superposé au centre écran par DispoApp. */
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
            Text(
                text = if (state.meDispo) {
                    "Tu es dispo jusqu'à minuit"
                } else {
                    "Tape le bouton si t'es chaud ce soir"
                },
                color = Cream,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(52.dp)
                .align(Alignment.BottomCenter),
            contentAlignment = Alignment.Center,
        ) {
            if (state.chatUnlocked) {
                Text(
                    "Chat ouvert",
                    color = SunYellow,
                    fontSize = 18.sp,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
