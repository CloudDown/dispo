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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.dispo.app.ui.theme.DarkBorder
import com.dispo.app.ui.theme.DarkField
import com.dispo.app.ui.theme.DarkSurface
import com.dispo.app.ui.theme.DarkText
import com.dispo.app.ui.theme.DarkTextMuted
import com.dispo.app.ui.theme.DispoGreen
import com.dispo.app.ui.theme.Gold
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

private fun feedbackColor(msg: String): Color {
    val lower = msg.lowercase()
    val isError = listOf(
        "invalide", "inconnu", "requis", "déjà", "erreur", "échou", "pas de",
        "impossible", "entre un", "propre nom",
    ).any { it in lower }
    return if (isError) CircusRed else DispoGreen
}

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
    var showAddFriend by remember { mutableStateOf(false) }
    var showEditName by remember { mutableStateOf(false) }

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
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        if (profile.name.isBlank()) "@toi" else "@${profile.name}",
                        fontFamily = BangersFamily,
                        fontSize = 28.sp,
                        color = DarkText,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LedCaption(
                        text = "Appuie sur la photo pour la changer",
                        fontSize = 18.sp,
                        color = DarkTextMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        item {
            ProfileCard(title = "Paramètres") {
                SettingsRow(
                    icon = Icons.Filled.Edit,
                    label = "Modifier le pseudo",
                    onClick = { showEditName = true },
                )
                HorizontalDivider(
                    color = DarkBorder.copy(alpha = 0.35f),
                    modifier = Modifier.padding(vertical = 4.dp),
                )
                SettingsRow(
                    icon = Icons.Filled.PersonAdd,
                    label = "Ajouter un ami",
                    onClick = { showAddFriend = true },
                )
                state.addFriendFeedback?.let { msg ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        msg,
                        color = feedbackColor(msg),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }

        item {
            ProfileCard(title = "Amis") {
                if (state.friends.isEmpty()) {
                    LedCaption(
                        text = "Personne pour l’instant — ajoute un ami dans Paramètres.",
                        fontSize = 18.sp,
                        color = DarkTextMuted,
                    )
                } else {
                    state.friends.forEachIndexed { index, friend ->
                        if (index > 0) {
                            HorizontalDivider(
                                color = DarkBorder.copy(alpha = 0.4f),
                                modifier = Modifier.padding(vertical = 4.dp),
                            )
                        }
                        FriendRow(friend = friend, onRemove = { onRemoveFriend(friend.id) })
                    }
                }
            }
        }
    }

    if (showAddFriend) {
        ProfileNameDialog(
            title = "Ajouter un ami",
            placeholder = "pseudo Instagram",
            confirmLabel = "Ajouter",
            onDismiss = { showAddFriend = false },
            onConfirm = { name ->
                onAddFriend(name)
                showAddFriend = false
            },
        )
    }

    if (showEditName) {
        ProfileNameDialog(
            title = "Modifier le pseudo",
            placeholder = "ton @",
            initialValue = profile.name,
            confirmLabel = "Enregistrer",
            onDismiss = { showEditName = false },
            onConfirm = { name ->
                onUpdateName(name)
                showEditName = false
            },
        )
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Gold.copy(alpha = 0.18f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = Gold, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            label,
            modifier = Modifier.weight(1f),
            color = DarkText,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
        )
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = DarkTextMuted,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun ProfileNameDialog(
    title: String,
    placeholder: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    initialValue: String = "",
) {
    var draft by remember(initialValue) {
        mutableStateOf(initialValue.removePrefix("@"))
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        titleContentColor = DarkText,
        textContentColor = DarkTextMuted,
        title = {
            Text(title, fontWeight = FontWeight.SemiBold, color = DarkText)
        },
        text = {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it.removePrefix("@").take(30) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(placeholder, color = DarkTextMuted) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { if (draft.isNotBlank()) onConfirm(draft.trim()) },
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Gold,
                    unfocusedBorderColor = DarkBorder,
                    focusedContainerColor = DarkField,
                    unfocusedContainerColor = DarkField,
                    cursorColor = Gold,
                    focusedTextColor = DarkText,
                    unfocusedTextColor = DarkText,
                ),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (draft.isNotBlank()) onConfirm(draft.trim()) },
                enabled = draft.isNotBlank(),
            ) {
                Text(
                    confirmLabel,
                    color = if (draft.isNotBlank()) DispoGreen else DarkTextMuted,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = DarkTextMuted)
            }
        },
    )
}

@Composable
private fun ProfileCard(
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface, CardShape)
            .border(2.5.dp, DarkBorder.copy(alpha = 0.5f), CardShape)
            .padding(16.dp),
    ) {
        if (title != null) {
            Text(
                title,
                fontFamily = BangersFamily,
                fontSize = 22.sp,
                color = Gold,
            )
            Spacer(modifier = Modifier.height(12.dp))
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
                    if (friend.dispo) DispoGreen else DarkBorder,
                    CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                friend.name.firstOrNull()?.uppercase() ?: "?",
                fontFamily = BangersFamily,
                fontSize = 18.sp,
                color = Cream,
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("@${friend.name}", color = DarkText, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(
                if (friend.dispo) "Dispo ce soir" else "Pas dispo",
                fontFamily = LedFamily,
                fontSize = 16.sp,
                color = if (friend.dispo) DispoGreen else DarkTextMuted,
            )
        }
        IconButton(onClick = onRemove) {
            Icon(Icons.Filled.Close, contentDescription = "Retirer", tint = CircusRed.copy(alpha = 0.7f))
        }
    }
}
