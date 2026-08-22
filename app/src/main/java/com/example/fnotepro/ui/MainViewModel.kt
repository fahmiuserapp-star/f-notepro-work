package com.example.fnotepro.ui

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fnotepro.data.model.Guide
import com.example.fnotepro.data.model.SavedNumber
import com.example.fnotepro.data.model.SendHistoryItem
import com.example.fnotepro.data.model.UserProfile
import com.example.fnotepro.data.repository.GuideRepository
import com.example.fnotepro.util.AppLanguage
import com.example.fnotepro.util.Localization
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(private val repository: GuideRepository) : ViewModel() {

    private val developerPrimaryUid = "OK7lshSuUrhVBV7WHn2EL0tiOJo2"

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _currentUid = MutableStateFlow("offline")
    val currentUid: StateFlow<String> = _currentUid.asStateFlow()

    private val _userProfile = MutableStateFlow(
        UserProfile(
            uid = "offline",
            email = "offline@local",
            displayName = "السيد فهمي (المدير)",
            safetyOfficerName = "عون السلامة محمد التونسي",
            safetyOfficerPhone = "+21698123456"
        )
    )
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _currentLanguage = MutableStateFlow(AppLanguage.ARABIC)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _adminUids = MutableStateFlow(listOf<String>())
    val adminUids: StateFlow<List<String>> = _adminUids.asStateFlow()

    private val _registeredUsers = MutableStateFlow(
        listOf(
            "admin@fnotepro.tn",
            "fahmi.manager@olivierspalace.com",
            "reception@olivierspalace.com",
            "safety.officer@tunisiatourisme.tn"
        )
    )
    val registeredUsers: StateFlow<List<String>> = _registeredUsers.asStateFlow()

    // Dialog & Screen states
    private val _activeDialog = MutableStateFlow<ActiveDialog?>(null)
    val activeDialog: StateFlow<ActiveDialog?> = _activeDialog.asStateFlow()

    private val _selectedGuide = MutableStateFlow<Guide?>(null)
    val selectedGuide: StateFlow<Guide?> = _selectedGuide.asStateFlow()

    private val _savedNumbers = MutableStateFlow<List<SavedNumber>>(emptyList())
    val savedNumbers: StateFlow<List<SavedNumber>> = _savedNumbers.asStateFlow()

    private val _sendHistory = MutableStateFlow<List<SendHistoryItem>>(emptyList())
    val sendHistory: StateFlow<List<SendHistoryItem>> = _sendHistory.asStateFlow()

    val isDeveloper: StateFlow<Boolean> = combine(_currentUid, _adminUids) { uid, admins ->
        uid == developerPrimaryUid || uid == "offline" || admins.contains(uid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val filteredGuides: StateFlow<List<Guide>> = combine(
        repository.allGuides,
        _searchQuery,
        _currentUid,
        isDeveloper
    ) { guides, query, uid, isDev ->
        var list = if (isDev) {
            guides
        } else {
            guides.filter { it.visibility == "public" || it.userId == uid }
        }
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.name.lowercase().contains(q) ||
                        it.phone.contains(q) ||
                        it.nationality.lowercase().contains(q) ||
                        it.agency.lowercase().contains(q) ||
                        it.plate.lowercase().contains(q)
            }
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allGuidesList: StateFlow<List<Guide>> = repository.allGuides
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun login(email: String, pass: String) {
        if (email.isNotBlank()) {
            val uid = "user_" + email.replace("@", "_").replace(".", "_")
            _currentUid.value = uid
            _isLoggedIn.value = true
            viewModelScope.launch {
                val profile = repository.getUserProfile(uid)
                _userProfile.value = profile.copy(
                    email = email,
                    displayName = if (profile.displayName.isBlank()) email.substringBefore("@") else profile.displayName
                )
            }
        }
    }

    fun loginOffline() {
        _currentUid.value = "offline"
        _isLoggedIn.value = true
        viewModelScope.launch {
            _userProfile.value = repository.getUserProfile("offline")
        }
    }

    fun logout() {
        _isLoggedIn.value = false
        _currentUid.value = "offline"
        _activeDialog.value = null
    }

    fun toggleDarkTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun setLanguage(lang: AppLanguage) {
        _currentLanguage.value = lang
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun openDialog(dialog: ActiveDialog) {
        _activeDialog.value = dialog
    }

    fun closeDialog() {
        _activeDialog.value = null
    }

    fun openDelegationCard(guide: Guide) {
        _selectedGuide.value = guide
        _activeDialog.value = ActiveDialog.DelegationCard(guide)
    }

    fun openSecurityCard(guide: Guide) {
        _selectedGuide.value = guide
        viewModelScope.launch {
            repository.getSavedNumbers(guide.id).collect {
                _savedNumbers.value = it
            }
        }
        viewModelScope.launch {
            repository.getSendHistory(guide.id).collect {
                _sendHistory.value = it
            }
        }
        _activeDialog.value = ActiveDialog.SecurityCard(guide)
    }

    fun openSafetyOfficer(guide: Guide) {
        _selectedGuide.value = guide
        _activeDialog.value = ActiveDialog.SafetyOfficer(guide)
    }

    fun openEditGuide(guide: Guide) {
        _activeDialog.value = ActiveDialog.AddEditGuide(guide)
    }

    fun openAddGuide() {
        _activeDialog.value = ActiveDialog.AddEditGuide(null)
    }

    fun saveGuide(guide: Guide) {
        viewModelScope.launch {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val nowStr = sdf.format(Date())
            val guideToSave = guide.copy(
                userId = if (guide.userId.isBlank()) _currentUid.value else guide.userId,
                creatorName = if (guide.creatorName.isBlank()) _userProfile.value.displayName else guide.creatorName,
                safetyOfficerName = if (guide.safetyOfficerName.isBlank()) _userProfile.value.safetyOfficerName else guide.safetyOfficerName,
                safetyOfficerPhone = if (guide.safetyOfficerPhone.isBlank()) _userProfile.value.safetyOfficerPhone else guide.safetyOfficerPhone,
                safetyOfficerPhoto = if (guide.safetyOfficerPhoto == null) _userProfile.value.safetyOfficerPhoto else guide.safetyOfficerPhoto,
                updatedAt = nowStr,
                createdAt = if (guide.createdAt.isBlank()) nowStr else guide.createdAt
            )
            repository.insertGuide(guideToSave)
            closeDialog()
        }
    }

    fun deleteGuide(id: String) {
        viewModelScope.launch {
            repository.deleteGuide(id)
        }
    }

    fun updateProfile(displayName: String, safetyName: String, safetyPhone: String, safetyPhoto: String?) {
        viewModelScope.launch {
            val updated = _userProfile.value.copy(
                displayName = displayName,
                safetyOfficerName = safetyName,
                safetyOfficerPhone = safetyPhone,
                safetyOfficerPhoto = safetyPhoto
            )
            _userProfile.value = updated
            repository.saveUserProfile(updated)
            closeDialog()
        }
    }

    fun addSavedNumbers(guideId: String, phonesRaw: String) {
        viewModelScope.launch {
            val numbers = phonesRaw.split("\n", ",").map { it.trim() }.filter { it.length >= 8 }
            repository.addSavedNumbers(guideId, numbers)
        }
    }

    fun recordSend(guideId: String, phone: String) {
        viewModelScope.launch {
            repository.recordSendHistory(guideId, phone)
        }
    }

    fun addAdminUid(uid: String) {
        if (uid.isNotBlank() && !_adminUids.value.contains(uid)) {
            _adminUids.value = _adminUids.value + uid
        }
    }

    fun removeAdminUid(uid: String) {
        _adminUids.value = _adminUids.value - uid
    }

    fun createUser(email: String) {
        if (email.isNotBlank() && !_registeredUsers.value.contains(email)) {
            _registeredUsers.value = _registeredUsers.value + email
        }
    }

    fun exportJson(context: Context) {
        viewModelScope.launch {
            val list = filteredGuides.value
            val json = Json { prettyPrint = true }.encodeToString(list)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "F-Note Pro Max Data Export")
                putExtra(Intent.EXTRA_TEXT, json)
            }
            context.startActivity(Intent.createChooser(intent, Localization.get("export_json", _currentLanguage.value)))
        }
    }

    fun exportReport(context: Context) {
        val list = filteredGuides.value
        val sb = StringBuilder()
        sb.append("📋 F-Note Pro Max - ").append(Localization.get("app_subtitle", _currentLanguage.value)).append("\n")
        sb.append("=========================================\n\n")
        list.forEachIndexed { index, g ->
            sb.append("${index + 1}. ${g.name} (${g.nationality})\n")
            sb.append("   📞 ${g.phone} | 🚐 ${g.transportType} | 👥 ${g.peopleCount}\n")
            sb.append("   🇹🇳 ${g.plate} | ${g.fromLocation} ➡️ ${g.toLocation}\n")
            sb.append("   🏢 ${g.agency}\n")
            if (g.notes.isNotBlank()) sb.append("   📝 ${g.notes}\n")
            sb.append("   🛡️ ${g.safetyOfficerName} (${g.safetyOfficerPhone})\n\n")
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "F-Note Pro Max Delegation Report")
            putExtra(Intent.EXTRA_TEXT, sb.toString())
        }
        context.startActivity(Intent.createChooser(intent, Localization.get("export_pdf", _currentLanguage.value)))
    }
}

sealed class ActiveDialog {
    data class AddEditGuide(val guide: Guide?) : ActiveDialog()
    data class DelegationCard(val guide: Guide) : ActiveDialog()
    data class SecurityCard(val guide: Guide) : ActiveDialog()
    data class SafetyOfficer(val guide: Guide) : ActiveDialog()
    object Profile : ActiveDialog()
    object Password : ActiveDialog()
    object ManageAdmins : ActiveDialog()
    object ManageUsers : ActiveDialog()
    object ViewAllGuides : ActiveDialog()
    object Terms : ActiveDialog()
}

class MainViewModelFactory(private val repository: GuideRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
