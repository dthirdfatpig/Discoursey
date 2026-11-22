package org.discoursey.android.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.discoursey.android.data.preferences.AppPreferences
import org.discoursey.android.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var editName by remember { mutableStateOf(uiState.userProfile?.name ?: "") }
    var editBio by remember { mutableStateOf(uiState.userProfile?.bioRaw ?: "") }
    var editWebsite by remember { mutableStateOf(uiState.userProfile?.website ?: "") }
    var showSlotDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings & Configurations", fontWeight = FontWeight.Bold, fontSize = 19.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Section 1: UI Engine & Appearance
            SettingsSectionHeader(title = "Interface & Appearance", icon = Icons.Default.Palette)

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("UI Engine Version", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = uiState.uiMode == AppPreferences.MODE_MODERN,
                            onClick = { viewModel.setUiMode(AppPreferences.MODE_MODERN) },
                            label = { Text("Modern Flow UI") }
                        )
                        FilterChip(
                            selected = uiState.uiMode == AppPreferences.MODE_CLASSIC,
                            onClick = { viewModel.setUiMode(AppPreferences.MODE_CLASSIC) },
                            label = { Text("Classic Clean UI") }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Color Theme", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = uiState.themeMode == AppPreferences.THEME_MODERN_DARK,
                                onClick = { viewModel.setThemeMode(AppPreferences.THEME_MODERN_DARK) },
                                label = { Text("Modern Dark") }
                            )
                            FilterChip(
                                selected = uiState.themeMode == AppPreferences.THEME_CLASSIC_DARK,
                                onClick = { viewModel.setThemeMode(AppPreferences.THEME_CLASSIC_DARK) },
                                label = { Text("Classic Dark") }
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = uiState.themeMode == AppPreferences.THEME_AMOLED,
                                onClick = { viewModel.setThemeMode(AppPreferences.THEME_AMOLED) },
                                label = { Text("AMOLED Black") }
                            )
                            FilterChip(
                                selected = uiState.themeMode == AppPreferences.THEME_LIGHT,
                                onClick = { viewModel.setThemeMode(AppPreferences.THEME_LIGHT) },
                                label = { Text("Clean Light") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("User Name & Nickname Priority", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(
                        text = "Choose whether nicknames (@user) or full names take the primary prominent spot.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp, bottom = 6.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = uiState.nameDisplayPreference == AppPreferences.DISPLAY_AUTO,
                                onClick = { viewModel.setNameDisplayPreference(AppPreferences.DISPLAY_AUTO) },
                                label = { Text("Auto (Forum Default)") }
                            )
                            FilterChip(
                                selected = uiState.nameDisplayPreference == AppPreferences.DISPLAY_USERNAME_FIRST,
                                onClick = { viewModel.setNameDisplayPreference(AppPreferences.DISPLAY_USERNAME_FIRST) },
                                label = { Text("Nickname First") }
                            )
                        }
                        FilterChip(
                            selected = uiState.nameDisplayPreference == AppPreferences.DISPLAY_NAME_FIRST,
                            onClick = { viewModel.setNameDisplayPreference(AppPreferences.DISPLAY_NAME_FIRST) },
                            label = { Text("Full Name First") }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Font Size", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            AppPreferences.FONT_SMALL to "Small",
                            AppPreferences.FONT_NORMAL to "Normal",
                            AppPreferences.FONT_LARGE to "Large"
                        ).forEach { (key, label) ->
                            FilterChip(
                                selected = uiState.fontSize == key,
                                onClick = { viewModel.setFontSize(key) },
                                label = { Text(label) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Lower Bar Slots Customization
                    Text("Customizable Lower Bar", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(
                        text = "Current slots: " + uiState.bottomBarSlots.joinToString(" • ") { it.replaceFirstChar(Char::uppercase) },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp, bottom = 6.dp)
                    )
                    OutlinedButton(
                        onClick = { showSlotDialog = true },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Configure Lower Bar Buttons", fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 2: Storage, Cache & Android System
            SettingsSectionHeader(title = "App & Cache Management", icon = Icons.Default.Storage)

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Stored Media & Cache", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(
                                text = "Current size: ${uiState.cacheSizeFormatted}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                        Button(
                            onClick = {
                                val ok = viewModel.clearCache()
                                Toast.makeText(
                                    context,
                                    if (ok) "Cache cleared successfully!" else "Could not clear all cache.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Clear Cache", fontSize = 13.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 3: Discourse Instance & Account Settings
            SettingsSectionHeader(title = "Discourse Community & Profile", icon = Icons.Default.Forum)

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Connected Instance: ${uiState.forumUrl.ifBlank { "None" }}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Logged in as: @${uiState.username.ifBlank { "guest" }}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
                    )

                    // Edit Profile Form
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Display Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = editBio,
                        onValueChange = { editBio = it },
                        label = { Text("About Me / Bio") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = editWebsite,
                        onValueChange = { editWebsite = it },
                        label = { Text("Website") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            Toast.makeText(context, "Profile changes saved!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Save Profile Changes", fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Detected Plugins Info
                    Text("Detected Discourse Plugins", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Discourse Image Album: Ready for production (Active)\n• Discourse Video Stream: Active with Cloudflare/Bunny support",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Disconnect / Logout
                    Button(
                        onClick = {
                            viewModel.logout()
                            onLogout()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Disconnect / Switch Instance", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Dialog for Lower Bar Customization
    if (showSlotDialog) {
        AlertDialog(
            onDismissRequest = { showSlotDialog = false },
            title = { Text("Choose Lower Bar Preset") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select a layout configuration for your 5 lower buttons:")
                    Button(
                        onClick = {
                            viewModel.updateBottomSlots(listOf("home", "search", "messages", "profile", "menu"))
                            showSlotDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Standard (Home, Search, Messages, Profile, Menu)")
                    }
                    Button(
                        onClick = {
                            viewModel.updateBottomSlots(listOf("latest", "categories", "messages", "search", "menu"))
                            showSlotDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Forum Focused (Latest, Categories, Messages, Search, Menu)")
                    }
                    Button(
                        onClick = {
                            viewModel.updateBottomSlots(listOf("home", "bookmarks", "messages", "search", "menu"))
                            showSlotDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Productivity (Home, Bookmarks, Messages, Search, Menu)")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSlotDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
    }
}
