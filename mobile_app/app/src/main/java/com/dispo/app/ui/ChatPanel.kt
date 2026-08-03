package com.dispo.app.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dispo.app.R
import com.dispo.app.core.ChatMessage
import com.dispo.app.core.CrewGroup
import com.dispo.app.core.DispoUiState
import com.dispo.app.core.Friend
import com.dispo.app.core.GoogleMapsUrls
import com.dispo.app.ui.theme.CircusPurple
import com.dispo.app.ui.theme.CircusRed
import com.dispo.app.ui.theme.Cream
import com.dispo.app.ui.theme.DarkBg
import com.dispo.app.ui.theme.DarkBorder
import com.dispo.app.ui.theme.DarkField
import com.dispo.app.ui.theme.DarkSurface
import com.dispo.app.ui.theme.DarkSurfaceRaised
import com.dispo.app.ui.theme.DarkText
import com.dispo.app.ui.theme.DarkTextMuted
import com.dispo.app.ui.theme.DispoGreen
import com.dispo.app.ui.theme.Gold
import com.dispo.app.ui.theme.GoldDark
import com.dispo.app.ui.theme.LedFamily
import com.dispo.app.ui.theme.SunYellow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlinx.coroutines.delay

private enum class ChatSection { Messages, Groupes }

private val BubbleMe = RoundedCornerShape(
    topStart = 20.dp,
    topEnd = 20.dp,
    bottomStart = 20.dp,
    bottomEnd = 6.dp,
)
private val BubbleOther = RoundedCornerShape(
    topStart = 20.dp,
    topEnd = 20.dp,
    bottomStart = 6.dp,
    bottomEnd = 20.dp,
)
private val CardShape = RoundedCornerShape(18.dp)

private val BubbleMeBg = Color(0xFF2A5A3D)
private val BubbleOtherBg = Color(0xFF2E2520)
private val LocationCardBg = Color(0xFF1A2420)

private val timeFormat = SimpleDateFormat("HH:mm", Locale.FRANCE)

/** Vert si succès, rouge si message d’erreur connu. */
private fun feedbackColor(msg: String): Color {
    val lower = msg.lowercase()
    val isError = listOf(
        "invalide", "inconnu", "requis", "déjà", "erreur", "échou", "pas de",
        "impossible", "entre un", "propre nom",
    ).any { it in lower }
    return if (isError) CircusRed else DispoGreen
}

