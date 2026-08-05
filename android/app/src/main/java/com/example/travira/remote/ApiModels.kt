package com.example.travira.remote

import com.example.travira.model.Place
import com.example.travira.model.User

// ── Places ──────────────────────────────────────────

data class PlacesResponse(
    val success: Boolean = false,
    val data: List<Place> = emptyList(),
    val message: String? = null
)

data class PlaceResponse(
    val success: Boolean = false,
    val place: Place? = null,
    val message: String? = null
)

data class AddPlaceRequest(
    val name: String,
    val shortDescription: String? = null,
    val description: String? = null,
    val city: String? = null,
    val state: String? = null,
    val country: String? = null,
    val location: String? = null,
    val imageUrl: String? = null
)

// ── Auth ────────────────────────────────────────────

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

data class AuthUserDto(
    val id: String? = null,
    val _id: String? = null,
    val name: String = "",
    val email: String = "",
    val role: String = "user"
)

data class LoginResponse(
    val message: String? = null,
    val accessToken: String = "",
    val refreshToken: String = "",
    val user: AuthUserDto? = null
)

data class RegisterResponse(
    val message: String? = null,
    val user: AuthUserDto? = null
)

data class RefreshRequest(
    val refreshToken: String
)

data class RefreshResponse(
    val accessToken: String = ""
)

data class UserProfileResponse(
    val success: Boolean = false,
    val user: User? = null,
    val message: String? = null
)

data class SimpleMessageResponse(
    val success: Boolean = false,
    val message: String? = null
)
