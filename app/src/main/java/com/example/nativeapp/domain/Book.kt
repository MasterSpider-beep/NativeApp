package com.example.nativeapp.domain

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.nativeapp.utils.BooleanAsNumberAdapter
import com.google.gson.annotations.JsonAdapter

@Entity(tableName = "books")
data class Book(
    @PrimaryKey var id: Int,
    var title: String,
    var author: String,
    var releaseDate: String,
    var quantity: Int,
    var isRentable: Boolean,
    var owner: String? = null,
    var image: String? = null,
    var lat: Double? = null,
    @ColumnInfo(name = "longitude") var long: Double? = null){
    constructor() : this(
        id = 0,
        title = "",
        author = "",
        releaseDate = "",
        quantity = 0,
        isRentable = false,
        owner = "",
        image = "",
        lat = 0.0,
        long = 0.0
    )
}
