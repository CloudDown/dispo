package com.dispo.app.core

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.time.LocalDate
import java.time.ZoneId
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
        private val KEY_FRIENDS_JSON = stringPreferencesKey("friends_json")

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
        )
    }

    val uiState = combine(
        meDispoFlow,
        profileFlow,
        friendsFlow,
        messagesFlow,
        feedbackFlow,
    ) { me, profile, friends, messages, feedback ->
        DispoUiState(
            meDispo = me,
            profile = profile,
            friends = friends,
            messages = messages,
            addFriendFeedback = feedback,
        )
    }.stateIn(scope, SharingStarted.Eagerly, DispoUiState())

    init {
        scope.launch {
            ensureProfile()
            loadFriends()
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
            // Migration : anciens comptes avec ID aléatoire → id = pseudo
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
        // Remet les dispos amis à false (sauf qu'on rejouera la démo Léa si besoin)
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
        appContext.dataStore.edit {
            it[KEY_USER_NAME] = handle
            it[KEY_USER_ID] = handle
        }
    }

    suspend fun cycleAvatarColor() {
        val current = appContext.dataStore.data.first()[KEY_AVATAR_COLOR] ?: 0
        appContext.dataStore.edit { it[KEY_AVATAR_COLOR] = (current + 1) % 6 }
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
            feedbackFlow.value = "Déjà dans ton crew"
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
        feedbackFlow.value = "Retiré du crew"
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
}
