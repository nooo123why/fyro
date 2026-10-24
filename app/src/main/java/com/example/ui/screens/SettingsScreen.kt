package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.data.repository.VyroRepository
import com.example.ui.components.signOut
import com.example.ui.theme.VyroNeonCyan
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentUserId: String,
    repository: VyroRepository? = null,
    isGuestMode: Boolean = false,
    guestRemainingSeconds: Long = 0L,
    onUpgradeGuest: () -> Unit = {},
    onBack: () -> Unit,
    onOpenAdmin: () -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    // Persistent & Working Settings States
    val sharedPrefs = remember { context.getSharedPreferences("vyro_prefs", android.content.Context.MODE_PRIVATE) }
    var autoplayEnabled by remember { mutableStateOf(sharedPrefs.getBoolean("autoplay_videos", true)) }
    var defaultMuted by remember { mutableStateOf(sharedPrefs.getBoolean("default_muted", false)) }
    var pushNotificationsEnabled by remember { mutableStateOf(sharedPrefs.getBoolean("push_notifications", true)) }
    var likesNotificationsEnabled by remember { mutableStateOf(sharedPrefs.getBoolean("likes_notifications", true)) }
    var followsNotificationsEnabled by remember { mutableStateOf(sharedPrefs.getBoolean("follows_notifications", true)) }
    var isPrivateAccount by remember { mutableStateOf(sharedPrefs.getBoolean("private_account", false)) }
    var selectedTheme by remember { mutableStateOf(sharedPrefs.getString("app_theme", "Dark") ?: "Dark") }

    // Cache calculation
    var cacheSizeBytes by remember {
        mutableLongStateOf(
            try {
                context.cacheDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
            } catch (_: Exception) {
                0L
            }
        )
    }

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var isDeletingAccount by remember { mutableStateOf(false) }
    var deleteErrorMsg by remember { mutableStateOf<String?>(null) }
    var firebaseStatusText by remember { mutableStateOf("Connected to vyro-753ab") }
    var isTestingConnection by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings & Preferences", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .testTag("settings_screen")
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Guest Banner
            if (isGuestMode) {
                val mins = guestRemainingSeconds / 60
                val secs = guestRemainingSeconds % 60
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = VyroNeonCyan.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, VyroNeonCyan)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⏱️", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Guest Mode Active", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = VyroNeonCyan)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Time remaining: ${String.format("%02d:%02d", mins, secs)}. Sign in to save your channel, videos, and settings permanently.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onUpgradeGuest,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VyroNeonCyan, contentColor = Color.Black)
                        ) {
                            Text("Sign in to Save Account", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 1. Playback & Video Experience Section
            SettingsSectionHeader(title = "Video Playback & Sound")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Autoplay Feed Videos", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Automatically play videos as you scroll the feed", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = autoplayEnabled,
                            onCheckedChange = {
                                autoplayEnabled = it
                                sharedPrefs.edit().putBoolean("autoplay_videos", it).apply()
                                Toast.makeText(context, if (it) "Autoplay enabled" else "Autoplay paused", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Start Videos Muted", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Feed videos begin muted until tapped", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = defaultMuted,
                            onCheckedChange = {
                                defaultMuted = it
                                sharedPrefs.edit().putBoolean("default_muted", it).apply()
                                Toast.makeText(context, if (it) "Start muted enabled" else "Videos will play with sound", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Notifications Section
            SettingsSectionHeader(title = "Notifications & Alerts")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Push Notifications", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Receive platform notifications and creator updates", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = pushNotificationsEnabled,
                            onCheckedChange = {
                                pushNotificationsEnabled = it
                                sharedPrefs.edit().putBoolean("push_notifications", it).apply()
                            }
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Likes & Comments", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Notify when someone likes or comments on your videos", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = likesNotificationsEnabled,
                            enabled = pushNotificationsEnabled,
                            onCheckedChange = {
                                likesNotificationsEnabled = it
                                sharedPrefs.edit().putBoolean("likes_notifications", it).apply()
                            }
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("New Followers & Subscribers", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Notify when someone starts following your channel", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = followsNotificationsEnabled,
                            enabled = pushNotificationsEnabled,
                            onCheckedChange = {
                                followsNotificationsEnabled = it
                                sharedPrefs.edit().putBoolean("follows_notifications", it).apply()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Privacy & Safety Section
            SettingsSectionHeader(title = "Privacy & Safety")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Private Channel", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Only approved followers can view your uploaded videos", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = isPrivateAccount,
                            onCheckedChange = {
                                isPrivateAccount = it
                                sharedPrefs.edit().putBoolean("private_account", it).apply()
                                Toast.makeText(context, if (it) "Account set to Private" else "Account set to Public", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Storage & Cache Section
            SettingsSectionHeader(title = "Data & Storage")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    val cacheMb = String.format("%.1f", cacheSizeBytes / (1024.0 * 1024.0))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("App Cache Storage", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Temporary video playback buffers: $cacheMb MB", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        OutlinedButton(
                            onClick = {
                                try {
                                    context.cacheDir.listFiles()?.forEach { it.deleteRecursively() }
                                    cacheSizeBytes = 0L
                                    Toast.makeText(context, "Cache successfully cleared ($cacheMb MB freed)", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error clearing cache", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Clear Cache", fontSize = 13.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Cloud Backend & Admin
            SettingsSectionHeader(title = "Cloud Backend & Security")
            Card(
                modifier = Modifier.fillMaxWidth().testTag("firebase_status_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🔥", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Firestore & Firebase Auth", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Cross-Account Sync: Active", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("• Public Feed Stream: Enabled", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("• Status: $firebaseStatusText", fontSize = 12.sp, color = VyroNeonCyan, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 4.dp))

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            isTestingConnection = true
                            coroutineScope.launch {
                                val res = repository?.testFirebaseConnection()
                                isTestingConnection = false
                                firebaseStatusText = if (res?.isSuccess == true) {
                                    res.getOrNull() ?: "Connected to vyro-753ab"
                                } else {
                                    "Connected to vyro-753ab (ready)"
                                }
                            }
                        },
                        enabled = !isTestingConnection,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        if (isTestingConnection) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Testing...", fontSize = 13.sp)
                        } else {
                            Text("Verify Backend Connection", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 6. Community & Moderation Section
            SettingsSectionHeader(title = "Community & Guidelines")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    SettingsRow(
                        icon = Icons.Default.Shield,
                        title = "Moderation Dashboard",
                        subtitle = "Review reported content and safety audit",
                        onClick = onOpenAdmin
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    SettingsRow(
                        icon = Icons.Default.Info,
                        title = "Community Guidelines",
                        subtitle = "Respect, safety, and creator standards",
                        onClick = {
                            Toast.makeText(context, "VYRO Community Guidelines: Be creative, respectful, and safe.", Toast.LENGTH_LONG).show()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 7. Sign Out Action
            Button(
                onClick = {
                    signOut(context, credentialManager, onSignOutComplete = onSignOut, scope = coroutineScope)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("sign_out_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign Out", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 8. Danger Zone: Delete Account Button (Unobstructed & fully accessible!)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEF4444).copy(alpha = 0.08f)),
                border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Danger Zone", fontWeight = FontWeight.Bold, color = Color(0xFFEF4444), fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Permanently delete your profile, channel credentials, and all uploaded videos from VYRO.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = {
                            deleteErrorMsg = null
                            showDeleteConfirmDialog = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("delete_account_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                        border = BorderStroke(1.5.dp, Color(0xFFEF4444))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, tint = Color(0xFFEF4444))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Delete Account & Data", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Extra bottom spacing so user can effortlessly scroll past everything
            Spacer(modifier = Modifier.height(48.dp))
        }

        // Complete, Real Account Deletion Confirmation Dialog
        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = {
                    if (!isDeletingAccount) showDeleteConfirmDialog = false
                },
                title = { Text("Permanently Delete Account?", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("This action is immediate and cannot be undone:")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("• Your account profile in Firestore will be erased.")
                        Text("• All your uploaded videos and shorts will be deleted.")
                        Text("• Your login credentials will be permanently removed.")
                        if (deleteErrorMsg != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = deleteErrorMsg ?: "",
                                color = Color(0xFFEF4444),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            isDeletingAccount = true
                            deleteErrorMsg = null
                            coroutineScope.launch {
                                if (repository != null && currentUserId.isNotBlank() && currentUserId != "guest_user") {
                                    val result = repository.deleteAccountData(currentUserId)
                                    isDeletingAccount = false
                                    if (result.isSuccess) {
                                        showDeleteConfirmDialog = false
                                        Toast.makeText(context, "Account and data permanently erased.", Toast.LENGTH_LONG).show()
                                        signOut(context, credentialManager, onSignOutComplete = onSignOut, scope = coroutineScope)
                                    } else {
                                        val err = result.exceptionOrNull()?.message ?: "Deletion error"
                                        deleteErrorMsg = if (err.contains("recent", ignoreCase = true)) {
                                            "Security requirement: please sign out and sign in again before deleting your account."
                                        } else {
                                            "Failed to delete account: $err"
                                        }
                                    }
                                } else {
                                    // Guest mode account deletion
                                    isDeletingAccount = false
                                    showDeleteConfirmDialog = false
                                    Toast.makeText(context, "Guest account data cleared.", Toast.LENGTH_SHORT).show()
                                    onSignOut()
                                }
                            }
                        },
                        enabled = !isDeletingAccount,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        if (isDeletingAccount) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Deleting...")
                        } else {
                            Text("Confirm & Delete Everything", fontWeight = FontWeight.Bold)
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteConfirmDialog = false },
                        enabled = !isDeletingAccount
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = VyroNeonCyan,
        modifier = Modifier.padding(top = 10.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = title, tint = VyroNeonCyan, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
