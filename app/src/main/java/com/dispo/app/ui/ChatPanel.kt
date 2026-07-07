package com.dispo.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

/**
 * Page Chat + Carte : la carte occupe tout l'écran, le chat vient
 * se poser par-dessus en panneau repliable (comme un rideau de cirque).
 */
@Composable
fun ChatPanel(
    state: DispoUiState,
    onSend: (String) -> Unit,
    onShareLocation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pins = state.messages.filter { it.hasLocation }

    Box(modifier = modifier.fillMaxSize()) {
        // Carte plein écran en fond
        CircusMap(pins = pins, modifier = Modifier.fillMaxSize())

        if (!state.chatUnlocked) {
            LockedChat(state, Modifier.align(Alignment.Center))
        } else {
            ChatOverlay(
                state = state,
                onSend = onSend,
                onShareLocation = onShareLocation,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@Composable
private fun ChatOverlay(
    state: DispoUiState,
    onSend: (String) -> Unit,
    onShareLocation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(true) }

    val listState = rememberLazyListState()
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .shadow(8.dp, RoundedCornerShape(20.dp))
            .background(Cream, RoundedCornerShape(20.dp))
            .border(3.dp, InkBrown, RoundedCornerShape(20.dp))
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
            )
            .padding(10.dp),
    ) {
        // En-tête : titre + compteur LED + repli
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CircusRed, RoundedCornerShape(14.dp))
                .border(2.5.dp, InkBrown, RoundedCornerShape(14.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { expanded = !expanded }
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                "LE CHAT DU SOIR",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                color = Cream,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                LedPanel(text = "${state.dispoCount} DISPOS", fontSize = 14.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    if (expanded) "▼" else "▲",
                    color = Cream,
                    fontSize = 16.sp,
                )
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
            ),
            exit = shrinkVertically(),
        ) {
            Column {
                Spacer(Modifier.height(8.dp))

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
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
                        placeholder = {
                            Text("Écris un message…", color = InkBrown.copy(alpha = 0.5f))
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CircusRed,
                            unfocusedBorderColor = InkBrown,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                        ),
                    )
                    Spacer(Modifier.width(6.dp))
                    // Partager un lieu : pose un pin et replie le chat pour voir la carte
                    Button(
                        onClick = {
                            onShareLocation()
                            expanded = false
                        },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = CircusPurple),
                        border = BorderStroke(3.dp, InkBrown),
                        modifier = Modifier.size(52.dp),
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        Text("📍", fontSize = 20.sp)
                    }
                    Spacer(Modifier.width(6.dp))
                    Button(
                        onClick = {
                            if (draft.isNotBlank()) {
                                onSend(draft.trim())
                                draft = ""
                            }
                        },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = DispoGreen),
                        border = BorderStroke(3.dp, InkBrown),
                        modifier = Modifier.size(52.dp),
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        Text("🚀", fontSize = 20.sp)
                    }
                }
            }
        }
    }
}

private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

@Composable
private fun MessageBubble(msg: ChatMessage) {
    val isMe = msg.authorId == "me"

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
                .shadow(3.dp, bubbleShape(isMe))
                .background(if (isMe) SunYellow else Color.White, bubbleShape(isMe))
                .border(2.5.dp, InkBrown, bubbleShape(isMe))
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
            Text(
                timeFormat.format(Date(msg.timestamp)),
                fontSize = 13.sp,
                fontFamily = LedFamily,
                color = InkBrown.copy(alpha = 0.55f),
                modifier = Modifier.align(Alignment.End),
            )
        }

        if (isMe) {
            Spacer(Modifier.width(8.dp))
            Avatar(name = "Moi")
        }
    }
}

private fun bubbleShape(isMe: Boolean) = RoundedCornerShape(
    topStart = 16.dp,
    topEnd = 16.dp,
    bottomStart = if (isMe) 16.dp else 4.dp,
    bottomEnd = if (isMe) 4.dp else 16.dp,
)

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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(24.dp)
            .shadow(8.dp, RoundedCornerShape(20.dp))
            .background(Cream, RoundedCornerShape(20.dp))
            .border(3.dp, InkBrown, RoundedCornerShape(20.dp))
            .padding(24.dp),
    ) {
        Text("🎪", fontSize = 56.sp)
        Spacer(Modifier.height(14.dp))
        LedPanel(
            text = when (state.dispoCount) {
                0 -> "EN ATTENTE DE 2 DISPOS…"
                else -> "ENCORE 1 ET C'EST PARTI !"
            },
            fontSize = 18.sp,
            blinking = true,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Le chat s'ouvre dès que 2 personnes ont tapé le bouton.",
            color = InkBrown,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
        )
    }
}
