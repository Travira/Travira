package com.example.travira.remote

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Uploads an image to Cloudinary (unsigned preset) and returns the secure URL.
 *
 * Setup on Cloudinary dashboard:
 * 1. Settings → Upload → Add upload preset
 * 2. Signing mode: Unsigned
 * 3. Put the preset name in UPLOAD_PRESET below
 * 4. Cloud name is already set from your existing image URLs
 */
object CloudinaryUploader {

    // From your existing Cloudinary URLs in PlaceData
    private const val CLOUD_NAME = "yv3rd7a3"

    /**
     * Create an unsigned upload preset in Cloudinary and put its name here.
     * Example: "travira_unsigned"
     */
    private const val UPLOAD_PRESET = "travira_unsigned"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun uploadImage(context: Context, imageUri: Uri): String = withContext(Dispatchers.IO) {
        val bytes = context.contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
            ?: throw Exception("Cannot read image")

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("upload_preset", UPLOAD_PRESET)
            .addFormDataPart(
                "file",
                "place_${System.currentTimeMillis()}.jpg",
                bytes.toRequestBody("image/*".toMediaTypeOrNull())
            )
            .build()

        val request = Request.Builder()
            .url("https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload")
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            val json = response.body?.string() ?: throw Exception("Empty Cloudinary response")
            if (!response.isSuccessful) {
                throw Exception("Cloudinary upload failed: $json")
            }
            val secureUrl = JSONObject(json).optString("secure_url")
            if (secureUrl.isBlank()) throw Exception("No secure_url in Cloudinary response")
            secureUrl
        }
    }
}
