package com.example.fnotepro.data.repository

import com.example.fnotepro.data.local.GuideDao
import com.example.fnotepro.data.model.Guide
import com.example.fnotepro.data.model.SavedNumber
import com.example.fnotepro.data.model.SendHistoryItem
import com.example.fnotepro.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GuideRepository(private val guideDao: GuideDao) {

    val allGuides: Flow<List<Guide>> = guideDao.getAllGuides()

    suspend fun insertGuide(guide: Guide) {
        guideDao.insertGuide(guide)
    }

    suspend fun updateGuide(guide: Guide) {
        guideDao.updateGuide(guide)
    }

    suspend fun deleteGuide(id: String) {
        guideDao.deleteGuideById(id)
    }

    suspend fun getGuideById(id: String): Guide? {
        return guideDao.getGuideById(id)
    }

    fun getSavedNumbers(guideId: String): Flow<List<SavedNumber>> {
        return guideDao.getSavedNumbersForGuide(guideId)
    }

    suspend fun addSavedNumbers(guideId: String, phones: List<String>) {
        phones.forEach { phone ->
            val clean = phone.trim()
            if (clean.isNotEmpty()) {
                guideDao.insertSavedNumber(SavedNumber(guideId = guideId, phone = clean))
            }
        }
    }

    fun getSendHistory(guideId: String): Flow<List<SendHistoryItem>> {
        return guideDao.getSendHistoryForGuide(guideId)
    }

    suspend fun recordSendHistory(guideId: String, phone: String) {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        guideDao.insertSendHistory(
            SendHistoryItem(
                guideId = guideId,
                phone = phone,
                timestamp = sdf.format(Date()),
                status = "sent"
            )
        )
    }

    suspend fun getUserProfile(uid: String): UserProfile {
        val existing = guideDao.getUserProfile(uid)
        return existing ?: UserProfile(
            uid = uid,
            email = if (uid == "offline") "offline@local" else uid,
            displayName = if (uid == "offline") "السيد فهمي (المدير)" else "فهمي",
            safetyOfficerName = "عون السلامة محمد التونسي",
            safetyOfficerPhone = "+21698123456",
            safetyOfficerPhoto = null
        )
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        guideDao.saveUserProfile(profile)
    }

    suspend fun seedInitialDataIfEmpty() {
        val current = guideDao.getAllGuides().firstOrNull()
        if (current.isNullOrEmpty()) {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val nowStr = sdf.format(Date())
            val initial = listOf(
                Guide(
                    id = "guide_1",
                    userId = "offline",
                    creatorName = "السيد فهمي",
                    name = "فوزي الطرابلسي",
                    phone = "98234567",
                    nationality = "فرنسية 🇫🇷",
                    peopleCount = "24",
                    carCount = 1,
                    transportType = "حافلة كبيرة",
                    delegationType = "tourist",
                    fromLocation = "مطار جربة جرجيس",
                    toLocation = "نزل Les Oliviers Palace - صفاقس",
                    plate = "4210 تونس 185",
                    guidePhoto = null,
                    busPhoto = null,
                    safetyOfficerName = "عون السلامة محمد",
                    safetyOfficerPhone = "98123456",
                    agency = "Voyages Tunisie Tourisme",
                    notes = "وصول الوفد في حدود الساعة 14:00. ترتيب إجراءات الاستقبال وتوزيع الغرف بأمان.",
                    visibility = "public",
                    isSynced = true,
                    createdAt = nowStr,
                    updatedAt = nowStr
                ),
                Guide(
                    id = "guide_2",
                    userId = "offline",
                    creatorName = "السيد فهمي",
                    name = "مراد بن سالم",
                    phone = "55443322",
                    nationality = "إيطالية 🇮🇹",
                    peopleCount = "18",
                    carCount = 1,
                    transportType = "حافلة صغيرة",
                    delegationType = "diplomatic",
                    fromLocation = "تونس قرطاج",
                    toLocation = "صفاقس المدينة العتيقة",
                    plate = "9855 تونس 214",
                    guidePhoto = null,
                    busPhoto = null,
                    safetyOfficerName = "عون السلامة كمال",
                    safetyOfficerPhone = "97654321",
                    agency = "Mediterranée Evasion",
                    notes = "جولة استطلاعية ثقافية مع مرافقة أمنية كاملة.",
                    visibility = "public",
                    isSynced = true,
                    createdAt = nowStr,
                    updatedAt = nowStr
                )
            )
            guideDao.insertGuides(initial)
        }
    }
}
