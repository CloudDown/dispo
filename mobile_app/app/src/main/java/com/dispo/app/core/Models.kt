package com.dispo.app.core

data class Friend(
    val id: String,
    val name: String,
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
    val friends: List<Friend> = emptyList(),
    val messages: List<ChatMessage> = emptyList(),
) {
    val dispoCount: Int get() = (if (meDispo) 1 else 0) + friends.count { it.dispo }
    val chatUnlocked: Boolean get() = dispoCount >= 2
}
