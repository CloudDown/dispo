package com.dispo.app.data

import com.dispo.app.data.dto.AddFriendRequest
import com.dispo.app.data.dto.AddMemberRequest
import com.dispo.app.data.dto.AvailabilityResponseDto
import com.dispo.app.data.dto.CreateGroupRequest
import com.dispo.app.data.dto.FriendPublicDto
import com.dispo.app.data.dto.GroupPublicDto
import com.dispo.app.data.dto.JoinGroupRequest
import com.dispo.app.data.dto.LoginRequest
import com.dispo.app.data.dto.MessagePublicDto
import com.dispo.app.data.dto.ProfileUpdateRequest
import com.dispo.app.data.dto.RegisterRequest
import com.dispo.app.data.dto.SendMessageRequest
import com.dispo.app.data.dto.TokenResponse
import com.dispo.app.data.dto.UserPublicDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface DispoApi {

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): TokenResponse

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): TokenResponse

    @GET("auth/me")
    suspend fun me(): UserPublicDto

    @PATCH("auth/me")
    suspend fun updateMe(@Body body: ProfileUpdateRequest): UserPublicDto

    @GET("friends")
    suspend fun listFriends(): List<FriendPublicDto>

    @POST("friends")
    suspend fun addFriend(@Body body: AddFriendRequest): FriendPublicDto

    @DELETE("friends/{publicId}")
    suspend fun removeFriend(@Path("publicId") publicId: String)

    @GET("groups")
    suspend fun listGroups(): List<GroupPublicDto>

    @POST("groups")
    suspend fun createGroup(@Body body: CreateGroupRequest): GroupPublicDto

    @POST("groups/join")
    suspend fun joinGroup(@Body body: JoinGroupRequest): GroupPublicDto

    @POST("groups/{groupId}/members")
    suspend fun addGroupMember(
        @Path("groupId") groupId: Int,
        @Body body: AddMemberRequest,
    ): GroupPublicDto

    @DELETE("groups/{groupId}/members/me")
    suspend fun leaveGroup(@Path("groupId") groupId: Int)

    @POST("availability/toggle")
    suspend fun toggleAvailability(@Query("group_id") groupId: Int?): AvailabilityResponseDto

    @GET("availability/me")
    suspend fun myAvailability(@Query("group_id") groupId: Int?): AvailabilityResponseDto

    @GET("chat/{groupId}/messages")
    suspend fun listMessages(@Path("groupId") groupId: Int): List<MessagePublicDto>

    @POST("chat/{groupId}/messages")
    suspend fun sendMessage(
        @Path("groupId") groupId: Int,
        @Body body: SendMessageRequest,
    ): MessagePublicDto
}
