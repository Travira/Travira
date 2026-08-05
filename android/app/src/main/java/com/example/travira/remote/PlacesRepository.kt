package com.example.travira.remote


import com.example.travira.model.Place
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * Fetches places from the live API and tolerates both response shapes:
 *  - raw array:  [ {...}, {...} ]          ← current Render deploy
 *  - wrapped:    { "success": true, "data": [ ... ] }
 */
object PlacesRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val listType = object : TypeToken<List<Place>>() {}.type

    suspend fun fetchPlaces(): List<Place> {
        // Prefer Retrofit typed call first
        return try {
            RetrofitInstance.placeApi.getPlaces()
        } catch (e: Exception) {
            // Fallback: raw parse (handles object-vs-array mismatch)
            fetchPlacesRaw()
        }
    }

    private fun fetchPlacesRaw(): List<Place> {
        val request = Request.Builder()
            .url(RetrofitInstance.BASE_URL + "api/places")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string()
                ?: throw Exception("Empty response from server")
            if (!response.isSuccessful) {
                throw Exception("HTTP ${response.code}: $body")
            }

            val element = JsonParser.parseString(body)
            return when {
                element.isJsonArray -> gson.fromJson(element, listType)
                element.isJsonObject -> {
                    val obj = element.asJsonObject
                    val arr = when {
                        obj.has("data") && obj.get("data").isJsonArray -> obj.getAsJsonArray("data")
                        obj.has("places") && obj.get("places").isJsonArray -> obj.getAsJsonArray("places")
                        else -> throw Exception("Unexpected JSON object (no data array)")
                    }
                    gson.fromJson(arr, listType)
                }
                else -> throw Exception("Unexpected JSON root")
            }
        }
    }
}
