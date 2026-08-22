package com.example.fnotepro.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.fnotepro.data.model.Guide
import com.example.fnotepro.data.model.UserProfile
import com.example.fnotepro.ui.components.GuideCardItem
import com.example.fnotepro.ui.theme.GoldAccent
import com.example.fnotepro.ui.theme.OkGreen
import com.example.fnotepro.ui.theme.OliveDark
import com.example.fnotepro.ui.theme.OlivePrimary
import com.example.fnotepro.ui.theme.SecurityBlue
import com.example.fnotepro.util.AppLanguage
import com.example.fnotepro.util.Localization
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    guides: List<Guide>,
    userProfile: UserProfile,
    isDarkTheme: Boolean,
    currentLanguage: AppLanguage,
    searchQuery: String,
    isDeveloper: Boolean,
    onSearchChange: (String) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onToggleDarkTheme: () -> Unit,
    onAddGuide: () -> Unit,
    onOpenCard: (Guide) -> Unit,
    onOpenSecurity: (Guide) -> Unit,
    onOpenSafetyOfficer: (Guide) -> Unit,
    onEditGuide: (Guide) -> Unit,
    onDeleteGuide: (String) -> Unit,
    onOpenProfile: () -> Unit,
    onOpenManageAdmins: () -> Unit,
    onOpenManageUsers: () -> Unit,
    onOpenViewAllGuides: () -> Unit,
    onOpenTerms: () -> Unit,
    onExportJson: (Context) -> Unit,
    onExportReport: (Context) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var langDropdownExpanded by remember { mutableStateOf(false) }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                onSearchChange(spokenText)
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.width(300.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Profile Header in Drawer
                    Surface(
                        onClick = {
                            scope.launch { drawerState.close() }
                            onOpenProfile()
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(OlivePrimary)
                                    .border(2.dp, GoldAccent, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!userProfile.photoUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = userProfile.photoUrl,
                                        contentDescription = "Avatar",
                                        modifier = Modifier.matchParentSize()
                                    )
                                } else {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = userProfile.displayName.ifBlank { "User" },
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = OlivePrimary
                                )
                                Text(
                                    text = userProfile.email,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = Localization.get("tools_section", currentLanguage),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Share, contentDescription = null, tint = OlivePrimary) },
                        label = { Text(Localization.get("export_json", currentLanguage)) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onExportJson(context)
                        },
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Description, contentDescription = null, tint = OlivePrimary) },
                        label = { Text(Localization.get("export_pdf", currentLanguage)) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onExportReport(context)
                        },
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Info, contentDescription = null, tint = OlivePrimary) },
                        label = { Text(Localization.get("terms", currentLanguage)) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onOpenTerms()
                        },
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                    )

                    // Developer Section
                    if (isDeveloper) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = Localization.get("developer_section", currentLanguage),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecurityBlue
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.Shield, contentDescription = null, tint = SecurityBlue) },
                            label = { Text(Localization.get("manage_admins", currentLanguage)) },
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                                onOpenManageAdmins()
                            },
                            colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                        )

                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.Group, contentDescription = null, tint = SecurityBlue) },
                            label = { Text(Localization.get("manage_users", currentLanguage)) },
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                                onOpenManageUsers()
                            },
                            colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                        )

                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.Description, contentDescription = null, tint = SecurityBlue) },
                            label = { Text(Localization.get("view_all_guides", currentLanguage)) },
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                                onOpenViewAllGuides()
                            },
                            colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.Red) },
                        label = { Text(Localization.get("logout", currentLanguage), color = Color.Red, fontWeight = FontWeight.Bold) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onLogout()
                        },
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(1.5.dp, GoldAccent, CircleShape)
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = Localization.APP_LOGO_URL,
                                    contentDescription = "Logo",
                                    modifier = Modifier.matchParentSize()
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = Localization.get("app_title", currentLanguage),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = "Les Oliviers Palace",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldAccent
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                        }
                    },
                    actions = {
                        // Language Dropdown Action
                        Box {
                            IconButton(onClick = { langDropdownExpanded = true }) {
                                Icon(Icons.Default.Language, contentDescription = "Language", tint = Color.White)
                            }
                            DropdownMenu(
                                expanded = langDropdownExpanded,
                                onDismissRequest = { langDropdownExpanded = false }
                            ) {
                                AppLanguage.values().forEach { lang ->
                                    DropdownMenuItem(
                                        text = { Text(lang.label) },
                                        onClick = {
                                            onLanguageChange(lang)
                                            langDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Theme Toggle Action
                        IconButton(onClick = onToggleDarkTheme) {
                            Icon(
                                if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Theme",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = OlivePrimary)
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onAddGuide,
                    containerColor = OkGreen,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Guide", modifier = Modifier.size(28.dp))
                }
            }
        ) { paddingValues ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Search Bar & Add Button on the same bar (Horizontal flex alignment)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(OlivePrimary)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        placeholder = { Text(Localization.get("search_placeholder", currentLanguage), fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = OlivePrimary) },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { onSearchChange("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            } else {
                                IconButton(
                                    onClick = {
                                        try {
                                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                                putExtra(RecognizerIntent.EXTRA_PROMPT, Localization.get("search_placeholder", currentLanguage))
                                            }
                                            speechLauncher.launch(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Voice search not available", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Mic, contentDescription = "Voice Search", tint = OlivePrimary)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = GoldAccent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                    )

                    // Circular Plus Button for adding items (outside search field, beside mic end)
                    Surface(
                        onClick = onAddGuide,
                        shape = CircleShape,
                        color = OkGreen,
                        shadowElevation = 4.dp,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = Localization.get("add_guide_title", currentLanguage),
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }

                // Stats & Status Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📋 ${guides.size} ${Localization.get("guide_name", currentLanguage)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(OkGreen.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.CloudDone, contentDescription = null, tint = OkGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Online Sync", fontSize = 10.sp, color = OkGreen, fontWeight = FontWeight.Bold)
                    }
                }

                // Guides List or Empty State
                if (guides.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = Localization.get("no_guides", currentLanguage),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(guides, key = { it.id }) { guide ->
                            GuideCardItem(
                                guide = guide,
                                language = currentLanguage,
                                canEdit = isDeveloper || guide.userId == userProfile.uid,
                                onOpenCard = { onOpenCard(guide) },
                                onOpenSecurity = { onOpenSecurity(guide) },
                                onOpenSafetyOfficer = { onOpenSafetyOfficer(guide) },
                                onEdit = { onEditGuide(guide) },
                                onDelete = { onDeleteGuide(guide.id) }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(60.dp))
                        }
                    }
                }
            }
        }
    }
}
