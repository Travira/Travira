package com.example.travira.remote

import com.example.travira.model.AddedByUser
import com.example.travira.model.Place
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

/**
 * Gson helpers: API sometimes returns ObjectId strings, sometimes populated objects
 * for addedBy / wishlist / addedPlaces.
 */
private class AddedByUserDeserializer : JsonDeserializer<AddedByUser?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): AddedByUser? {
        if (json == null || json.isJsonNull) return null
        if (json.isJsonPrimitive) {
            val id = json.asString
            return if (id.isBlank()) null else AddedByUser(_id = id)
        }
        if (json.isJsonObject) {
            return context?.deserialize(json, AddedByUser::class.java)
        }
        return null
    }
}

private class PlaceListDeserializer : JsonDeserializer<List<Place>> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): List<Place> {
        if (json == null || !json.isJsonArray) return emptyList()
        val out = ArrayList<Place>()
        for (el in json.asJsonArray) {
            when {
                el == null || el.isJsonNull -> continue
                el.isJsonObject -> {
                    context?.deserialize<Place>(el, Place::class.java)?.let { out.add(it) }
                }
                el.isJsonPrimitive -> {
                    val id = el.asString
                    if (id.isNotBlank()) out.add(Place(_id = id))
                }
            }
        }
        return out
    }
}

object RetrofitInstance {

    const val BASE_URL = "https://travira-qg2q.onrender.com/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()
    }

    private val gson by lazy {
        val placeListType = object : TypeToken<List<Place>>() {}.type
        GsonBuilder()
            .registerTypeAdapter(AddedByUser::class.java, AddedByUserDeserializer())
            .registerTypeAdapter(placeListType, PlaceListDeserializer())
            .create()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val placeApi: PlaceApi by lazy { retrofit.create(PlaceApi::class.java) }
    val authApi: AuthApi by lazy { retrofit.create(AuthApi::class.java) }
    val adminApi: AdminApi by lazy { retrofit.create(AdminApi::class.java) }

    /** @deprecated use placeApi */
    val api: PlaceApi get() = placeApi
}
