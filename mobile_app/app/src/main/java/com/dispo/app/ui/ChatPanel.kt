package com.dispo.app.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dispo.app.core.ChatMessage
import com.dispo.app.core.CrewGroup
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
import com.dispo.app.ui.theme.SunYellow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

private enum class ChatSection { Messages, Groupes }

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
private val CardShape = RoundedCornerShape(18.dp)

private val timeFormat = SimpleDateFormat("HH:mm", Locale.FRANCE)

@Composable
fun ChatPanel(
    state: DispoUiState,
    onSend: (String) -> Unit,
    onOpenMap: () -> Unit,
    onCreateGroup: (String) -> Unit,
    onJoinGroup: (String) -> Unit,
    onAddFriendToGroup: (groupId: String, friendId: String) -> Unit,
    onLeaveGroup: (String) -> Unit,
    onClearFeedback: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var section by remember { mutableStateOf(ChatSection.Messages) }
    var expandedGroupId by remember { mutableStateOf<String?>(null) }
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CircusRed, RoundedCornerShape(18.dp))
                .border(3.dp, InkBrown, RoundedCornerShape(18.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    "LE CHAT DU SOIR",
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                    color = Gold,
                )
                Text(
                    if (state.chatUnlocked) "🎪 Tout le monde est là" else "🎪 En attente d'une dispo",
                    fontFamily = LedFamily,
                    fontSize = 18.sp,
                    color = Cream.copy(alpha = 0.9f),
                )
            }
            Box(
                modifier = Modifier
                    .background(Cream, RoundedCornerShape(12.dp))
                    .border(2.dp, InkBrown, RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Text(
                    "${state.dispoCount} dispos",
                    fontFamily = LedFamily,
                    fontSize = 16.sp,
                    color = InkBrown,
                )
            }
        }

        Spacer(Modifier.height(10.dp))

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
                onCreateGroup = { name ->
                    onCreateGroup(name)
                    section = ChatSection.Groupes
                },
                onJoinGroup = onJoinGroup,
                onCopyCode = { code ->
                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(ClipData.newPlainText("code", code))
                },
                onAddFriendToGroup = onAddFriendToGroup,
                onLeaveGroup = onLeaveGroup,
            )
        }
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
    val bg = if (selected) CircusRed else Color.White
    val textColor = if (selected) Cream else InkBrown
    Row(
        modifier = modifier
            .background(bg, RoundedCornerShape(14.dp))
            .border(
                width = if (selected) 2.5.dp else 2.dp,
                color = if (selected) InkBrown else InkBrown.copy(alpha = 0.2f),
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
                Text(badge, fontSize = 12.sp, color = InkBrown, fontWeight = FontWeight.Bold)
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
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(20.dp))
                .border(2.5.dp, InkBrown.copy(alpha = 0.18f), RoundedCornerShape(20.dp)),
        ) {
            if (state.messages.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text("💬", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    LedCaption(
                        text = "Lance la conversation",
                        fontSize = 22.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
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
            }
        }

        Spacer(Modifier.height(8.dp))
        ChatInputBar(onSend = onSend, onOpenMap = onOpenMap)
    }
}

