package com.example.travira.model


/**
 * Matches the live API response from GET /api/places
 * (raw JSON array with rating field).
 */
data class Place(
    val _id: String = "",
    val id: Int = 0,
    val name: String = "",
    val shortDescription: String? = null,
    val description: String? = null,
    val city: String? = null,
    val state: String? = null,
    val country: String? = null,
    val location: String? = null,
    val imageUrl: String? = null,
    val rating: Double = 0.0,
    val averageRating: Double = 0.0,
    val visitorsCount: Int = 0,
    val approvalStatus: String? = null
) {
    /** Prefer live `rating`, fall back to averageRating */
    val displayRating: Double
        get() = if (rating > 0) rating else averageRating
}
