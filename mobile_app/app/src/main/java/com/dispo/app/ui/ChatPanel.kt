package com.dispo.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dispo.app.core.ChatMessage
import com.dispo.app.core.DispoUiState
import com.dispo.app.ui.theme.CircusPurple
import com.dispo.app.ui.theme.CircusRed
import com.dispo.app.ui.theme.Cream
import com.dispo.app.ui.theme.DispoGreen
import com.dispo.app.ui.theme.InkBrown
import com.dispo.app.ui.theme.LedFamily
import com.dispo.app.ui.theme.SunYellow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val BubbleMe = RoundedCornerShape(
    topStart = 16.dp,
    topEnd = 16.dp,
    bottomStart = 16.dp,
    bottomEnd = 4.dp,
)
private val BubbleOther = RoundedCornerShape(
    topStart = 16.dp,
    topEnd = 16.dp,
    bottomStart = 4.dp,
    bottomEnd = 16.dp,
)

private val timeFormat = SimpleDateFormat("HH:mm", Locale.FRANCE)

@Composable
fun ChatPanel(
    state: DispoUiState,
    onSend: (String) -> Unit,
    onOpenMap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!state.chatUnlocked) {
        LockedChat(state, modifier)
        return
    }

    val listState = rememberLazyListState()
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            // Scroll instantané = plus fluide qu'une anim concurrente au fling
            listState.scrollToItem(state.messages.size - 1)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp)
                .background(CircusRed, RoundedCornerShape(14.dp))
                .border(3.dp, InkBrown, RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                "LE CHAT DU SOIR",
                style = MaterialTheme.typography.titleLarge,
                color = Cream,
            )
            Text(
                "${state.dispoCount} dispos",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 16.sp),
                color = Cream,
            )
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 10.dp),
        ) {
            items(
                items = state.messages,
                key = { it.id },
                contentType = { if (it.authorId == state.profile.id) "me" else "other" },
            ) { msg ->
                MessageBubble(
                    msg = msg,
                    myId = state.profile.id,
                    onOpenMap = onOpenMap,
                )
            }
        }

        // Isolé : taper ne recompose pas toute la liste de messages
        ChatInputBar(onSend = onSend, onOpenMap = onOpenMap)
    }
}

@Composable
private fun ChatInputBar(
    onSend: (String) -> Unit,
    onOpenMap: () -> Unit,
) {
    var draft by remember { mutableStateOf("") }
    val sendDraft = {
        if (draft.isNotBlank()) {
            onSend(draft.trim())
            draft = ""
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = draft,
            onValueChange = { draft = it },
            modifier = Modifier.weight(1f),
            placeholder = { Text("Écris un message…", color = InkBrown.copy(alpha = 0.5f)) },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { sendDraft() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CircusRed,
                unfocusedBorderColor = InkBrown,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
            ),
        )
        Spacer(Modifier.width(6.dp))
        Button(
            onClick = onOpenMap,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = CircusPurple),
            border = BorderStroke(3.dp, InkBrown),
            modifier = Modifier.size(52.dp),
            contentPadding = PaddingValues(0.dp),
        ) {
            Text("📍", fontSize = 20.sp)
        }
    }
}

@Composable
private fun MessageBubble(msg: ChatMessage, myId: String, onOpenMap: () -> Unit) {
    val isMe = msg.authorId == myId
    val shape = if (isMe) BubbleMe else BubbleOther
    val timeText = remember(msg.timestamp) { timeFormat.format(Date(msg.timestamp)) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        if (!isMe) {
            Avatar(name = msg.authorName)
            Spacer(Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                // Pas de .shadow() : les ombres soft coûtent très cher au scroll
                .background(if (isMe) SunYellow else Color.White, shape)
                .border(2.5.dp, InkBrown, shape)
                .then(
                    if (msg.hasLocation) Modifier.clickable(onClick = onOpenMap) else Modifier
                )
                .padding(horizontal = 14.dp, vertical = 8.dp),
        ) {
            if (!isMe) {
                Text(
                    msg.authorName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = CircusRed,
                )
            }
            Text(
                if (msg.hasLocation) "📍 ${msg.text}" else msg.text,
                fontSize = 15.sp,
                color = InkBrown,
            )
            if (msg.hasLocation) {
                Text(
                    "Voir sur la carte →",
                    fontSize = 13.sp,
                    color = CircusPurple,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline,
                )
            }
            Text(
                timeText,
                fontSize = 13.sp,
                fontFamily = LedFamily,
                color = InkBrown.copy(alpha = 0.55f),
                modifier = Modifier.align(Alignment.End),
            )
        }

        if (isMe) {
            Spacer(Modifier.width(8.dp))
            Avatar(name = msg.authorName)
        }
    }
}

@Composable
private fun Avatar(name: String) {
    val color = when (name) {
        "Moi" -> DispoGreen
        "Léa" -> CircusRed
        "Max" -> CircusPurple
        else -> SunYellow
    }
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(color, CircleShape)
            .border(2.5.dp, InkBrown, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            name.first().uppercase(),
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
            color = Cream,
        )
    }
}

@Composable
private fun LockedChat(state: DispoUiState, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .background(Cream, RoundedCornerShape(20.dp))
                .border(3.dp, InkBrown, RoundedCornerShape(20.dp))
                .padding(24.dp),
        ) {
            Text("🎪", fontSize = 56.sp)
            Spacer(Modifier.height(14.dp))
            Text(
                "En attente d'une dispo…",
                style = MaterialTheme.typography.titleLarge,
                color = CircusRed,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Le chat s'ouvre dès qu'une personne a tapé le bouton.",
                color = InkBrown,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}
