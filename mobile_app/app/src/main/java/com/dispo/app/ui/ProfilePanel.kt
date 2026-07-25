package com.dispo.app.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dispo.app.core.DispoUiState
import com.dispo.app.core.Friend
import com.dispo.app.ui.theme.BangersFamily
import com.dispo.app.ui.theme.CircusPurple
import com.dispo.app.ui.theme.CircusRed
import com.dispo.app.ui.theme.Cream
import com.dispo.app.ui.theme.DispoGreen
import com.dispo.app.ui.theme.Gold
import com.dispo.app.ui.theme.GoldDark
import com.dispo.app.ui.theme.InkBrown
import com.dispo.app.ui.theme.LedFamily
import java.io.File
import kotlinx.coroutines.delay

val AvatarPalette = listOf(
    DispoGreen,
    Color(0xFFE85D4C),
    CircusPurple,
    Color(0xFFE8B84A),
    Color(0xFF4ECDC4),
    Color(0xFFFF6B35),
)

private val CardShape = RoundedCornerShape(20.dp)

@Composable
fun ProfilePanel(
    state: DispoUiState,
    onUpdateName: (String) -> Unit,
    onPickAvatar: (Uri) -> Unit,
    onAddFriend: (String) -> Unit,
    onRemoveFriend: (String) -> Unit,
    onClearFeedback: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val profile = state.profile
    var nameDraft by remember(profile.name) { mutableStateOf(profile.name) }
    var friendDraft by remember { mutableStateOf("") }
    var editingName by remember { mutableStateOf(false) }

    val pickMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) onPickAvatar(uri)
    }

    LaunchedEffect(state.addFriendFeedback) {
        if (state.addFriendFeedback != null) {
            delay(2200)
            onClearFeedback()
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            ProfileCard {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(108.dp)
                            .border(4.dp, Gold, CircleShape)
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(AvatarPalette[profile.avatarColor % AvatarPalette.size])
                            .clickable {
                                pickMedia.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly,
                                    ),
                                )
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        val path = profile.avatarPath
                        if (!path.isNullOrBlank() && File(path).exists()) {
                            AsyncImage(
                                model = File(path),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                            )
                        } else {
                            Text(
                                profile.name.firstOrNull()?.uppercase() ?: "?",
                                fontFamily = BangersFamily,
                                fontSize = 44.sp,
                                color = Cream,
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Mon profil",
                        fontFamily = LedFamily,
                        fontSize = 18.sp,
                        color = GoldDark,
                        letterSpacing = 1.sp,
                    )
                    Spacer(Modifier.height(4.dp))
                    if (editingName) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            ProfileField(
                                value = nameDraft,
                                onValueChange = { nameDraft = it.removePrefix("@").take(30) },
                                modifier = Modifier.weight(1f),
                                onDone = {
                                    onUpdateName(nameDraft)
                                    editingName = false
                                },
                            )
                            GreenIconButton(onClick = {
                                onUpdateName(nameDraft)
                                editingName = false
                            }) {
                                Icon(Icons.Filled.Check, contentDescription = "OK", tint = Cream)
                            }
                            IconButton(onClick = { editingName = false }) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = null,
                                    tint = InkBrown.copy(alpha = 0.5f),
                                )
                            }
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                if (profile.name.isBlank()) "@toi" else "@${profile.name}",
                                fontFamily = BangersFamily,
                                fontSize = 28.sp,
                                color = InkBrown,
                            )
                            IconButton(onClick = { editingName = true }) {
                                Icon(
                                    Icons.Filled.Edit,
                                    contentDescription = "Modifier",
                                    tint = GoldDark.copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    LedCaption(
                        text = "Appuie sur la photo pour la changer",
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        item {
            ProfileCard(title = "Amis") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    ProfileField(
                        value = friendDraft,
                        onValueChange = { friendDraft = it.removePrefix("@").take(30) },
                        modifier = Modifier.weight(1f),
                        placeholder = "@pseudo",
                        onDone = {
                            onAddFriend(friendDraft)
                            friendDraft = ""
                        },
                    )
                    GreenIconButton(onClick = {
                        onAddFriend(friendDraft)
                        friendDraft = ""
                    }) {
                        Icon(Icons.Filled.PersonAdd, contentDescription = "Ajouter", tint = Cream)
                    }
                }
                state.addFriendFeedback?.let { msg ->
                    Spacer(Modifier.height(8.dp))
                    Text(msg, color = DispoGreen, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
                if (state.friends.isEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    LedCaption(
                        text = "Ajoute des potes avec leur @",
                        fontSize = 20.sp,
                    )
                } else {
                    Spacer(Modifier.height(8.dp))
                    state.friends.forEachIndexed { index, friend ->
                        if (index > 0) {
                            HorizontalDivider(
                                color = InkBrown.copy(alpha = 0.08f),
                                modifier = Modifier.padding(vertical = 4.dp),
                            )
                        }
                        FriendRow(friend = friend, onRemove = { onRemoveFriend(friend.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileCard(
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, CardShape)
            .border(2.5.dp, InkBrown.copy(alpha = 0.15f), CardShape)
            .padding(16.dp),
    ) {
        if (title != null) {
            Text(
                title,
                fontFamily = BangersFamily,
                fontSize = 22.sp,
                color = GoldDark,
            )
            Spacer(Modifier.height(12.dp))
        }
        content()
    }
}

@Composable
private fun FriendRow(friend: Friend, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(AvatarPalette[friend.avatarColor % AvatarPalette.size], CircleShape)
                .border(
                    2.5.dp,
                    if (friend.dispo) DispoGreen else InkBrown.copy(alpha = 0.2f),
                    CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                friend.name.first().uppercase(),
                fontFamily = BangersFamily,
                fontSize = 18.sp,
                color = Cream,
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("@${friend.name}", color = InkBrown, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(
                if (friend.dispo) "Dispo ce soir" else "Pas dispo",
                fontFamily = LedFamily,
                fontSize = 16.sp,
                color = if (friend.dispo) DispoGreen else InkBrown.copy(alpha = 0.4f),
            )
        }
        IconButton(onClick = onRemove) {
            Icon(Icons.Filled.Close, contentDescription = "Retirer", tint = CircusRed.copy(alpha = 0.5f))
        }
    }
}

@Composable
private fun GreenIconButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp)
            .background(DispoGreen, CircleShape)
            .border(2.dp, InkBrown.copy(alpha = 0.2f), CircleShape),
    ) { content() }
}

@Composable
private fun ProfileField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    onDone: () -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = {
            if (placeholder.isNotEmpty()) {
                Text(placeholder, color = InkBrown.copy(alpha = 0.35f))
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GoldDark,
            unfocusedBorderColor = InkBrown.copy(alpha = 0.18f),
            focusedContainerColor = Cream,
            unfocusedContainerColor = Cream,
            cursorColor = CircusRed,
            focusedTextColor = InkBrown,
            unfocusedTextColor = InkBrown,
        ),
    )
}
