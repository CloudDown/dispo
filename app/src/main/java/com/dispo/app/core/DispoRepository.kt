package com.dispo.app.core

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.time.LocalDate
import java.time.ZoneId
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

private val Context.dataStore by preferencesDataStore(name = "dispo")

/**
 * Source de vérité locale (MVP sans backend).
 * L'état "je suis dispo" est persisté dans DataStore pour être partagé
 * entre l'app et le widget. Les amis sont simulés en mémoire en attendant
 * l'intégration Supabase.
 */
class DispoRepository private constructor(private val appContext: Context) {

    companion object {
        private val KEY_ACTIVE = booleanPreferencesKey("me_dispo")
        private val KEY_EXPIRES_AT = longPreferencesKey("me_dispo_expires_at")

        @Volatile
        private var instance: DispoRepository? = null

        fun get(context: Context): DispoRepository =
            instance ?: synchronized(this) {
                instance ?: DispoRepository(context.applicationContext).also { instance = it }
            }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Seule Léa est simulée (démo backend) ; les vrais membres viendront de Supabase.
    private val friendsFlow = MutableStateFlow(
        listOf(Friend(id = "lea", name = "Léa")),
    )

    private val messagesFlow = MutableStateFlow<List<ChatMessage>>(emptyList())
    private var nextMessageId = 1L
    private var demoReplySent = false

    val meDispoFlow = appContext.dataStore.data.map { prefs ->
        val active = prefs[KEY_ACTIVE] ?: false
        val expiresAt = prefs[KEY_EXPIRES_AT] ?: 0L
        active && System.currentTimeMillis() < expiresAt
    }

    val uiState = combine(meDispoFlow, friendsFlow, messagesFlow) { me, friends, messages ->
        DispoUiState(meDispo = me, friends = friends, messages = messages)
    }.stateIn(scope, SharingStarted.Eagerly, DispoUiState())

    /** Fin de journée locale : la dispo expire à minuit. */
    private fun endOfDayMillis(): Long =
        LocalDate.now()
            .plusDays(1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

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

    /** Démo sans backend : Léa répond dispo quelques secondes après toi. */
    private fun simulateFriendReply() {
        if (demoReplySent) return
        demoReplySent = true
        scope.launch {
            delay(5_000)
            friendsFlow.value = friendsFlow.value.map {
                if (it.id == "lea") it.copy(dispo = true) else it
            }
            postMessage(
                authorId = "lea",
                authorName = "Léa",
                text = "Chaud ! On se retrouve où ? 🎪",
            )
        }
    }

    fun sendMessage(text: String, lat: Double? = null, lon: Double? = null) {
        postMessage(authorId = "me", authorName = "Moi", text = text, lat = lat, lon = lon)
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
}
