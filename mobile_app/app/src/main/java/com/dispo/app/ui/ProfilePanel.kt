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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PhotoCamera
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
import com.dispo.app.ui.theme.InkBrown
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

private val FieldShape = RoundedCornerShape(16.dp)

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
        modifier = modifier
            .fillMaxSize()
            .background(Cream),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(104.dp)
                        .clip(CircleShape)
                        .background(AvatarPalette[profile.avatarColor % AvatarPalette.size])
                        .border(3.dp, InkBrown, CircleShape)
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
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(InkBrown, CircleShape)
                        .border(2.dp, Cream, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.PhotoCamera,
                        contentDescription = "Photo",
                        tint = Cream,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }

        item {
            if (editingName) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    OutlinedTextField(
                        value = nameDraft,
                        onValueChange = { nameDraft = it.removePrefix("@").take(30) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = FieldShape,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                onUpdateName(nameDraft)
                                editingName = false
                            },
                        ),
                        colors = fieldColors(),
                    )
                    IconButton(
                        onClick = {
                            onUpdateName(nameDraft)
                            editingName = false
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(DispoGreen, CircleShape),
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = "OK", tint = Color.White)
                    }
                    IconButton(
                        onClick = { editingName = false },
                        modifier = Modifier
                            .size(48.dp)
                            .background(InkBrown.copy(alpha = 0.15f), CircleShape),
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Annuler", tint = InkBrown)
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
                            tint = InkBrown.copy(alpha = 0.55f),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = friendDraft,
                    onValueChange = { friendDraft = it.removePrefix("@").take(30) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("@pseudo") },
                    singleLine = true,
                    shape = FieldShape,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            onAddFriend(friendDraft)
                            friendDraft = ""
                        },
                    ),
                    colors = fieldColors(),
                )
                IconButton(
                    onClick = {
                        onAddFriend(friendDraft)
                        friendDraft = ""
                    },
                    modifier = Modifier
                        .size(54.dp)
                        .background(CircusRed, CircleShape)
                        .border(2.dp, InkBrown, CircleShape),
                ) {
                    Icon(
                        Icons.Filled.PersonAdd,
                        contentDescription = "Ajouter",
                        tint = Cream,
                    )
                }
            }
            state.addFriendFeedback?.let { msg ->
                Spacer(Modifier.height(8.dp))
                Text(msg, color = DispoGreen, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }

        items(state.friends, key = { it.id }) { friend ->
            FriendRow(
                friend = friend,
                onRemove = { onRemoveFriend(friend.id) },
            )
        }

        if (state.friends.isEmpty()) {
            item {
                Icon(
                    Icons.Filled.PersonAdd,
                    contentDescription = null,
                    tint = InkBrown.copy(alpha = 0.25f),
                    modifier = Modifier.size(40.dp),
                )
            }
        }
    }
}

@Composable
private fun FriendRow(friend: Friend, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(2.dp, InkBrown.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(
                    AvatarPalette[friend.avatarColor % AvatarPalette.size],
                    CircleShape,
                )
                .border(2.dp, InkBrown, CircleShape),
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
            Text("@${friend.name}", fontWeight = FontWeight.SemiBold, color = InkBrown)
            if (friend.dispo) {
                Box(
                    modifier = Modifier
                        .padding(top = 3.dp)
                        .size(8.dp)
                        .background(DispoGreen, CircleShape),
                )
            }
        }
        IconButton(onClick = onRemove) {
            Icon(
                Icons.Filled.Close,
                contentDescription = "Retirer",
                tint = CircusRed,
            )
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = CircusRed,
    unfocusedBorderColor = InkBrown.copy(alpha = 0.3f),
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    cursorColor = CircusRed,
    focusedTextColor = InkBrown,
    unfocusedTextColor = InkBrown,
)