@Composable
private fun ChatGroupsSection(
    state: DispoUiState,
    expandedGroupId: String?,
    onExpandGroup: (String) -> Unit,
    onCreateGroup: (String) -> Unit,
    onJoinGroup: (String) -> Unit,
    onCopyCode: (String) -> Unit,
    onAddFriendToGroup: (String, String) -> Unit,
    onLeaveGroup: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var groupNameDraft by remember { mutableStateOf("") }
    var joinCodeDraft by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 12.dp),
    ) {
        item {
            GroupActionCard(
                step = "1",
                title = "Créer un groupe",
                hint = "Donne un nom à ton crew du soir",
            ) {
                ChatGroupField(
                    value = groupNameDraft,
                    onValueChange = { groupNameDraft = it.take(64) },
                    placeholder = "Ex : Les copains du vendredi",
                    onDone = {
                        if (groupNameDraft.isNotBlank()) {
                            onCreateGroup(groupNameDraft)
                            groupNameDraft = ""
                        }
                    },
                )
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = {
                        onCreateGroup(groupNameDraft)
                        groupNameDraft = ""
                    },
                    enabled = groupNameDraft.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DispoGreen,
                        disabledContainerColor = InkBrown.copy(alpha = 0.12f),
                    ),
                    border = BorderStroke(2.5.dp, InkBrown.copy(alpha = 0.25f)),
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = Cream)
                    Spacer(Modifier.width(8.dp))
                    Text("Créer le groupe", fontFamily = LedFamily, fontSize = 20.sp, color = Cream)
                }
            }
        }

        item {
            GroupActionCard(
                step = "2",
                title = "Rejoindre un groupe",
                hint = "Entre le code reçu d'un pote",
            ) {
                ChatGroupField(
                    value = joinCodeDraft,
                    onValueChange = {
                        joinCodeDraft = it.uppercase().filter { c -> c.isLetterOrDigit() }.take(12)
                    },
                    placeholder = "CODE INVITATION",
                    onDone = {
                        if (joinCodeDraft.isNotBlank()) {
                            onJoinGroup(joinCodeDraft)
                            joinCodeDraft = ""
                        }
                    },
                )
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = {
                        onJoinGroup(joinCodeDraft)
                        joinCodeDraft = ""
                    },
                    enabled = joinCodeDraft.length >= 4,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Gold,
                        disabledContainerColor = InkBrown.copy(alpha = 0.12f),
                    ),
                    border = BorderStroke(2.5.dp, InkBrown.copy(alpha = 0.25f)),
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = InkBrown)
                    Spacer(Modifier.width(8.dp))
                    Text("Rejoindre", fontFamily = LedFamily, fontSize = 20.sp, color = InkBrown)
                }
            }
        }

        state.addFriendFeedback?.let { msg ->
            item {
                Text(
                    msg,
                    color = DispoGreen,
                    fontFamily = LedFamily,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
        }

        item {
            Text(
                "Tes groupes (${state.groups.size})",
                fontFamily = BangersFamily,
                fontSize = 22.sp,
                color = GoldDark,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        if (state.groups.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, CardShape)
                        .border(2.dp, InkBrown.copy(alpha = 0.12f), CardShape)
                        .padding(20.dp),
                ) {
                    LedCaption(
                        text = "Aucun groupe pour l'instant — crée-en un ci-dessus",
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        } else {
            items(state.groups, key = { it.id }) { group ->
                GroupRow(
                    group = group,
                    friends = state.friends,
                    myId = state.profile.id,
                    expanded = expandedGroupId == group.id,
                    onToggle = { onExpandGroup(group.id) },
                    onCopyCode = { onCopyCode(group.inviteCode) },
                    onAddFriend = { onAddFriendToGroup(group.id, it) },
                    onLeave = { onLeaveGroup(group.id) },
                )
            }
        }
    }
}

@Composable
private fun GroupActionCard(
    step: String,
    title: String,
    hint: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, CardShape)
            .border(2.5.dp, InkBrown.copy(alpha = 0.15f), CardShape)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(Gold.copy(alpha = 0.35f), CircleShape)
                    .border(2.dp, GoldDark.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(step, fontFamily = BangersFamily, fontSize = 16.sp, color = InkBrown)
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(title, fontFamily = BangersFamily, fontSize = 20.sp, color = InkBrown)
                LedCaption(text = hint, fontSize = 17.sp)
            }
        }
        Spacer(Modifier.height(14.dp))
        content()
    }
}

@Composable
private fun ChatGroupField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    onDone: () -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = InkBrown.copy(alpha = 0.4f), fontSize = 15.sp) },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GoldDark,
            unfocusedBorderColor = InkBrown.copy(alpha = 0.2f),
            focusedContainerColor = Cream,
            unfocusedContainerColor = Cream,
            cursorColor = CircusRed,
            focusedTextColor = InkBrown,
            unfocusedTextColor = InkBrown,
        ),
    )
}

