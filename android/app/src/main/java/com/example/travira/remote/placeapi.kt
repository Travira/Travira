package com.example.travira.remote


import com.example.travira.model.Place
import retrofit2.http.GET

interface PlaceApi {

    @GET("api/places")
    suspend fun getPlaces(): List<Place>

}