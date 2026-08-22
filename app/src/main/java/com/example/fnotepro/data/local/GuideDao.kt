package com.example.fnotepro.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.fnotepro.data.model.Guide
import com.example.fnotepro.data.model.SavedNumber
import com.example.fnotepro.data.model.SendHistoryItem
import com.example.fnotepro.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface GuideDao {
    @Query("SELECT * FROM guides ORDER BY createdAt DESC")
    fun getAllGuides(): Flow<List<Guide>>

    @Query("SELECT * FROM guides WHERE id = :id")
    suspend fun getGuideById(id: String): Guide?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGuide(guide: Guide)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGuides(guides: List<Guide>)

    @Update
    suspend fun updateGuide(guide: Guide)

    @Query("DELETE FROM guides WHERE id = :id")
    suspend fun deleteGuideById(id: String)

    @Query("DELETE FROM guides")
    suspend fun clearAllGuides()

    // Saved Numbers
    @Query("SELECT * FROM saved_numbers WHERE guideId = :guideId")
    fun getSavedNumbersForGuide(guideId: String): Flow<List<SavedNumber>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedNumber(number: SavedNumber)

    @Query("DELETE FROM saved_numbers WHERE guideId = :guideId AND phone = :phone")
    suspend fun deleteSavedNumber(guideId: String, phone: String)

    // Send History
    @Query("SELECT * FROM send_history WHERE guideId = :guideId ORDER BY historyId DESC")
    fun getSendHistoryForGuide(guideId: String): Flow<List<SendHistoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSendHistory(item: SendHistoryItem)

    // User Profile
    @Query("SELECT * FROM user_profile WHERE uid = :uid")
    suspend fun getUserProfile(uid: String): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(profile: UserProfile)
}
