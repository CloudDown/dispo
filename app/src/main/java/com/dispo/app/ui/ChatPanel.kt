package com.dispo.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dispo.app.core.ChatMessage
import com.dispo.app.core.DispoUiState
import com.dispo.app.ui.theme.CircusPurple
import com.dispo.app.ui.theme.Cream
import com.dispo.app.ui.theme.DispoGreen
import com.dispo.app.ui.theme.InkBrown
import com.dispo.app.ui.theme.SunYellow

@Composable
fun ChatPanel(
    state: DispoUiState,
    onSend: (String) -> Unit,
    onShareLocation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!state.chatUnlocked) {
        LockedChat(state, modifier)
        return
    }

    val listState = rememberLazyListState()
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Column(modifier = modifier.fillMaxSize().padding(12.dp)) {
        Text(
            "Le chat du jour 🎪",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(8.dp))

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(state.messages, key = { it.id }) { msg ->
                MessageBubble(msg)
            }
        }

        Spacer(Modifier.height(8.dp))

        var draft by remember { mutableStateOf("") }
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Écris un message…") },
                singleLine = true,
            )
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = {
                    if (draft.isNotBlank()) {
                        onSend(draft.trim())
                        draft = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = DispoGreen),
            ) {
                Text("Go")
            }
        }
        Spacer(Modifier.height(4.dp))
        Button(
            onClick = onShareLocation,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = CircusPurple),
        ) {
            Text("📍 Partager un lieu")
        }
    }
}

@Composable
private fun MessageBubble(msg: ChatMessage) {
    val isMe = msg.authorId == "me"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
    ) {
        Column(
            modifier = Modifier
                .background(
                    if (isMe) SunYellow else Cream,
                    RoundedCornerShape(14.dp),
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Text(
                msg.authorName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                if (msg.hasLocation) "📍 ${msg.text}" else msg.text,
                fontSize = 15.sp,
                color = InkBrown,
            )
        }
    }
}

@Composable
private fun LockedChat(state: DispoUiState, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🔒", fontSize = 48.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                when (state.dispoCount) {
                    0 -> "Le chat s'ouvre quand 2 personnes sont dispos."
                    else -> "Encore 1 personne dispo et le chat s'ouvre !"
                },
                style = MaterialTheme.typography.titleLarge,
                color = InkBrown,
            )
        }
    }
}
