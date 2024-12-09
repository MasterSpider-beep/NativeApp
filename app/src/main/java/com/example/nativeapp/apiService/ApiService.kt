package com.example.nativeapp.apiService

import com.example.nativeapp.domain.Book
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    @GET("books")
    fun getBooks(@Header("Authorization") token: String): Call<List<Book>>

    @GET("books/{id}")
    fun getBookById(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Call<Book>

    @PUT("books")
    fun createBook(
        @Header("Authorization") token: String,
        @Body book: Book
    ): Call<Book>
}