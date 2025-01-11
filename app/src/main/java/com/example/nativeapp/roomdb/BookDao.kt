package com.example.nativeapp.roomdb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.nativeapp.domain.Book

@Dao
interface BookDao {
    @Insert
    suspend fun insertBooks(books: List<Book>)

    @Query("SELECT * FROM books")
    suspend fun getBooks(): List<Book>
}