@Composable
fun ChatPanel(
    state: DispoUiState,
    onSend: (String) -> Unit,
    onOpenMap: () -> Unit,
    onCreateGroup: (onCreated: (groupId: String) -> Unit) -> Unit,
    onRenameGroup: (groupId: String, name: String) -> Unit,
    onJoinGroup: (String) -> Unit,
    onAddFriendToGroup: (groupId: String, friendId: String) -> Unit,
    onLeaveGroup: (String) -> Unit,
    onSelectGroup: (String) -> Unit,
    onClearFeedback: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var section by remember { mutableStateOf(ChatSection.Messages) }
    var expandedGroupId by remember { mutableStateOf<String?>(null) }
    var renameGroupId by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    LaunchedEffect(state.addFriendFeedback) {
        if (state.addFriendFeedback != null) {
            delay(2200)
            onClearFeedback()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        ChatSectionToggle(
            selected = section,
            groupCount = state.groups.size,
            onSelect = { section = it },
        )

        Spacer(Modifier.height(10.dp))

        when (section) {
            ChatSection.Messages -> ChatMessagesSection(
                modifier = Modifier.weight(1f),
                state = state,
                onSend = onSend,
                onOpenMap = onOpenMap,
            )
            ChatSection.Groupes -> ChatGroupsSection(
                modifier = Modifier.weight(1f),
                state = state,
                expandedGroupId = expandedGroupId,
                onExpandGroup = { id ->
                    expandedGroupId = if (expandedGroupId == id) null else id
                },
                onCreateGroup = {
                    onCreateGroup { id ->
                        renameGroupId = id
                        expandedGroupId = id
                    }
                },
                onRequestRename = { id -> renameGroupId = id },
                onJoinGroup = onJoinGroup,
                onCopyCode = { code ->
                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(ClipData.newPlainText("code", code))
                },
                onAddFriendToGroup = onAddFriendToGroup,
                onLeaveGroup = onLeaveGroup,
                onSelectGroup = onSelectGroup,
            )
        }
    }

    renameGroupId?.let { groupId ->
        val currentName = state.groups.find { it.id == groupId }?.name.orEmpty()
        RenameGroupDialog(
            initialName = currentName,
            onDismiss = { renameGroupId = null },
            onConfirm = { name ->
                onRenameGroup(groupId, name)
                renameGroupId = null
            },
        )
    }
}

@Composable
private fun ChatSectionToggle(
    selected: ChatSection,
    groupCount: Int,
    onSelect: (ChatSection) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ChatSectionChip(
            label = "Messages",
            emoji = "💬",
            selected = selected == ChatSection.Messages,
            onClick = { onSelect(ChatSection.Messages) },
            modifier = Modifier.weight(1f),
        )
        ChatSectionChip(
            label = "Groupes",
            emoji = "👥",
            badge = if (groupCount > 0) groupCount.toString() else null,
            selected = selected == ChatSection.Groupes,
            onClick = { onSelect(ChatSection.Groupes) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ChatSectionChip(
    label: String,
    emoji: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null,
) {
    val bg = if (selected) CircusRed else DarkSurface
    val textColor = if (selected) Cream else DarkTextMuted
    Row(
        modifier = modifier
            .background(bg, RoundedCornerShape(14.dp))
            .border(
                width = if (selected) 2.5.dp else 2.dp,
                color = if (selected) DarkBorder else DarkBorder.copy(alpha = 0.6f),
                shape = RoundedCornerShape(14.dp),
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(emoji, fontSize = 16.sp)
        Spacer(Modifier.width(6.dp))
        Text(label, fontFamily = LedFamily, fontSize = 18.sp, color = textColor)
        if (badge != null) {
            Spacer(Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .background(if (selected) Gold else DispoGreen, CircleShape)
                    .padding(horizontal = 7.dp, vertical = 2.dp),
            ) {
                Text(badge, fontSize = 12.sp, color = DarkBg, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ChatMessagesSection(
    state: DispoUiState,
    onSend: (String) -> Unit,
    onOpenMap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!state.chatUnlocked) {
        LockedChat(modifier = modifier.fillMaxWidth())
        return
    }

    val listState = rememberLazyListState()
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.scrollToItem(state.messages.size - 1)
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        state.activeGroupId?.let { gid ->
            val groupName = state.groups.find { it.id == gid }?.name
            if (groupName != null) {
                LedCaption(
                    text = "Groupe : $groupName",
                    fontSize = 18.sp,
                    color = Gold,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFF181410))
                .border(1.5.dp, DarkBorder.copy(alpha = 0.35f), RoundedCornerShape(22.dp)),
        ) {
            if (state.messages.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(Gold.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("💬", fontSize = 32.sp)
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Rien pour l’instant",
                        color = DarkText,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Envoie un message ou un lieu sur la carte",
                        color = DarkTextMuted,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 14.dp),
                ) {
                    items(
                        items = state.messages,
                        key = { it.id },
                        contentType = { if (it.authorId == state.profile.id) "me" else "other" },
                    ) { msg ->
                        MessageBubble(
                            msg = msg,
                            myId = state.profile.id,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        ChatInputBar(onSend = onSend, onOpenMap = onOpenMap)
    }
}

@Composable
private fun ChatGroupsSection(
    state: DispoUiState,
    expandedGroupId: String?,
    onExpandGroup: (String) -> Unit,
    onCreateGroup: () -> Unit,
    onRequestRename: (String) -> Unit,
    onJoinGroup: (String) -> Unit,
    onCopyCode: (String) -> Unit,
    onAddFriendToGroup: (String, String) -> Unit,
    onLeaveGroup: (String) -> Unit,
    onSelectGroup: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var joinCodeDraft by remember { mutableStateOf("") }

    // Ne vide le champ qu’en cas de succès (sinon on perd le code après une erreur)
    LaunchedEffect(state.addFriendFeedback) {
        val msg = state.addFriendFeedback ?: return@LaunchedEffect
        val lower = msg.lowercase()
        val joinedOk = lower.startsWith("bienvenue") || "déjà dans ce groupe" in lower
        if (joinedOk) joinCodeDraft = ""
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 12.dp),
    ) {
        item {
            Button(
                onClick = onCreateGroup,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DispoGreen),
                border = BorderStroke(2.dp, DarkBorder),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, tint = Cream)
                Spacer(Modifier.width(8.dp))
                Text("Créer un groupe", color = Cream, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = joinCodeDraft,
                    onValueChange = {
                        joinCodeDraft = it.uppercase().filter { c -> c.isLetterOrDigit() }.take(12)
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text("Code d’invitation", color = DarkTextMuted.copy(alpha = 0.7f), fontSize = 14.sp)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (joinCodeDraft.length >= 4) onJoinGroup(joinCodeDraft)
                        },
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Gold,
                        unfocusedBorderColor = DarkBorder.copy(alpha = 0.6f),
                        focusedContainerColor = DarkField,
                        unfocusedContainerColor = DarkField,
                        cursorColor = Gold,
                        focusedTextColor = DarkText,
                        unfocusedTextColor = DarkText,
                    ),
                )
                Button(
                    onClick = { onJoinGroup(joinCodeDraft) },
                    enabled = joinCodeDraft.length >= 4,
                    modifier = Modifier.height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Gold,
                        disabledContainerColor = DarkBorder.copy(alpha = 0.35f),
                        contentColor = DarkBg,
                        disabledContentColor = DarkTextMuted,
                    ),
                    border = BorderStroke(2.dp, DarkBorder.copy(alpha = 0.6f)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                ) {
                    Text("OK", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        state.addFriendFeedback?.let { msg ->
            item {
                Text(
                    msg,
                    color = feedbackColor(msg),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 2.dp),
                )
            }
        }

        item {
            Text(
                "Tes groupes",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = DarkTextMuted,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        if (state.groups.isEmpty()) {
            item {
                Text(
                    "Aucun groupe pour l’instant.",
                    color = DarkTextMuted,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
        } else {
            items(state.groups, key = { it.id }) { group ->
                GroupRow(
                    group = group,
                    friends = state.friends,
                    myId = state.profile.id,
                    isActive = state.activeGroupId == group.id,
                    expanded = expandedGroupId == group.id,
                    onToggle = { onExpandGroup(group.id) },
                    onSelect = { onSelectGroup(group.id) },
                    onRename = { onRequestRename(group.id) },
                    onCopyCode = { onCopyCode(group.inviteCode) },
                    onAddFriend = { onAddFriendToGroup(group.id, it) },
                    onLeave = { onLeaveGroup(group.id) },
                )
            }
        }
    }
}

@Composable
private fun RenameGroupDialog(
    initialName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var draft by remember(initialName) {
        mutableStateOf(if (initialName == "Nouveau groupe") "" else initialName)
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        titleContentColor = DarkText,
        textContentColor = DarkTextMuted,
        title = {
            Text("Nommer le groupe", fontWeight = FontWeight.SemiBold, color = DarkText)
        },
        text = {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it.take(64) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Ex : Les copains", color = DarkTextMuted) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
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
                Text("Enregistrer", color = if (draft.isNotBlank()) DispoGreen else DarkTextMuted)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Plus tard", color = DarkTextMuted)
            }
        },
    )
}

@Composable
private fun GroupRow(
    group: CrewGroup,
    friends: List<Friend>,
    myId: String,
    isActive: Boolean,
    expanded: Boolean,
    onToggle: () -> Unit,
    onSelect: () -> Unit,
    onRename: () -> Unit,
    onCopyCode: () -> Unit,
    onAddFriend: (String) -> Unit,
    onLeave: () -> Unit,
) {
    val addable = friends.filter { it.id !in group.memberIds }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface, CardShape)
            .border(
                2.5.dp,
                if (isActive) Gold else DarkBorder.copy(alpha = 0.5f),
                CardShape,
            )
            .clickable(onClick = onToggle)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Gold.copy(alpha = 0.25f), CircleShape)
                    .border(2.dp, GoldDark.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text("👥", fontSize = 18.sp)
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        group.name,
                        color = DarkText,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp,
                        modifier = Modifier.clickable { onRename() },
                    )
                    if (isActive) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "ACTIF",
                            fontFamily = LedFamily,
                            fontSize = 14.sp,
                            color = Gold,
                        )
                    }
                }
                Text(
                    "${group.memberIds.size} membre${if (group.memberIds.size > 1) "s" else ""}",
                    fontFamily = LedFamily,
                    fontSize = 16.sp,
                    color = DarkTextMuted,
                )
            }
            Text(
                if (expanded) "▲" else "▼",
                color = DarkTextMuted.copy(alpha = 0.6f),
                fontSize = 12.sp,
            )
        }
        if (expanded) {
            HorizontalDivider(color = DarkBorder.copy(alpha = 0.4f))
            if (!isActive) {
                Button(
                    onClick = onSelect,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DispoGreen),
                ) {
                    Text("Utiliser pour le chat", color = Cream, fontWeight = FontWeight.SemiBold)
                }
            }
            LedCaption(text = "Code à partager", fontSize = 16.sp, color = DarkTextMuted)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkField, RoundedCornerShape(12.dp))
                    .border(2.dp, DarkBorder, RoundedCornerShape(12.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(group.inviteCode, fontFamily = LedFamily, color = Gold, fontSize = 22.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onCopyCode, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Filled.ContentCopy,
                            contentDescription = "Copier le code",
                            tint = Gold,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    IconButton(onClick = onLeave, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Filled.ExitToApp,
                            contentDescription = "Quitter",
                            tint = CircusRed.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
            LedCaption(text = "Membres", fontSize = 16.sp, color = DarkTextMuted)
            group.memberIds.forEach { id ->
                Text(
                    if (id == myId) "@$id · toi" else "@$id",
                    fontFamily = LedFamily,
                    fontSize = 17.sp,
                    color = DarkText.copy(alpha = 0.85f),
                )
            }
            if (addable.isNotEmpty()) {
                LedCaption(text = "Ajouter un ami", fontSize = 16.sp, color = DarkTextMuted)
                addable.forEach { friend ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAddFriend(friend.id) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Filled.PersonAdd, null, tint = DispoGreen, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "@${friend.name}",
                            fontFamily = LedFamily,
                            fontSize = 17.sp,
                            color = DarkText,
                        )
                    }
                }
            }
        }
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
            .clip(RoundedCornerShape(28.dp))
            .background(DarkSurfaceRaised)
            .border(1.5.dp, DarkBorder.copy(alpha = 0.55f), RoundedCornerShape(28.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onOpenMap,
            modifier = Modifier
                .size(42.dp)
                .background(CircusPurple.copy(alpha = 0.85f), CircleShape),
        ) {
            Text("📍", fontSize = 16.sp)
        }
        OutlinedTextField(
            value = draft,
            onValueChange = { draft = it },
            modifier = Modifier.weight(1f),
            placeholder = {
                Text("Message…", color = DarkTextMuted.copy(alpha = 0.75f), fontSize = 15.sp)
            },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { sendDraft() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                cursorColor = Gold,
                focusedTextColor = DarkText,
                unfocusedTextColor = DarkText,
            ),
        )
        IconButton(
            onClick = sendDraft,
            enabled = draft.isNotBlank(),
            modifier = Modifier
                .size(42.dp)
                .background(
                    if (draft.isNotBlank()) DispoGreen else DarkBorder.copy(alpha = 0.45f),
                    CircleShape,
                ),
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Send,
                contentDescription = "Envoyer",
                tint = if (draft.isNotBlank()) Cream else DarkTextMuted,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun MessageBubble(msg: ChatMessage, myId: String) {
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
            modifier = Modifier.widthIn(max = 290.dp),
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
        ) {
            if (msg.hasLocation) {
                LocationBubble(
                    msg = msg,
                    isMe = isMe,
                    shape = shape,
                    timeText = timeText,
                )
            } else {
                Column(
                    modifier = Modifier
                        .background(if (isMe) BubbleMeBg else BubbleOtherBg, shape)
                        .border(
                            1.dp,
                            if (isMe) DispoGreen.copy(alpha = 0.35f) else DarkBorder.copy(alpha = 0.4f),
                            shape,
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                ) {
                    if (!isMe) {
                        Text(
                            msg.authorName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Gold,
                        )
                        Spacer(Modifier.height(4.dp))
                    }
                    Text(
                        msg.text,
                        fontSize = 15.sp,
                        color = DarkText,
                        lineHeight = 20.sp,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        timeText,
                        fontSize = 11.sp,
                        color = DarkTextMuted,
                        modifier = Modifier.align(Alignment.End),
                    )
                }
            }
        }

        if (isMe) {
            Spacer(Modifier.width(8.dp))
            Avatar(name = msg.authorName)
        }
    }
}

@Composable
private fun LocationBubble(
    msg: ChatMessage,
    isMe: Boolean,
    shape: RoundedCornerShape,
    timeText: String,
) {
    val lat = msg.lat ?: return
    val lon = msg.lon ?: return
    val context = LocalContext.current
    val mapsLink = remember(msg.text, lat, lon) {
        msg.text.takeIf { GoogleMapsUrls.isMapsLink(it) }
            ?: GoogleMapsUrls.placeLink(lat, lon)
    }
    val previewUrl = remember(lat, lon) { GoogleMapsUrls.previewImageUrl(lat, lon) }
    val coords = remember(lat, lon) { formatCoords(lat, lon) }

    fun openGoogleMaps() {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(mapsLink)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
        )
    }

    Column(
        modifier = Modifier
            .widthIn(min = 236.dp, max = 290.dp)
            .clip(shape)
            .background(LocationCardBg)
            .border(1.5.dp, Gold.copy(alpha = 0.35f), shape)
            .clickable(onClick = ::openGoogleMaps),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(118.dp)
                .background(Color(0xFFE8E8E8)),
        ) {
            AsyncImage(
                model = previewUrl,
                contentDescription = "Aperçu du lieu",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            Image(
                painter = painterResource(R.drawable.pin_map),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = 18.dp)
                    .size(width = 28.dp, height = 36.dp),
            )
            Text(
                "Lieu",
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.92f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                color = Color(0xFF333333),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (!isMe) {
                Text(
                    "@${msg.authorName}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Gold,
                )
            }
            Text(
                coords,
                fontFamily = LedFamily,
                fontSize = 15.sp,
                color = DarkTextMuted,
                letterSpacing = 0.5.sp,
            )
            Text(
                mapsLink,
                fontSize = 12.sp,
                color = DispoGreen.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DispoGreen.copy(alpha = 0.18f))
                    .border(1.dp, DispoGreen.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Ouvrir dans Google Maps",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DispoGreen,
                )
                Text("→", fontSize = 16.sp, color = DispoGreen)
            }
            Text(
                timeText,
                fontSize = 11.sp,
                color = DarkTextMuted.copy(alpha = 0.8f),
                modifier = Modifier.align(Alignment.End),
            )
        }
    }
}

private fun formatCoords(lat: Double, lon: Double): String {
    val latHem = if (lat >= 0) "N" else "S"
    val lonHem = if (lon >= 0) "E" else "O"
    return String.format(
        Locale.US,
        "%.4f° %s  ·  %.4f° %s",
        abs(lat),
        latHem,
        abs(lon),
        lonHem,
    )
}

@Composable
private fun Avatar(name: String) {
    val color = when (name.lowercase()) {
        "moi", "toi" -> DispoGreen
        "lea", "léa" -> CircusRed
        "max" -> CircusPurple
        else -> SunYellow
    }
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(color, CircleShape)
            .border(2.5.dp, DarkBorder, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            name.firstOrNull()?.uppercase() ?: "?",
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
            color = Cream,
        )
    }
}

@Composable
private fun LockedChat(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface, RoundedCornerShape(24.dp))
                .border(3.dp, DarkBorder, RoundedCornerShape(24.dp))
                .padding(28.dp),
        ) {
            Text("🎪", fontSize = 56.sp)
            Spacer(Modifier.height(14.dp))
            Text(
                "En attente d'une dispo…",
                style = MaterialTheme.typography.titleLarge,
                color = CircusRed,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            LedCaption(
                text = "Le chat s'ouvre dès qu'une personne a tapé le bouton.",
                fontSize = 20.sp,
                color = DarkTextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))
            LedCaption(
                text = "En attendant, crée ton crew dans l'onglet Groupes →",
                fontSize = 18.sp,
                color = Gold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
