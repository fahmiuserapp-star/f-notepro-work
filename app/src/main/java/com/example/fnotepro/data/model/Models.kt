package com.example.fnotepro.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "guides")
data class Guide(
    @PrimaryKey val id: String,
    val userId: String = "offline",
    val creatorName: String = "",
    val name: String = "",
    val phone: String = "",
    val nationality: String = "",
    val peopleCount: String = "",
    val carCount: Int = 1,
    val transportType: String = "",
    val delegationType: String = "tourist", // "tourist", "diplomatic", "official", "business", "sports"
    val fromLocation: String = "",
    val toLocation: String = "",
    val plate: String = "",
    val isForeignPlate: Boolean = false,
    val guidePhoto: String? = null,
    val busPhoto: String? = null,
    val vehiclePhotos: List<String> = emptyList(), // Up to 5 photos
    val motorcyclePhotos: List<String> = emptyList(), // Up to 10 photos
    val safetyOfficerName: String = "",
    val safetyOfficerPhone: String = "",
    val safetyOfficerPhoto: String? = null,
    val agency: String = "",
    val notes: String = "",
    val visibility: String = "public", // "public" or "private"
    val isSynced: Boolean = true,
    val createdAt: String = "",
    val updatedAt: String = ""
)

@Serializable
@Entity(tableName = "saved_numbers")
data class SavedNumber(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val guideId: String,
    val phone: String
)

@Serializable
@Entity(tableName = "send_history")
data class SendHistoryItem(
    @PrimaryKey(autoGenerate = true) val historyId: Long = 0,
    val guideId: String,
    val phone: String,
    val timestamp: String,
    val status: String = "sent"
)

@Serializable
@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val uid: String,
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String? = null,
    val safetyOfficerName: String = "",
    val safetyOfficerPhone: String = "",
    val safetyOfficerPhoto: String? = null
)
