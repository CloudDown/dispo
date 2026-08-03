package com.dispo.app.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    @SerialName("public_id") val publicId: String,
    val password: String,
)

@Serializable
data class RegisterRequest(
    @SerialName("display_name") val displayName: String,
    val password: String,
    @SerialName("public_id") val publicId: String? = null,
)

@Serializable
data class TokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("token_type") val tokenType: String = "bearer",
    @SerialName("user_id") val userId: Int,
    @SerialName("public_id") val publicId: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("avatar_color") val avatarColor: Int,
)

@Serializable
data class UserPublicDto(
    val id: Int,
    @SerialName("public_id") val publicId: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("avatar_color") val avatarColor: Int,
)

@Serializable
data class ProfileUpdateRequest(
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("avatar_color") val avatarColor: Int? = null,
)

@Serializable
data class FriendPublicDto(
    @SerialName("user_id") val userId: Int,
    @SerialName("public_id") val publicId: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("avatar_color") val avatarColor: Int,
    val dispo: Boolean = false,
)

@Serializable
data class AddFriendRequest(
    @SerialName("public_id") val publicId: String,
)

@Serializable
data class GroupMemberDto(
    @SerialName("user_id") val userId: Int,
    @SerialName("public_id") val publicId: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("avatar_color") val avatarColor: Int,
    val dispo: Boolean = false,
)

@Serializable
data class GroupPublicDto(
    val id: Int,
    val name: String,
    @SerialName("invite_code") val inviteCode: String,
    val members: List<GroupMemberDto> = emptyList(),
)

@Serializable
data class CreateGroupRequest(val name: String)

@Serializable
data class JoinGroupRequest(
    @SerialName("invite_code") val inviteCode: String,
)

@Serializable
data class AddMemberRequest(
    @SerialName("public_id") val publicId: String,
)

@Serializable
data class SendMessageRequest(
    val text: String,
    val lat: Double? = null,
    val lon: Double? = null,
)

@Serializable
data class MessagePublicDto(
    val id: Int,
    @SerialName("group_id") val groupId: Int,
    @SerialName("author_id") val authorId: Int,
    @SerialName("author_public_id") val authorPublicId: String,
    @SerialName("author_name") val authorName: String,
    val text: String,
    val lat: Double? = null,
    val lon: Double? = null,
    @SerialName("created_at") val createdAt: String,
)

@Serializable
data class AvailabilityResponseDto(
    val active: Boolean,
    @SerialName("expires_at") val expiresAt: String? = null,
    @SerialName("dispo_count_in_group") val dispoCountInGroup: Int = 0,
)

@Serializable
data class ApiErrorDto(
    val detail: String? = null,
)
