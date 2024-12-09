package com.example.nativeapp.apiService

import com.example.nativeapp.utils.BooleanAsNumberAdapter
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:3000/" // Emulator's localhost

    private val retrofit by lazy {
        // Create Gson instance and register custom adapter for both serialization and deserialization
        val gson = GsonBuilder()
            .registerTypeAdapter(Boolean::class.java, BooleanAsNumberAdapter())
            .registerTypeAdapter(Boolean::class.javaPrimitiveType, BooleanAsNumberAdapter()) // Handle primitive boolean
            .create()

        val client = OkHttpClient.Builder()
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson)) // Use custom Gson
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
