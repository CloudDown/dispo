package com.dispo.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dispo.app.core.DispoUiState
import com.dispo.app.ui.theme.DispoGreen
import com.dispo.app.ui.theme.InkBrown

@Composable
fun HomePanel(
    state: DispoUiState,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Dispo",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (state.meDispo) "Dispo jusqu'à minuit !" else "Tape le bouton si t'es chaud",
            style = MaterialTheme.typography.titleLarge,
            color = InkBrown,
        )
        Spacer(Modifier.height(24.dp))

        DispoButton(dispo = state.meDispo, onToggle = onToggle)

        Spacer(Modifier.height(32.dp))

        val dispoFriends = state.friends.filter { it.dispo }
        if (dispoFriends.isEmpty() && !state.meDispo) {
            Text("Personne n'est dispo pour l'instant…", fontSize = 16.sp, color = InkBrown)
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (state.meDispo) FriendRow(name = "Moi")
                dispoFriends.forEach { FriendRow(name = it.name) }
            }
        }

        if (state.dispoCount == 1) {
            Spacer(Modifier.height(12.dp))
            Text(
                "Encore 1 personne dispo et le chat s'ouvre !",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.tertiary,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun FriendRow(name: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(10.dp)
                .background(DispoGreen, CircleShape)
        )
        Spacer(Modifier.size(8.dp))
        Text("$name est dispo 😀", fontSize = 16.sp, color = InkBrown)
    }
}
