package com.example.travira.model

data class User(
    val id: String = "",
    val _id: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "user",
    val profileImage: String? = null,
    val wishlist: List<Place> = emptyList(),
    val addedPlaces: List<Place> = emptyList(),
    val notifications: List<NotificationItem> = emptyList()
) {
    val userId: String get() = id.ifBlank { _id }
}

data class NotificationItem(
    val title: String = "",
    val message: String = "",
    val read: Boolean = false,
    val createdAt: String? = null
)
