package com.example.nativeapp.domain

import com.example.nativeapp.utils.BooleanAsNumberAdapter
import com.google.gson.annotations.JsonAdapter

data class Book(val id: Int,
                val title: String,
                val author: String,
                val releaseDate: String,
                val quantity: Int,
                @JsonAdapter(BooleanAsNumberAdapter::class) val isRentable: Boolean,
                val owner: String? = null,
                val image: String? = null,
                val lat: Double? = null,
                val long: Double? = null)
