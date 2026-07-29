package com.dispo.app.core

import android.content.Context
import android.net.Uri
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.File
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import kotlin.random.Random
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore by preferencesDataStore(name = "dispo")

/**
 * Source de vérité locale (MVP sans backend).
 * Le pseudo (= nom Insta) sert d'identifiant. Léa reste la seule démo auto.
 */
class DispoRepository private constructor(private val appContext: Context) {

    companion object {
        private val KEY_ACTIVE = booleanPreferencesKey("me_dispo")
        private val KEY_EXPIRES_AT = longPreferencesKey("me_dispo_expires_at")
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_AVATAR_COLOR = intPreferencesKey("avatar_color")
        private val KEY_AVATAR_PATH = stringPreferencesKey("avatar_path")
        private val KEY_FRIENDS_JSON = stringPreferencesKey("friends_json")
        private val KEY_GROUPS_JSON = stringPreferencesKey("groups_json")

        private val CODE_ALPHABET = ('A'..'Z') + ('0'..'9')

        /** Annuaire démo : pseudos style Insta. */
        private val DIRECTORY = mapOf(
            "lea" to Friend(id = "lea", name = "lea", avatarColor = 1),
            "max" to Friend(id = "max", name = "max", avatarColor = 2),
            "sam" to Friend(id = "sam", name = "sam", avatarColor = 3),
        )

        @Volatile
        private var instance: DispoRepository? = null

        fun get(context: Context): DispoRepository =
            instance ?: synchronized(this) {
                instance ?: DispoRepository(context.applicationContext).also { instance = it }
            }

        /** Normalise un pseudo Insta : sans @, minuscules, caractères simples. */
        fun normalizeHandle(raw: String): String =
            raw.trim()
                .removePrefix("@")
                .lowercase()
                .filter { it.isLetterOrDigit() || it == '.' || it == '_' }
                .take(30)
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val friendsFlow = MutableStateFlow<List<Friend>>(emptyList())
    private val groupsFlow = MutableStateFlow<List<CrewGroup>>(emptyList())
    private val messagesFlow = MutableStateFlow<List<ChatMessage>>(emptyList())
    private val feedbackFlow = MutableStateFlow<String?>(null)
    private var nextMessageId = 1L
    private var demoReplySent = false

    val meDispoFlow = appContext.dataStore.data.map { prefs ->
        val active = prefs[KEY_ACTIVE] ?: false
        val expiresAt = prefs[KEY_EXPIRES_AT] ?: 0L
        active && System.currentTimeMillis() < expiresAt
    }

    val profileFlow = appContext.dataStore.data.map { prefs ->
        UserProfile(
            id = prefs[KEY_USER_ID] ?: "———",
            name = prefs[KEY_USER_NAME] ?: "Toi",
            avatarColor = prefs[KEY_AVATAR_COLOR] ?: 0,
            avatarPath = prefs[KEY_AVATAR_PATH],
        )
    }

    val uiState = combine(
        combine(meDispoFlow, profileFlow, friendsFlow, groupsFlow) { me, profile, friends, groups ->
            Quad(me, profile, friends, groups)
        },
        combine(messagesFlow, feedbackFlow) { messages, feedback -> messages to feedback },
    ) { quad, msgFb ->
        DispoUiState(
            meDispo = quad.a,
            profile = quad.b,
            friends = quad.c,
            groups = quad.d,
            messages = msgFb.first,
            addFriendFeedback = msgFb.second,
        )
    }.stateIn(scope, SharingStarted.Eagerly, DispoUiState())

    init {
        scope.launch {
            ensureProfile()
            loadFriends()
            loadGroups()
        }
    }

    private suspend fun ensureProfile() {
        val prefs = appContext.dataStore.data.first()
        if (prefs[KEY_USER_NAME].isNullOrBlank()) {
            appContext.dataStore.edit {
                it[KEY_USER_NAME] = "toi"
                it[KEY_USER_ID] = "toi"
                it[KEY_AVATAR_COLOR] = Random.nextInt(0, 6)
            }
        } else if (prefs[KEY_USER_ID].isNullOrBlank()) {
            val name = prefs[KEY_USER_NAME]!!
            appContext.dataStore.edit {
                it[KEY_USER_ID] = normalizeHandle(name).ifBlank { "toi" }
            }
        }
    }

    private suspend fun loadFriends() {
        val json = appContext.dataStore.data.first()[KEY_FRIENDS_JSON]
        friendsFlow.value = if (json.isNullOrBlank()) emptyList() else decodeFriends(json)
    }

    private suspend fun persistFriends(friends: List<Friend>) {
        friendsFlow.value = friends
        appContext.dataStore.edit { it[KEY_FRIENDS_JSON] = encodeFriends(friends) }
    }

    private suspend fun loadGroups() {
        val json = appContext.dataStore.data.first()[KEY_GROUPS_JSON]
        groupsFlow.value = if (json.isNullOrBlank()) emptyList() else decodeGroups(json)
    }

    private suspend fun persistGroups(groups: List<CrewGroup>) {
        groupsFlow.value = groups
        appContext.dataStore.edit { it[KEY_GROUPS_JSON] = encodeGroups(groups) }
    }

    private fun endOfDayMillis(): Long =
        LocalDate.now()
            .plusDays(1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

    suspend fun resetDispoOnLaunch() {
        appContext.dataStore.edit { prefs ->
            prefs[KEY_ACTIVE] = false
            prefs[KEY_EXPIRES_AT] = 0L
        }
        friendsFlow.value = friendsFlow.value.map { it.copy(dispo = false) }
        demoReplySent = false
    }

    suspend fun toggleMeDispo(): Boolean {
        val currentlyDispo = meDispoFlow.first()
        val newValue = !currentlyDispo
        appContext.dataStore.edit { prefs ->
            prefs[KEY_ACTIVE] = newValue
            prefs[KEY_EXPIRES_AT] = if (newValue) endOfDayMillis() else 0L
        }
        if (newValue) simulateFriendReply()
        return newValue
    }

    suspend fun updateDisplayName(name: String) {
        val handle = normalizeHandle(name)
        if (handle.isBlank()) return
        val oldId = profileFlow.first().id
        appContext.dataStore.edit {
            it[KEY_USER_NAME] = handle
            it[KEY_USER_ID] = handle
        }
        if (oldId != handle) {
            persistGroups(
                groupsFlow.value.map { g ->
                    g.copy(memberIds = g.memberIds.map { if (it == oldId) handle else it })
                },
            )
        }
    }

    suspend fun cycleAvatarColor() {
        val current = appContext.dataStore.data.first()[KEY_AVATAR_COLOR] ?: 0
        appContext.dataStore.edit { it[KEY_AVATAR_COLOR] = (current + 1) % 6 }
    }

    /** Copie la photo choisie dans le stockage privé de l'app. */
    suspend fun setAvatarFromUri(uri: Uri) {
        val dest = File(appContext.filesDir, "avatar.jpg")
        appContext.contentResolver.openInputStream(uri)?.use { input ->
            dest.outputStream().use { output -> input.copyTo(output) }
        } ?: run {
            feedbackFlow.value = "Impossible de lire la photo"
            return
        }
        appContext.dataStore.edit { it[KEY_AVATAR_PATH] = dest.absolutePath }
        feedbackFlow.value = "Photo mise à jour"
    }

    suspend fun clearAvatar() {
        val path = appContext.dataStore.data.first()[KEY_AVATAR_PATH]
        if (!path.isNullOrBlank()) File(path).delete()
        appContext.dataStore.edit { it.remove(KEY_AVATAR_PATH) }
    }

    /**
     * Ajoute un ami par pseudo (même idée que le @ Instagram).
     * Démo : lea / max / sam.
     */
    suspend fun addFriendById(rawId: String): Boolean {
        val handle = normalizeHandle(rawId)
        if (handle.isBlank()) {
            feedbackFlow.value = "Entre un nom"
            return false
        }
        val myHandle = normalizeHandle(profileFlow.first().name)
        if (handle == myHandle) {
            feedbackFlow.value = "C'est ton propre nom"
            return false
        }
        if (friendsFlow.value.any { it.id.equals(handle, ignoreCase = true) }) {
            feedbackFlow.value = "Déjà dans tes amis"
            return false
        }

        val friend = DIRECTORY[handle] ?: Friend(
            id = handle,
            name = handle,
            avatarColor = handle.hashCode().and(0x7FFFFFFF) % 6,
        )
        persistFriends(friendsFlow.value + friend)
        feedbackFlow.value = "@${friend.name} ajouté·e !"
        return true
    }

    suspend fun removeFriend(friendId: String) {
        persistFriends(friendsFlow.value.filterNot { it.id == friendId })
        // Retire aussi des groupes
        persistGroups(
            groupsFlow.value.map { g ->
                g.copy(memberIds = g.memberIds.filterNot { it == friendId })
            },
        )
        feedbackFlow.value = "Retiré des amis"
    }

    /** Crée un groupe avec un nom par défaut. Retourne l'id pour permettre le renommage. */
    suspend fun createGroup(): String {
        val me = normalizeHandle(profileFlow.first().name).ifBlank { "toi" }
        val group = CrewGroup(
            id = UUID.randomUUID().toString(),
            name = "Nouveau groupe",
            inviteCode = generateInviteCode(),
            memberIds = listOf(me),
        )
        persistGroups(groupsFlow.value + group)
        feedbackFlow.value = "Groupe créé"
        return group.id
    }

    suspend fun renameGroup(groupId: String, rawName: String): Boolean {
        val name = rawName.trim().take(64)
        if (name.isBlank()) {
            feedbackFlow.value = "Nom du groupe requis"
            return false
        }
        if (groupsFlow.value.none { it.id == groupId }) return false
        persistGroups(
            groupsFlow.value.map {
                if (it.id == groupId) it.copy(name = name) else it
            },
        )
        feedbackFlow.value = "Groupe renommé « $name »"
        return true
    }

    suspend fun joinGroupByCode(rawCode: String): Boolean {
        val code = rawCode.trim().uppercase().filter { it.isLetterOrDigit() }.take(12)
        if (code.length < 4) {
            feedbackFlow.value = "Code invalide"
            return false
        }
        val group = groupsFlow.value.find { it.inviteCode == code }
        if (group == null) {
            // Démo locale : pas de serveur — on ne peut rejoindre que des codes déjà connus
            feedbackFlow.value = "Code inconnu (sur cet appareil)"
            return false
        }
        val me = normalizeHandle(profileFlow.first().name).ifBlank { "toi" }
        if (group.memberIds.contains(me)) {
            feedbackFlow.value = "Tu es déjà dans ce groupe"
            return false
        }
        persistGroups(
            groupsFlow.value.map {
                if (it.id == group.id) it.copy(memberIds = it.memberIds + me) else it
            },
        )
        feedbackFlow.value = "Bienvenue dans « ${group.name} »"
        return true
    }

    /** Ajoute un ami déjà connu dans le groupe. */
    suspend fun addFriendToGroup(groupId: String, friendId: String): Boolean {
        val friend = friendsFlow.value.find { it.id == friendId }
        if (friend == null) {
            feedbackFlow.value = "Ajoute-le d'abord en ami"
            return false
        }
        val group = groupsFlow.value.find { it.id == groupId } ?: return false
        if (group.memberIds.contains(friendId)) {
            feedbackFlow.value = "Déjà dans le groupe"
            return false
        }
        persistGroups(
            groupsFlow.value.map {
                if (it.id == groupId) it.copy(memberIds = it.memberIds + friendId) else it
            },
        )
        feedbackFlow.value = "@${friend.name} ajouté·e au groupe"
        return true
    }

    suspend fun leaveGroup(groupId: String) {
        val me = normalizeHandle(profileFlow.first().name).ifBlank { "toi" }
        val next = groupsFlow.value.mapNotNull { g ->
            if (g.id != groupId) return@mapNotNull g
            val members = g.memberIds.filterNot { it == me }
            if (members.isEmpty()) null else g.copy(memberIds = members)
        }
        persistGroups(next)
        feedbackFlow.value = "Tu as quitté le groupe"
    }

    fun clearFeedback() {
        feedbackFlow.value = null
    }

    /** Démo : si lea est dans le crew, elle répond dispo après 5 s. */
    private fun simulateFriendReply() {
        if (demoReplySent) return
        val lea = friendsFlow.value.find { it.id == "lea" } ?: return
        demoReplySent = true
        scope.launch {
            delay(5_000)
            friendsFlow.value = friendsFlow.value.map {
                if (it.id == "lea") it.copy(dispo = true) else it
            }
            postMessage(
                authorId = lea.id,
                authorName = lea.name,
                text = "Chaud ! On se retrouve où ? 🎪",
            )
        }
    }

    fun sendMessage(text: String, lat: Double? = null, lon: Double? = null) {
        scope.launch {
            val profile = profileFlow.first()
            postMessage(
                authorId = profile.id,
                authorName = profile.name,
                text = text,
                lat = lat,
                lon = lon,
            )
        }
    }

    private fun postMessage(
        authorId: String,
        authorName: String,
        text: String,
        lat: Double? = null,
        lon: Double? = null,
    ) {
        messagesFlow.value = messagesFlow.value + ChatMessage(
            id = nextMessageId++,
            authorId = authorId,
            authorName = authorName,
            text = text,
            lat = lat,
            lon = lon,
        )
    }

    private fun generateInviteCode(): String {
        val existing = groupsFlow.value.map { it.inviteCode }.toSet()
        repeat(40) {
            val code = (1..8).map { CODE_ALPHABET.random() }.joinToString("")
            if (code !in existing) return code
        }
        return UUID.randomUUID().toString().take(8).uppercase()
    }

    private fun encodeFriends(friends: List<Friend>): String {
        val arr = JSONArray()
        friends.forEach { f ->
            arr.put(
                JSONObject()
                    .put("id", f.id)
                    .put("name", f.name)
                    .put("avatarColor", f.avatarColor)
                    .put("dispo", f.dispo),
            )
        }
        return arr.toString()
    }

    private fun decodeFriends(json: String): List<Friend> {
        val arr = JSONArray(json)
        return buildList {
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                add(
                    Friend(
                        id = o.getString("id"),
                        name = o.getString("name"),
                        avatarColor = o.optInt("avatarColor", 0),
                        dispo = o.optBoolean("dispo", false),
                    ),
                )
            }
        }
    }

    private fun encodeGroups(groups: List<CrewGroup>): String {
        val arr = JSONArray()
        groups.forEach { g ->
            arr.put(
                JSONObject()
                    .put("id", g.id)
                    .put("name", g.name)
                    .put("inviteCode", g.inviteCode)
                    .put("memberIds", JSONArray(g.memberIds)),
            )
        }
        return arr.toString()
    }

    private fun decodeGroups(json: String): List<CrewGroup> {
        val arr = JSONArray(json)
        return buildList {
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val membersJson = o.getJSONArray("memberIds")
                val memberIds = buildList {
                    for (j in 0 until membersJson.length()) add(membersJson.getString(j))
                }
                add(
                    CrewGroup(
                        id = o.getString("id"),
                        name = o.getString("name"),
                        inviteCode = o.getString("inviteCode"),
                        memberIds = memberIds,
                    ),
                )
            }
        }
    }

    private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
}
