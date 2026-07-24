package com.dispo.app.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Place
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dispo.app.core.ChatMessage
import com.dispo.app.core.CrewGroup
import com.dispo.app.core.DispoUiState
import com.dispo.app.core.Friend
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
import kotlinx.coroutines.delay

private val BubbleMe = RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
private val BubbleOther = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
private val timeFormat = SimpleDateFormat("HH:mm", Locale.FRANCE)
private val ChipShape = RoundedCornerShape(999.dp)
private val PanelShape = RoundedCornerShape(16.dp)

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
    var selectedGroupId by remember { mutableStateOf(state.groups.firstOrNull()?.id) }
    var manageOpen by remember { mutableStateOf(state.groups.isEmpty()) }

    LaunchedEffect(state.groups) {
        if (selectedGroupId == null || state.groups.none { it.id == selectedGroupId }) {
            selectedGroupId = state.groups.firstOrNull()?.id
        }
    }
    LaunchedEffect(state.addFriendFeedback) {
        if (state.addFriendFeedback != null) {
            delay(2200)
            onClearFeedback()
        }
    }

    val selectedGroup = state.groups.find { it.id == selectedGroupId }

    Column(modifier = modifier.fillMaxSize()) {
        GroupsHeader(
            groups = state.groups,
            selectedGroupId = selectedGroupId,
            manageOpen = manageOpen,
            onSelect = {
                selectedGroupId = it
                manageOpen = false
            },
            onToggleManage = { manageOpen = !manageOpen },
        )

        when {
            manageOpen -> GroupsManager(
                state = state,
                selectedGroup = selectedGroup,
                onCreateGroup = onCreateGroup,
                onJoinGroup = onJoinGroup,
                onSelectGroup = {
                    selectedGroupId = it
                    manageOpen = false
                },
                onAddFriendToGroup = onAddFriendToGroup,
                onLeaveGroup = onLeaveGroup,
                modifier = Modifier.weight(1f).fillMaxWidth(),
            )
            !state.chatUnlocked -> LockedChat(Modifier = Modifier.weight(1f).fillMaxWidth())
            else -> ChatThread(
                state = state,
                onSend = onSend,
                onOpenMap = onOpenMap,
                modifier = Modifier.weight(1f).fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun GroupsHeader(
    groups: List<CrewGroup>,
    selectedGroupId: String?,
    manageOpen: Boolean,
    onSelect: (String) -> Unit,
    onToggleManage: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        groups.forEach { group ->
            val selected = group.id == selectedGroupId && !manageOpen
            Box(
                modifier = Modifier
                    .clip(ChipShape)
                    .background(if (selected) SunYellow else Color.White)
                    .border(2.dp, InkBrown, ChipShape)
                    .clickable { onSelect(group.id) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Text(
                    group.name,
                    fontWeight = FontWeight.Bold,
                    color = InkBrown,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        IconButton(
            onClick = onToggleManage,
            modifier = Modifier
                .size(40.dp)
                .background(if (manageOpen) DispoGreen else InkBrown, CircleShape)
                .border(2.dp, InkBrown, CircleShape),
        ) {
            Icon(
                imageVector = if (manageOpen) Icons.Filled.Close else Icons.Filled.GroupAdd,
                contentDescription = if (manageOpen) "Fermer" else "Groupes",
                tint = Cream,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun GroupsManager(
    state: DispoUiState,
    selectedGroup: CrewGroup?,
    onCreateGroup: (String) -> Unit,
    onJoinGroup: (String) -> Unit,
    onSelectGroup: (String) -> Unit,
    onAddFriendToGroup: (groupId: String, friendId: String) -> Unit,
    onLeaveGroup: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var groupNameDraft by remember { mutableStateOf("") }
    var joinCodeDraft by remember { mutableStateOf("") }
    var expandedId by remember { mutableStateOf(selectedGroup?.id) }

    LazyColumn(
        modifier = modifier.padding(horizontal = 12.dp),
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, PanelShape)
                    .border(2.dp, InkBrown, PanelShape)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = groupNameDraft,
                        onValueChange = { groupNameDraft = it.take(64) },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Nom") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = chatFieldColors(),
                    )
                    IconButton(
                        onClick = {
                            onCreateGroup(groupNameDraft)
                            groupNameDraft = ""
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(InkBrown, CircleShape),
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Créer", tint = Cream)
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = joinCodeDraft,
                        onValueChange = {
                            joinCodeDraft = it.uppercase().filter { c -> c.isLetterOrDigit() }.take(12)
                        },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Code") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = chatFieldColors(),
                    )
                    IconButton(
                        onClick = {
                            onJoinGroup(joinCodeDraft)
                            joinCodeDraft = ""
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(DispoGreen, CircleShape),
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = "Rejoindre", tint = Color.White)
                    }
                }
                state.addFriendFeedback?.let { msg ->
                    Text(msg, color = DispoGreen, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        items(state.groups, key = { it.id }) { group ->
            GroupCard(
                group = group,
                friends = state.friends,
                myId = state.profile.id,
                expanded = expandedId == group.id,
                onToggle = { expandedId = if (expandedId == group.id) null else group.id },
                onOpenChat = { onSelectGroup(group.id) },
                onCopyCode = {
                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(ClipData.newPlainText("code", group.inviteCode))
                },
                onAddFriend = { friendId -> onAddFriendToGroup(group.id, friendId) },
                onLeave = { onLeaveGroup(group.id) },
            )
        }
    }
}

@Composable
private fun GroupCard(
    group: CrewGroup,
    friends: List<Friend>,
    myId: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    onOpenChat: () -> Unit,
    onCopyCode: () -> Unit,
    onAddFriend: (String) -> Unit,
    onLeave: () -> Unit,
) {
    val addable = friends.filter { it.id !in group.memberIds }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(PanelShape)
            .background(Color.White)
            .border(2.dp, InkBrown, PanelShape)
            .clickable(onClick = onToggle)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(Icons.Filled.Group, contentDescription = null, tint = CircusRed, modifier = Modifier.size(20.dp))
            Text(
                group.name,
                fontWeight = FontWeight.Bold,
                color = InkBrown,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text("${group.memberIds.size}", color = InkBrown.copy(alpha = 0.5f), fontSize = 13.sp)
        }

        if (expanded) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = onOpenChat,
                    modifier = Modifier.size(40.dp).background(CircusRed, CircleShape),
                ) {
                    Icon(Icons.Filled.Check, contentDescription = "Ouvrir", tint = Cream, modifier = Modifier.size(18.dp))
                }
                IconButton(
                    onClick = onCopyCode,
                    modifier = Modifier.size(40.dp).background(SunYellow, CircleShape),
                ) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = "Copier", tint = InkBrown, modifier = Modifier.size(18.dp))
                }
                IconButton(
                    onClick = onLeave,
                    modifier = Modifier.size(40.dp).background(InkBrown.copy(alpha = 0.12f), CircleShape),
                ) {
                    Icon(Icons.Filled.ExitToApp, contentDescription = "Quitter", tint = CircusRed, modifier = Modifier.size(18.dp))
                }
            }

            Text(group.inviteCode, fontFamily = LedFamily, fontSize = 18.sp, color = InkBrown.copy(alpha = 0.7f))

            group.memberIds.forEach { id ->
                Text(
                    if (id == myId) "@$id ·" else "@$id",
                    fontSize = 13.sp,
                    color = InkBrown,
                )
            }

            addable.forEach { friend ->
                IconButton(
                    onClick = { onAddFriend(friend.id) },
                    modifier = Modifier.size(36.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.PersonAdd, contentDescription = "Ajouter", tint = InkBrown, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("@${friend.name}", color = InkBrown, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatThread(
    state: DispoUiState,
    onSend: (String) -> Unit,
    onOpenMap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) listState.scrollToItem(state.messages.size - 1)
    }

    Column(modifier = modifier) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 10.dp),
        ) {
            items(
                items = state.messages,
                key = { it.id },
                contentType = { if (it.authorId == state.profile.id) "me" else "other" },
            ) { msg ->
                MessageBubble(msg = msg, myId = state.profile.id, onOpenMap = onOpenMap)
            }
        }
        ChatInputBar(onSend = onSend, onOpenMap = onOpenMap)
    }
}

@Composable
private fun chatFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = CircusRed,
    unfocusedBorderColor = InkBrown.copy(alpha = 0.3f),
    focusedContainerColor = Cream,
    unfocusedContainerColor = Cream,
    cursorColor = CircusRed,
    focusedTextColor = InkBrown,
    unfocusedTextColor = InkBrown,
)

@Composable
private fun ChatInputBar(onSend: (String) -> Unit, onOpenMap: () -> Unit) {
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
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        OutlinedTextField(
            value = draft,
            onValueChange = { draft = it },
            modifier = Modifier.weight(1f),
            placeholder = { Text("…", color = InkBrown.copy(alpha = 0.4f)) },
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
        IconButton(
            onClick = onOpenMap,
            modifier = Modifier
                .size(48.dp)
                .background(CircusPurple, CircleShape)
                .border(BorderStroke(2.dp, InkBrown), CircleShape),
        ) {
            Icon(Icons.Filled.Place, contentDescription = "Carte", tint = Cream)
        }
        IconButton(
            onClick = sendDraft,
            modifier = Modifier
                .size(48.dp)
                .background(CircusRed, CircleShape)
                .border(BorderStroke(2.dp, InkBrown), CircleShape),
        ) {
            Icon(Icons.Filled.Check, contentDescription = "Envoyer", tint = Cream)
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
                .background(if (isMe) SunYellow else Color.White, shape)
                .border(2.dp, InkBrown, shape)
                .then(if (msg.hasLocation) Modifier.clickable(onClick = onOpenMap) else Modifier)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            if (!isMe) {
                Text(msg.authorName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CircusRed)
            }
            if (msg.hasLocation) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Place, contentDescription = null, tint = CircusPurple, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(msg.text, fontSize = 15.sp, color = InkBrown)
                }
            } else {
                Text(msg.text, fontSize = 15.sp, color = InkBrown)
            }
            Text(
                timeText,
                fontSize = 12.sp,
                fontFamily = LedFamily,
                color = InkBrown.copy(alpha = 0.5f),
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
            .size(34.dp)
            .background(color, CircleShape)
            .border(2.dp, InkBrown, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            name.first().uppercase(),
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 16.sp),
            color = Cream,
        )
    }
}

@Composable
private fun LockedChat(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Icon(
            Icons.Filled.Lock,
            contentDescription = null,
            tint = InkBrown.copy(alpha = 0.25f),
            modifier = Modifier.size(56.dp),
        )
    }
}
