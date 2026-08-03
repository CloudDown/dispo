package com.dispo.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dispo.app.data.dto.TokenResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.tokenStore by preferencesDataStore(name = "dispo_auth")

class TokenStore(private val context: Context) {

    companion object {
        private val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val KEY_USER_ID = intPreferencesKey("user_id")
        private val KEY_PUBLIC_ID = stringPreferencesKey("public_id")
        private val KEY_DISPLAY_NAME = stringPreferencesKey("display_name")
        private val KEY_AVATAR_COLOR = intPreferencesKey("avatar_color")
    }

    /** Cache mémoire pour l'intercepteur OkHttp (lecture synchrone). */
    @Volatile
    var bearerToken: String? = null
        private set

    val isLoggedIn: Flow<Boolean> = context.tokenStore.data.map { prefs ->
        !prefs[KEY_ACCESS_TOKEN].isNullOrBlank()
    }

    suspend fun loadIntoMemory() {
        bearerToken = context.tokenStore.data.first()[KEY_ACCESS_TOKEN]
    }

    suspend fun save(token: TokenResponse) {
        bearerToken = token.accessToken
        context.tokenStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN] = token.accessToken
            prefs[KEY_USER_ID] = token.userId
            prefs[KEY_PUBLIC_ID] = token.publicId
            prefs[KEY_DISPLAY_NAME] = token.displayName
            prefs[KEY_AVATAR_COLOR] = token.avatarColor
        }
    }

    suspend fun clear() {
        bearerToken = null
        context.tokenStore.edit { it.clear() }
    }

    suspend fun userId(): Int? =
        context.tokenStore.data.first()[KEY_USER_ID]

    suspend fun publicId(): String? =
        context.tokenStore.data.first()[KEY_PUBLIC_ID]
}
