package com.dispo.app.core

/** Profil local : le nom sert de pseudo (idéalement le même qu'Instagram). */
data class UserProfile(
    /** Pseudo normalisé (clé interne), ex. "lea". */
    val id: String,
    /** Affichage / pseudo saisi. */
    val name: String,
    /** Index de couleur d'avatar (0..palette-1). */
    val avatarColor: Int = 0,
)

data class Friend(
    val id: String,
    val name: String,
    val avatarColor: Int = 0,
    val dispo: Boolean = false,
)

data class ChatMessage(
    val id: Long,
    val authorId: String,
    val authorName: String,
    val text: String,
    val lat: Double? = null,
    val lon: Double? = null,
    val timestamp: Long = System.currentTimeMillis(),
) {
    val hasLocation: Boolean get() = lat != null && lon != null
}

data class DispoUiState(
    val meDispo: Boolean = false,
    val profile: UserProfile = UserProfile(id = "toi", name = "toi"),
    val friends: List<Friend> = emptyList(),
    val messages: List<ChatMessage> = emptyList(),
    val addFriendFeedback: String? = null,
) {
    val dispoCount: Int get() = (if (meDispo) 1 else 0) + friends.count { it.dispo }
    val chatUnlocked: Boolean get() = dispoCount >= 2
}
