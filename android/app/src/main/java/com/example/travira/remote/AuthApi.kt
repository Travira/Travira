package com.example.travira.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {

    @POST("api/users/register")
    suspend fun register(@Body body: RegisterRequest): RegisterResponse

    @POST("api/users/register-admin")
    suspend fun registerAdmin(@Body body: RegisterAdminRequest): RegisterResponse

    @POST("api/users/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @POST("api/users/refresh-token")
    suspend fun refreshToken(@Body body: RefreshRequest): RefreshResponse

    @GET("api/users/me")
    suspend fun getCurrentUser(
        @Header("Authorization") bearer: String
    ): UserProfileResponse

    @GET("api/users/profile")
    suspend fun getProfile(
        @Header("Authorization") bearer: String
    ): UserProfileResponse

    @POST("api/users/logout")
    suspend fun logout(
        @Header("Authorization") bearer: String,
        @Body body: RefreshRequest
    ): SimpleMessageResponse
}
