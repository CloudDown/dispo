package com.dispo.app.ui

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dispo.app.core.DispoUiState
import com.dispo.app.core.Friend
import com.dispo.app.ui.theme.CircusPurple
import com.dispo.app.ui.theme.Cream
import com.dispo.app.ui.theme.DispoGreen
import com.dispo.app.ui.theme.InkBrown
import kotlinx.coroutines.delay

val AvatarPalette = listOf(
    DispoGreen,
    Color(0xFFE85D4C),
    CircusPurple,
    Color(0xFFE8B84A),
    Color(0xFF4ECDC4),
    Color(0xFFFF6B35),
)

// Palette profil — plus douce / lisible
private val ProfileBg = Color(0xFFFFF6EC)
private val ProfileInk = Color(0xFF2C241C)
private val ProfileMuted = Color(0xFF7A6E62)
private val ProfileLine = Color(0xFFE5D9C8)
private val ProfileAccent = Color(0xFFD64545)
private val ProfileField = Color(0xFFFFFBF5)
private val ProfileSave = Color(0xFF2BB673)

private val FieldShape = RoundedCornerShape(14.dp)

@Composable
private fun profileFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = ProfileAccent,
    unfocusedBorderColor = ProfileLine,
    focusedContainerColor = ProfileField,
    unfocusedContainerColor = ProfileField,
    focusedLabelColor = ProfileAccent,
    unfocusedLabelColor = ProfileMuted,
    cursorColor = ProfileAccent,
    focusedTextColor = ProfileInk,
    unfocusedTextColor = ProfileInk,
)

@Composable
fun ProfilePanel(
    state: DispoUiState,
    onUpdateName: (String) -> Unit,
    onCycleAvatar: () -> Unit,
    onAddFriend: (String) -> Unit,
    onRemoveFriend: (String) -> Unit,
    onClearFeedback: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val profile = state.profile
    var nameDraft by remember(profile.name) { mutableStateOf(profile.name) }
    var friendNameDraft by remember { mutableStateOf("") }

    LaunchedEffect(state.addFriendFeedback) {
        if (state.addFriendFeedback != null) {
            delay(2500)
            onClearFeedback()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ProfileBg)
            .padding(horizontal = 22.dp),
        contentPadding = PaddingValues(top = 20.dp, bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .background(
                            AvatarPalette[profile.avatarColor % AvatarPalette.size],
                            CircleShape,
                        )
                        .border(2.dp, ProfileInk.copy(alpha = 0.15f), CircleShape)
                        .clickable(onClick = onCycleAvatar),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        profile.name.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 36.sp),
                        color = Cream,
                    )
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    if (profile.name.isBlank()) "Ton profil" else "@${profile.name}",
                    color = ProfileInk,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Touche l’avatar pour la couleur",
                    fontSize = 13.sp,
                    color = ProfileMuted,
                )
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Pseudo",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = ProfileMuted,
                )
                OutlinedTextField(
                    value = nameDraft,
                    onValueChange = { nameDraft = it.removePrefix("@").take(30) },
                    placeholder = { Text("comme sur Instagram") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = FieldShape,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { onUpdateName(nameDraft) },
                    ),
                    colors = profileFieldColors(),
                )
                Button(
                    onClick = { onUpdateName(nameDraft) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ProfileSave,
                        contentColor = Color.White,
                    ),
                    shape = FieldShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                ) {
                    Text("Enregistrer", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
            }
        }

        item {
            HorizontalDivider(color = ProfileLine, thickness = 1.dp)
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Ajouter au crew",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = ProfileMuted,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = friendNameDraft,
                        onValueChange = { friendNameDraft = it.removePrefix("@").take(30) },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("pseudo") },
                        singleLine = true,
                        shape = FieldShape,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                onAddFriend(friendNameDraft)
                                friendNameDraft = ""
                            },
                        ),
                        colors = profileFieldColors(),
                    )
                    Button(
                        onClick = {
                            onAddFriend(friendNameDraft)
                            friendNameDraft = ""
                        },
                        shape = FieldShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ProfileInk,
                            contentColor = Cream,
                        ),
                        modifier = Modifier.size(48.dp),
                        contentPadding = PaddingValues(0.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    ) {
                        Text("+", fontSize = 22.sp, fontWeight = FontWeight.Medium)
                    }
                }
                state.addFriendFeedback?.let { msg ->
                    Text(
                        msg,
                        color = ProfileSave,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                    )
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Crew",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = ProfileMuted,
                    )
                    Text(
                        "${state.friends.size}",
                        fontSize = 13.sp,
                        color = ProfileMuted,
                    )
                }

                if (state.friends.isEmpty()) {
                    Text(
                        "Personne pour l’instant — ajoute un pseudo au-dessus.",
                        color = ProfileMuted,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 10.dp, bottom = 4.dp),
                    )
                }
            }
        }

        items(state.friends, key = { it.id }) { friend ->
            FriendRow(
                friend = friend,
                onRemove = { onRemoveFriend(friend.id) },
            )
        }

        item {
            Text(
                "Démo · lea · max · sam",
                fontSize = 12.sp,
                color = ProfileMuted.copy(alpha = 0.65f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun FriendRow(friend: Friend, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ProfileField)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    AvatarPalette[friend.avatarColor % AvatarPalette.size],
                    CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                friend.name.first().uppercase(),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Cream,
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "@${friend.name}",
                fontWeight = FontWeight.SemiBold,
                color = ProfileInk,
                fontSize = 15.sp,
            )
            if (friend.dispo) {
                Text(
                    "Dispo",
                    fontSize = 12.sp,
                    color = ProfileSave,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
        TextButton(onClick = onRemove) {
            Text(
                "Retirer",
                color = ProfileAccent,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
            )
        }
    }
}
