package com.example.nativeapp.utils

import androidx.room.TypeConverter

class BooleanConverter {
    @TypeConverter
    fun fromInt(value: Int): Boolean = value == 1

    @TypeConverter
    fun toInt(value: Boolean): Int = if (value) 1 else 0
}