package com.dispo.app.data

import com.dispo.app.BuildConfig
import com.dispo.app.core.ChatMessage
import com.dispo.app.core.CrewGroup
import com.dispo.app.core.Friend
import com.dispo.app.core.UserProfile
import com.dispo.app.data.dto.FriendPublicDto
import com.dispo.app.data.dto.GroupPublicDto
import com.dispo.app.data.dto.MessagePublicDto
import com.dispo.app.data.dto.UserPublicDto
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object ApiMappers {

    fun toProfile(user: UserPublicDto, avatarPath: String? = null): UserProfile =
        UserProfile(
            id = user.publicId,
            name = user.displayName,
            avatarColor = user.avatarColor,
            avatarPath = avatarPath,
        )

    fun toFriend(dto: FriendPublicDto): Friend =
        Friend(
            id = dto.publicId,
            name = dto.displayName,
            avatarColor = dto.avatarColor,
            dispo = dto.dispo,
        )

    fun toGroup(dto: GroupPublicDto): CrewGroup =
        CrewGroup(
            id = dto.id.toString(),
            name = dto.name,
            inviteCode = dto.inviteCode,
            memberIds = dto.members.map { it.publicId },
        )

    fun toMessage(dto: MessagePublicDto): ChatMessage =
        ChatMessage(
            id = dto.id.toLong(),
            authorId = dto.authorPublicId,
            authorName = dto.authorName,
            text = dto.text,
            lat = dto.lat,
            lon = dto.lon,
            timestamp = parseTimestamp(dto.createdAt),
        )

    private fun parseTimestamp(raw: String): Long {
        return try {
            when {
                raw.endsWith("Z") -> Instant.parse(raw).toEpochMilli()
                raw.contains("T") -> LocalDateTime.parse(raw, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    .toInstant(ZoneOffset.UTC)
                    .toEpochMilli()
                else -> System.currentTimeMillis()
            }
        } catch (_: Exception) {
            System.currentTimeMillis()
        }
    }
}

fun apiErrorMessage(t: Throwable): String =
    when (t) {
        is retrofit2.HttpException -> {
            val body = t.response()?.errorBody()?.string().orEmpty()
            when {
                body.contains("detail") && body.length < 120 ->
                    body.substringAfter("detail\":\"").substringBefore("\"").ifBlank { "Erreur ${t.code()}" }
                t.code() == 401 -> "Session expirée — reconnecte-toi"
                t.code() == 403 -> "Action non autorisée"
                t.code() == 404 -> "Introuvable sur le serveur"
                else -> "Erreur réseau (${t.code()})"
            }
        }
        else -> t.message ?: "Erreur réseau"
    }