@Composable
private fun GroupRow(
    group: CrewGroup,
    friends: List<Friend>,
    myId: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    onCopyCode: () -> Unit,
    onAddFriend: (String) -> Unit,
    onLeave: () -> Unit,
) {
    val addable = friends.filter { it.id !in group.memberIds }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, CardShape)
            .border(2.5.dp, InkBrown.copy(alpha = 0.15f), CardShape)
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
                Text(group.name, color = InkBrown, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                Text(
                    "${group.memberIds.size} membre${if (group.memberIds.size > 1) "s" else ""}",
                    fontFamily = LedFamily,
                    fontSize = 16.sp,
                    color = InkBrown.copy(alpha = 0.5f),
                )
            }
            Text(
                if (expanded) "▲" else "▼",
                color = InkBrown.copy(alpha = 0.35f),
                fontSize = 12.sp,
            )
        }
        if (expanded) {
            HorizontalDivider(color = InkBrown.copy(alpha = 0.08f))
            LedCaption(text = "Code à partager", fontSize = 16.sp, color = InkBrown.copy(alpha = 0.5f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Cream, RoundedCornerShape(12.dp))
                    .border(2.dp, InkBrown.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(group.inviteCode, fontFamily = LedFamily, color = InkBrown, fontSize = 22.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onCopyCode, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Filled.ContentCopy,
                            contentDescription = "Copier le code",
                            tint = GoldDark,
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
            LedCaption(text = "Membres", fontSize = 16.sp, color = InkBrown.copy(alpha = 0.5f))
            group.memberIds.forEach { id ->
                Text(
                    if (id == myId) "@$id · toi" else "@$id",
                    fontFamily = LedFamily,
                    fontSize = 17.sp,
                    color = InkBrown.copy(alpha = 0.75f),
                )
            }
            if (addable.isNotEmpty()) {
                LedCaption(text = "Ajouter un ami", fontSize = 16.sp, color = InkBrown.copy(alpha = 0.5f))
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
                            color = InkBrown,
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
            .background(Color.White, RoundedCornerShape(28.dp))
            .border(2.5.dp, InkBrown.copy(alpha = 0.2f), RoundedCornerShape(28.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = draft,
            onValueChange = { draft = it },
            modifier = Modifier.weight(1f),
            placeholder = { Text("Écris un message…", color = InkBrown.copy(alpha = 0.45f)) },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { sendDraft() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                cursorColor = CircusRed,
                focusedTextColor = InkBrown,
                unfocusedTextColor = InkBrown,
            ),
        )
        IconButton(
            onClick = sendDraft,
            enabled = draft.isNotBlank(),
            modifier = Modifier
                .size(44.dp)
                .background(
                    if (draft.isNotBlank()) DispoGreen else InkBrown.copy(alpha = 0.12f),
                    CircleShape,
                ),
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Send,
                contentDescription = "Envoyer",
                tint = if (draft.isNotBlank()) Cream else InkBrown.copy(alpha = 0.35f),
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(Modifier.width(4.dp))
        Button(
            onClick = onOpenMap,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = CircusPurple),
            border = BorderStroke(2.5.dp, InkBrown),
            modifier = Modifier.size(44.dp),
            contentPadding = PaddingValues(0.dp),
        ) {
            Text("📍", fontSize = 18.sp)
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
                .background(if (isMe) SunYellow else Cream, shape)
                .border(2.5.dp, InkBrown.copy(alpha = 0.35f), shape)
                .then(
                    if (msg.hasLocation) Modifier.clickable(onClick = onOpenMap) else Modifier,
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
private fun LockedChat(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(24.dp))
                .border(3.dp, InkBrown.copy(alpha = 0.25f), RoundedCornerShape(24.dp))
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
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))
            LedCaption(
                text = "En attendant, crée ton crew dans l'onglet Groupes →",
                fontSize = 18.sp,
                color = GoldDark,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
