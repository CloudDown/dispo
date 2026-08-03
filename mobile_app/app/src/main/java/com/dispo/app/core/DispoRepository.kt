package com.dispo.app.core

import android.content.Context
import android.net.Uri
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dispo.app.data.ApiClient
import com.dispo.app.data.ApiMappers
import com.dispo.app.data.DispoApi
import com.dispo.app.data.TokenStore
import com.dispo.app.data.apiErrorMessage
import com.dispo.app.data.dto.AddFriendRequest
import com.dispo.app.data.dto.AddMemberRequest
import com.dispo.app.data.dto.CreateGroupRequest
import com.dispo.app.data.dto.JoinGroupRequest
import com.dispo.app.data.dto.LoginRequest
import com.dispo.app.data.dto.ProfileUpdateRequest
import com.dispo.app.data.dto.RegisterRequest
import com.dispo.app.data.dto.SendMessageRequest
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private val Context.dataStore by preferencesDataStore(name = "dispo")

class DispoRepository private constructor(private val appContext: Context) {

    companion object {
        private val KEY_AVATAR_PATH = stringPreferencesKey("avatar_path")
        private val KEY_ACTIVE_GROUP_ID = stringPreferencesKey("active_group_id")
        @Volatile
        private var instance: DispoRepository? = null

        fun get(context: Context): DispoRepository =
            instance ?: synchronized(this) {
                instance ?: DispoRepository(context.applicationContext).also { instance = it }
            }

        fun normalizeHandle(raw: String): String =
            raw.trim()
                .removePrefix("@")
                .lowercase()
                .filter { it.isLetterOrDigit() || it == '.' || it == '_' }
                .take(30)
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val tokenStore = TokenStore(appContext)
    private val api: DispoApi = ApiClient.create(tokenStore)

    private val friendsFlow = MutableStateFlow<List<Friend>>(emptyList())
    private val groupsFlow = MutableStateFlow<List<CrewGroup>>(emptyList())
    private val messagesFlow = MutableStateFlow<List<ChatMessage>>(emptyList())
    private val feedbackFlow = MutableStateFlow<String?>(null)
    private val loginErrorFlow = MutableStateFlow<String?>(null)
    private val syncingFlow = MutableStateFlow(false)
    private val meDispoFlow = MutableStateFlow(false)
    private val groupDispoCountFlow = MutableStateFlow(0)
    private val profileFlow = MutableStateFlow(UserProfile(id = "", name = ""))
    private val activeGroupIdFlow = MutableStateFlow<String?>(null)

    private var pollingJob: Job? = null

    val isLoggedIn = tokenStore.isLoggedIn.stateIn(scope, SharingStarted.Eagerly, false)

    val uiState = combine(
        combine(meDispoFlow, profileFlow, friendsFlow, groupsFlow) { me, profile, friends, groups ->
            CoreSlice(me, profile, friends, groups)
        },
        combine(messagesFlow, feedbackFlow, loginErrorFlow, syncingFlow) { messages, feedback, loginErr, syncing ->
            UiSlice(messages, feedback, loginErr, syncing)
        },
        combine(isLoggedIn, activeGroupIdFlow, groupDispoCountFlow) { loggedIn, activeGroup, groupDispo ->
            AuthSlice(loggedIn, activeGroup, groupDispo)
        },
    ) { core, ui, auth ->
        DispoUiState(
            meDispo = core.meDispo,
            profile = core.profile,
            friends = core.friends,
            groups = core.groups,
            messages = ui.messages,
            addFriendFeedback = ui.feedback ?: ui.loginError,
            isLoggedIn = auth.loggedIn,
            isSyncing = ui.syncing,
            activeGroupId = auth.activeGroupId,
            groupDispoCount = auth.groupDispoCount,
        )
    }.stateIn(scope, SharingStarted.Eagerly, DispoUiState())

    /** État dispo pour le widget (lecture seule). */
    val meDispo get() = meDispoFlow.value

    init {
        scope.launch {
            tokenStore.loadIntoMemory()
            activeGroupIdFlow.value = appContext.dataStore.data.first()[KEY_ACTIVE_GROUP_ID]
            profileFlow.value = profileFlow.value.copy(
                avatarPath = appContext.dataStore.data.first()[KEY_AVATAR_PATH],
            )
            if (tokenStore.bearerToken != null) {
                runCatching { refreshAll() }
            }
        }
    }

    fun startPolling(intervalMs: Long = 5_000L) {
        pollingJob?.cancel()
        pollingJob = scope.launch {
            while (isActive) {
                if (tokenStore.bearerToken != null) {
                    runCatching { refreshAll(silent = true) }
                }
                delay(intervalMs)
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    suspend fun login(publicId: String, password: String): Boolean {
        syncingFlow.value = true
        loginErrorFlow.value = null
        return try {
            val handle = normalizeHandle(publicId)
            val token = api.login(LoginRequest(publicId = handle, password = password))
            tokenStore.save(token)
            refreshAll(silent = true)
            startPolling()
            feedbackFlow.value = "Connecté en @${token.publicId}"
            true
        } catch (t: Throwable) {
            val msg = apiErrorMessage(t)
            loginErrorFlow.value = msg
            false
        } finally {
            syncingFlow.value = false
        }
    }

    suspend fun register(displayName: String, publicId: String, password: String): Boolean {
        syncingFlow.value = true
        loginErrorFlow.value = null
        return try {
            val name = displayName.trim()
            val handle = normalizeHandle(publicId).ifBlank { null }
            val token = api.register(
                RegisterRequest(
                    displayName = name,
                    password = password,
                    publicId = handle,
                ),
            )
            tokenStore.save(token)
            refreshAll(silent = true)
            startPolling()
            feedbackFlow.value = "Compte @${token.publicId} créé"
            true
        } catch (t: Throwable) {
            val msg = apiErrorMessage(t)
            loginErrorFlow.value = msg
            false
        } finally {
            syncingFlow.value = false
        }
    }

    suspend fun logout() {
        stopPolling()
        tokenStore.clear()
        friendsFlow.value = emptyList()
        groupsFlow.value = emptyList()
        messagesFlow.value = emptyList()
        meDispoFlow.value = false
        groupDispoCountFlow.value = 0
        profileFlow.value = UserProfile(id = "", name = "")
        activeGroupIdFlow.value = null
    }

    suspend fun refreshAll(silent: Boolean = false) {
        if (tokenStore.bearerToken == null) return
        if (!silent) syncingFlow.value = true
        try {
            val me = api.me()
            val avatarPath = appContext.dataStore.data.first()[KEY_AVATAR_PATH]
            profileFlow.value = ApiMappers.toProfile(me, avatarPath)

            friendsFlow.value = api.listFriends().map(ApiMappers::toFriend)
            groupsFlow.value = api.listGroups().map(ApiMappers::toGroup)

            ensureActiveGroup()
            refreshAvailability(silent = true)
            refreshMessages(silent = true)
        } finally {
            if (!silent) syncingFlow.value = false
        }
    }

    private suspend fun ensureActiveGroup() {
        val groups = groupsFlow.value
        if (groups.isEmpty()) {
            activeGroupIdFlow.value = null
            appContext.dataStore.edit { it.remove(KEY_ACTIVE_GROUP_ID) }
            return
        }
        val current = activeGroupIdFlow.value
        if (current == null || groups.none { it.id == current }) {
            val preferred = groups.find { it.inviteCode == "CREWDEMO" } ?: groups.first()
            setActiveGroup(preferred.id)
        }
    }

    suspend fun setActiveGroup(groupId: String) {
        activeGroupIdFlow.value = groupId
        appContext.dataStore.edit { it[KEY_ACTIVE_GROUP_ID] = groupId }
        refreshMessages(silent = true)
        refreshAvailability(silent = true)
    }

    private suspend fun refreshAvailability(silent: Boolean = false) {
        val gid = activeGroupIdFlow.value?.toIntOrNull()
        val resp = api.myAvailability(groupId = gid)
        meDispoFlow.value = resp.active
        groupDispoCountFlow.value = resp.dispoCountInGroup
    }

    private suspend fun refreshMessages(silent: Boolean = false) {
        val gid = activeGroupIdFlow.value?.toIntOrNull() ?: run {
            messagesFlow.value = emptyList()
            return
        }
        messagesFlow.value = api.listMessages(gid).map(ApiMappers::toMessage)
    }

    suspend fun toggleMeDispo(): Boolean = withApi {
        val gid = activeGroupIdFlow.value?.toIntOrNull()
        val resp = api.toggleAvailability(groupId = gid)
        meDispoFlow.value = resp.active
        groupDispoCountFlow.value = resp.dispoCountInGroup
        refreshAll(silent = true)
        resp.active
    } ?: meDispoFlow.value

    suspend fun updateDisplayName(name: String) = withApi {
        val trimmed = name.trim().take(64)
        if (trimmed.isBlank()) return@withApi
        val updated = api.updateMe(ProfileUpdateRequest(displayName = trimmed))
        val avatarPath = appContext.dataStore.data.first()[KEY_AVATAR_PATH]
        profileFlow.value = ApiMappers.toProfile(updated, avatarPath)
        feedbackFlow.value = "Pseudo mis à jour"
    }

    suspend fun setAvatarFromUri(uri: Uri) {
        val dest = File(appContext.filesDir, "avatar.jpg")
        appContext.contentResolver.openInputStream(uri)?.use { input ->
            dest.outputStream().use { output -> input.copyTo(output) }
        } ?: run {
            feedbackFlow.value = "Impossible de lire la photo"
            return
        }
        appContext.dataStore.edit { it[KEY_AVATAR_PATH] = dest.absolutePath }
        profileFlow.value = profileFlow.value.copy(avatarPath = dest.absolutePath)
        feedbackFlow.value = "Photo mise à jour"
    }

    suspend fun addFriendById(rawId: String): Boolean {
        val handle = normalizeHandle(rawId)
        if (handle.isBlank()) {
            feedbackFlow.value = "Entre un @pseudo"
            return false
        }
        return withApi {
            api.addFriend(AddFriendRequest(publicId = handle))
            refreshAll(silent = true)
            feedbackFlow.value = "@$handle ajouté·e !"
            true
        } ?: false
    }

    suspend fun removeFriend(friendId: String) = withApi {
        api.removeFriend(normalizeHandle(friendId))
        refreshAll(silent = true)
        feedbackFlow.value = "Retiré des amis"
    }

    suspend fun createGroup(): String = withApi {
        val group = api.createGroup(CreateGroupRequest(name = "Nouveau groupe"))
        refreshAll(silent = true)
        setActiveGroup(group.id.toString())
        feedbackFlow.value = "Groupe créé"
        group.id.toString()
    } ?: ""

    suspend fun renameGroup(groupId: String, rawName: String): Boolean {
        val name = rawName.trim().take(64)
        if (name.isBlank()) {
            feedbackFlow.value = "Nom du groupe requis"
            return false
        }
        groupsFlow.value = groupsFlow.value.map {
            if (it.id == groupId) it.copy(name = name) else it
        }
        feedbackFlow.value = "Groupe renommé (local — pas encore sur le serveur)"
        return true
    }

    suspend fun joinGroupByCode(rawCode: String): Boolean {
        val code = rawCode.trim().uppercase().filter { it.isLetterOrDigit() }.take(12)
        if (code.length < 4) {
            feedbackFlow.value = "Code invalide"
            return false
        }
        return withApi {
            val group = api.joinGroup(JoinGroupRequest(inviteCode = code))
            refreshAll(silent = true)
            setActiveGroup(group.id.toString())
            feedbackFlow.value = "Bienvenue dans « ${group.name} »"
            true
        } ?: false
    }

    suspend fun addFriendToGroup(groupId: String, friendId: String): Boolean {
        val gid = groupId.toIntOrNull() ?: return false
        return withApi {
            api.addGroupMember(gid, AddMemberRequest(publicId = normalizeHandle(friendId)))
            refreshAll(silent = true)
            feedbackFlow.value = "@$friendId ajouté·e au groupe"
            true
        } ?: false
    }

    suspend fun leaveGroup(groupId: String) = withApi {
        val gid = groupId.toIntOrNull() ?: return@withApi
        api.leaveGroup(gid)
        if (activeGroupIdFlow.value == groupId) {
            activeGroupIdFlow.value = null
            appContext.dataStore.edit { it.remove(KEY_ACTIVE_GROUP_ID) }
        }
        refreshAll(silent = true)
        feedbackFlow.value = "Tu as quitté le groupe"
    }

    fun sendMessage(text: String, lat: Double? = null, lon: Double? = null) {
        scope.launch {
            withApi {
                val gid = activeGroupIdFlow.value?.toIntOrNull()
                    ?: run {
                        feedbackFlow.value = "Rejoins ou crée un groupe d'abord"
                        return@withApi
                    }
                api.sendMessage(
                    gid,
                    SendMessageRequest(text = text, lat = lat, lon = lon),
                )
                refreshMessages(silent = true)
                refreshAvailability(silent = true)
            }
        }
    }

    fun clearFeedback() {
        feedbackFlow.value = null
        loginErrorFlow.value = null
    }

    /** Compat widget : sync depuis le serveur au lieu de reset local. */
    suspend fun resetDispoOnLaunch() {
        if (tokenStore.bearerToken != null) {
            runCatching { refreshAll(silent = true) }
        }
    }

    private suspend fun <T> withApi(block: suspend () -> T): T? {
        return try {
            block()
        } catch (t: Throwable) {
            val msg = apiErrorMessage(t)
            loginErrorFlow.value = msg
            feedbackFlow.value = msg
            null
        }
    }

    private data class CoreSlice(
        val meDispo: Boolean,
        val profile: UserProfile,
        val friends: List<Friend>,
        val groups: List<CrewGroup>,
    )

    private data class UiSlice(
        val messages: List<ChatMessage>,
        val feedback: String?,
        val loginError: String?,
        val syncing: Boolean,
    )

    private data class AuthSlice(
        val loggedIn: Boolean,
        val activeGroupId: String?,
        val groupDispoCount: Int,
    )
}
