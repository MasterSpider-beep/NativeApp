package com.example.nativeapp.domain

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.nativeapp.utils.BooleanAsNumberAdapter
import com.google.gson.annotations.JsonAdapter

@Entity(tableName = "books")
data class Book(
                @PrimaryKey val id: Int,
                val title: String,
                val author: String,
                val releaseDate: String,
                val quantity: Int,
                @JsonAdapter(BooleanAsNumberAdapter::class) val isRentable: Boolean,
                val owner: String? = "",
                val image: String? = "",
                val lat: Double? = 0.0,
                val long: Double? = 0.0){
    constructor() : this(
        id = 0,
        title = "",
        author = "",
        releaseDate = "",
        quantity = 0,
        isRentable = false,
        owner = "",
        image = "",
        lat = null,
        long = null
    )
}
