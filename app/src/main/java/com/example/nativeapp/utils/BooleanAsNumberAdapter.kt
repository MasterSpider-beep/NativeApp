package com.example.nativeapp.utils

import android.util.Log
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

class BooleanAsNumberAdapter : TypeAdapter<Boolean>() {
    override fun write(out: JsonWriter, value: Boolean?) {
        Log.d("BooleanAdapter", "write: $value")
        if (value == null) {
            out.nullValue() // Handle nulls
        } else {
            out.value(if (value) 1 else 0) // Serialize true as 1, false as 0
        }
    }

    override fun read(reader: JsonReader): Boolean {
        return when (reader.peek()) {
            JsonToken.BOOLEAN -> reader.nextBoolean() // Handle standard true/false
            JsonToken.NUMBER -> reader.nextInt() != 0 // Convert 0/1 to false/true
            JsonToken.NULL -> {
                reader.nextNull()
                false // Default for null
            }
            else -> throw IllegalStateException("Unexpected value: ${reader.peek()}")
        }
    }
}
