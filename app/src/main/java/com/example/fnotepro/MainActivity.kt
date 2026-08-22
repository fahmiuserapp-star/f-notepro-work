package com.example.fnotepro

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.fnotepro.ui.ActiveDialog
import com.example.fnotepro.ui.MainViewModel
import com.example.fnotepro.ui.MainViewModelFactory
import com.example.fnotepro.ui.screens.AddEditGuideDialog
import com.example.fnotepro.ui.screens.ChangePasswordDialog
import com.example.fnotepro.ui.screens.DelegationCardDialog
import com.example.fnotepro.ui.screens.HomeScreen
import com.example.fnotepro.ui.screens.LoginScreen
import com.example.fnotepro.ui.screens.ManageAdminsDialog
import com.example.fnotepro.ui.screens.ManageUsersDialog
import com.example.fnotepro.ui.screens.ProfileDialog
import com.example.fnotepro.ui.screens.SafetyOfficerDialog
import com.example.fnotepro.ui.screens.SecurityCardDialog
import com.example.fnotepro.ui.screens.TermsDialog
import com.example.fnotepro.ui.screens.ViewAllGuidesDialog
import com.example.fnotepro.ui.theme.FNoteProTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory((application as FNoteApp).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            val currentLanguage by viewModel.currentLanguage.collectAsState()
            val isLoggedIn by viewModel.isLoggedIn.collectAsState()
            val userProfile by viewModel.userProfile.collectAsState()
            val guides by viewModel.filteredGuides.collectAsState()
            val allGuides by viewModel.allGuidesList.collectAsState()
            val searchQuery by viewModel.searchQuery.collectAsState()
            val isDeveloper by viewModel.isDeveloper.collectAsState()
            val adminUids by viewModel.adminUids.collectAsState()
            val registeredUsers by viewModel.registeredUsers.collectAsState()
            val activeDialog by viewModel.activeDialog.collectAsState()
            val savedNumbers by viewModel.savedNumbers.collectAsState()
            val sendHistory by viewModel.sendHistory.collectAsState()

            val layoutDir = if (currentLanguage.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

            CompositionLocalProvider(LocalLayoutDirection provides layoutDir) {
                FNoteProTheme(darkTheme = isDarkTheme) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        if (!isLoggedIn) {
                            LoginScreen(
                                currentLanguage = currentLanguage,
                                onLanguageChange = { viewModel.setLanguage(it) },
                                onLogin = { email, pass -> viewModel.login(email, pass) },
                                onLoginOffline = { viewModel.loginOffline() }
                            )
                        } else {
                            HomeScreen(
                                guides = guides,
                                userProfile = userProfile,
                                isDarkTheme = isDarkTheme,
                                currentLanguage = currentLanguage,
                                searchQuery = searchQuery,
                                isDeveloper = isDeveloper,
                                onSearchChange = { viewModel.setSearchQuery(it) },
                                onLanguageChange = { viewModel.setLanguage(it) },
                                onToggleDarkTheme = { viewModel.toggleDarkTheme() },
                                onAddGuide = { viewModel.openAddGuide() },
                                onOpenCard = { viewModel.openDelegationCard(it) },
                                onOpenSecurity = { viewModel.openSecurityCard(it) },
                                onOpenSafetyOfficer = { viewModel.openSafetyOfficer(it) },
                                onEditGuide = { viewModel.openEditGuide(it) },
                                onDeleteGuide = {
                                    viewModel.deleteGuide(it)
                                    Toast.makeText(this, "🗑️ Deleted", Toast.LENGTH_SHORT).show()
                                },
                                onOpenProfile = { viewModel.openDialog(ActiveDialog.Profile) },
                                onOpenManageAdmins = { viewModel.openDialog(ActiveDialog.ManageAdmins) },
                                onOpenManageUsers = { viewModel.openDialog(ActiveDialog.ManageUsers) },
                                onOpenViewAllGuides = { viewModel.openDialog(ActiveDialog.ViewAllGuides) },
                                onOpenTerms = { viewModel.openDialog(ActiveDialog.Terms) },
                                onExportJson = { viewModel.exportJson(this) },
                                onExportReport = { viewModel.exportReport(this) },
                                onLogout = { viewModel.logout() }
                            )

                            // Render active dialog
                            when (val dialog = activeDialog) {
                                is ActiveDialog.AddEditGuide -> {
                                    AddEditGuideDialog(
                                        initialGuide = dialog.guide,
                                        language = currentLanguage,
                                        onDismiss = { viewModel.closeDialog() },
                                        onSave = {
                                            viewModel.saveGuide(it)
                                            Toast.makeText(this, "✅ Saved", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                                is ActiveDialog.DelegationCard -> {
                                    DelegationCardDialog(
                                        guide = dialog.guide,
                                        language = currentLanguage,
                                        onDismiss = { viewModel.closeDialog() }
                                    )
                                }
                                is ActiveDialog.SecurityCard -> {
                                    SecurityCardDialog(
                                        guide = dialog.guide,
                                        language = currentLanguage,
                                        savedNumbers = savedNumbers,
                                        sendHistory = sendHistory,
                                        onAddSavedNumbers = { viewModel.addSavedNumbers(dialog.guide.id, it) },
                                        onRecordSend = { viewModel.recordSend(dialog.guide.id, it) },
                                        onDismiss = { viewModel.closeDialog() }
                                    )
                                }
                                is ActiveDialog.SafetyOfficer -> {
                                    SafetyOfficerDialog(
                                        guide = dialog.guide,
                                        language = currentLanguage,
                                        onDismiss = { viewModel.closeDialog() }
                                    )
                                }
                                is ActiveDialog.Profile -> {
                                    ProfileDialog(
                                        userProfile = userProfile,
                                        language = currentLanguage,
                                        onDismiss = { viewModel.closeDialog() },
                                        onChangePassword = { viewModel.openDialog(ActiveDialog.Password) },
                                        onSave = { name, sName, sPhone, sPhoto ->
                                            viewModel.updateProfile(name, sName, sPhone, sPhoto)
                                            Toast.makeText(this, "✅ Profile Updated", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                                is ActiveDialog.Password -> {
                                    ChangePasswordDialog(
                                        language = currentLanguage,
                                        onDismiss = { viewModel.closeDialog() },
                                        onSuccess = {
                                            Toast.makeText(this, "🔒 Password Updated", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                                is ActiveDialog.ManageAdmins -> {
                                    ManageAdminsDialog(
                                        adminUids = adminUids,
                                        language = currentLanguage,
                                        onAddAdmin = { viewModel.addAdminUid(it) },
                                        onRemoveAdmin = { viewModel.removeAdminUid(it) },
                                        onDismiss = { viewModel.closeDialog() }
                                    )
                                }
                                is ActiveDialog.ManageUsers -> {
                                    ManageUsersDialog(
                                        users = registeredUsers,
                                        language = currentLanguage,
                                        onCreateUser = {
                                            viewModel.createUser(it)
                                            Toast.makeText(this, "👤 User Created", Toast.LENGTH_SHORT).show()
                                        },
                                        onDismiss = { viewModel.closeDialog() }
                                    )
                                }
                                is ActiveDialog.ViewAllGuides -> {
                                    ViewAllGuidesDialog(
                                        allGuides = allGuides,
                                        language = currentLanguage,
                                        onDismiss = { viewModel.closeDialog() }
                                    )
                                }
                                is ActiveDialog.Terms -> {
                                    TermsDialog(
                                        language = currentLanguage,
                                        onDismiss = { viewModel.closeDialog() }
                                    )
                                }
                                null -> { /* No dialog active */ }
                            }
                        }
                    }
                }
            }
        }
    }
}
