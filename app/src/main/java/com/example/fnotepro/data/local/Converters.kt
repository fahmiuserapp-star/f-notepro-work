package com.example.fnotepro.data.local

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class StringListConverter {
    @TypeConverter
    fun fromStringList(list: List<String>?): String {
        if (list == null) return "[]"
        return Json.encodeToString(list)
    }

    @TypeConverter
    fun toStringList(data: String?): List<String> {
        if (data.isNullOrEmpty()) return emptyList()
        return try {
            Json.decodeFromString<List<String>>(data)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
