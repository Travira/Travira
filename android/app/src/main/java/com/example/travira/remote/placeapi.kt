package com.example.travira.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface PlaceApi {

    /**
     * Current backend: GET /api/place
     * Response: { "success": true, "data": [ ...places ] }
     */
    @GET("api/place")
    suspend fun getPlaces(): PlacesResponse

    @GET("api/place/{id}")
    suspend fun getPlaceById(@Path("id") id: String): PlaceResponse

    @POST("api/place/add")
    suspend fun addPlace(
        @Header("Authorization") bearer: String,
        @Body body: AddPlaceRequest
    ): PlaceResponse

    @POST("api/place/{id}/wishlist")
    suspend fun addWishlist(
        @Header("Authorization") bearer: String,
        @Path("id") id: String
    ): SimpleMessageResponse

    @DELETE("api/place/{id}/wishlist")
    suspend fun removeWishlist(
        @Header("Authorization") bearer: String,
        @Path("id") id: String
    ): SimpleMessageResponse

    @GET("api/place/user/wishlist")
    suspend fun getWishlist(
        @Header("Authorization") bearer: String
    ): SimpleMessageResponse

    @POST("api/place/{id}/rating")
    suspend fun ratePlace(
        @Header("Authorization") bearer: String,
        @Path("id") id: String,
        @Body body: Map<String, Int>
    ): SimpleMessageResponse
}
