package com.example.travira.remote

import com.example.travira.model.Place
import com.example.travira.model.User
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

data class AdminPlacesResponse(
    val success: Boolean = false,
    val places: List<Place> = emptyList(),
    val counts: PlaceCounts? = null
)

data class PlaceCounts(
    val pending: Int = 0,
    val approved: Int = 0,
    val rejected: Int = 0,
    val total: Int = 0
)

data class AdminPlaceDetailResponse(
    val success: Boolean = false,
    val place: Place? = null,
    val stats: PlaceStats? = null
)

data class PlaceStats(
    val visitorsCount: Int = 0,
    val averageRating: Double = 0.0,
    val ratingsCount: Int = 0,
    val wishlistCount: Int = 0
)

data class AdminUsersResponse(
    val success: Boolean = false,
    val users: List<User> = emptyList()
)

data class AdminUserDetailResponse(
    val success: Boolean = false,
    val user: User? = null,
    val passwordNote: String? = null
)

data class AdminListResponse(
    val success: Boolean = false,
    val admins: List<User> = emptyList()
)

data class StatusBody(
    val status: String,
    val feedback: String? = null,
    val message: String? = null
)

data class RegisterAdminRequest(
    val name: String,
    val email: String,
    val password: String,
    val phone: String? = null,
    val location: String? = null
)

interface AdminApi {

    @GET("api/admin/places")
    suspend fun getAllPlaces(
        @Header("Authorization") bearer: String,
        @Query("status") status: String? = "all"
    ): AdminPlacesResponse

    @GET("api/admin/places/{id}")
    suspend fun getPlaceDetail(
        @Header("Authorization") bearer: String,
        @Path("id") id: String
    ): AdminPlaceDetailResponse

    @PUT("api/admin/places/{id}/approve")
    suspend fun approvePlace(
        @Header("Authorization") bearer: String,
        @Path("id") id: String,
        @Body body: Map<String, String>
    ): SimpleMessageResponse

    @PUT("api/admin/places/{id}/reject")
    suspend fun rejectPlace(
        @Header("Authorization") bearer: String,
        @Path("id") id: String,
        @Body body: Map<String, String>
    ): SimpleMessageResponse

    @PUT("api/admin/places/{id}/status")
    suspend fun setPlaceStatus(
        @Header("Authorization") bearer: String,
        @Path("id") id: String,
        @Body body: StatusBody
    ): SimpleMessageResponse

    @DELETE("api/admin/places/{id}")
    suspend fun deletePlace(
        @Header("Authorization") bearer: String,
        @Path("id") id: String
    ): SimpleMessageResponse

    @POST("api/admin/places")
    suspend fun addPlace(
        @Header("Authorization") bearer: String,
        @Body body: AddPlaceRequest
    ): PlaceResponse

    @GET("api/admin/users")
    suspend fun getUsers(
        @Header("Authorization") bearer: String
    ): AdminUsersResponse

    @GET("api/admin/users/{id}")
    suspend fun getUserDetail(
        @Header("Authorization") bearer: String,
        @Path("id") id: String
    ): AdminUserDetailResponse

    @POST("api/admin/users")
    suspend fun createUser(
        @Header("Authorization") bearer: String,
        @Body body: Map<String, String>
    ): SimpleMessageResponse

    @GET("api/admin/admins")
    suspend fun getAdmins(
        @Header("Authorization") bearer: String
    ): AdminListResponse

    @PUT("api/admin/admins/{id}/status")
    suspend fun setAdminStatus(
        @Header("Authorization") bearer: String,
        @Path("id") id: String,
        @Body body: StatusBody
    ): SimpleMessageResponse
}
