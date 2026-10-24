package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.data.repository.VyroRepository
import com.example.data.util.GuestSessionManager
import com.example.ui.components.AuthScreen
import com.example.ui.components.GoogleSignInButton
import com.example.ui.components.VyroBottomBar
import com.example.ui.components.VyroTopBar
import com.example.ui.navigation.BottomTab
import com.example.ui.navigation.Screen
import com.example.ui.screens.*
import com.example.ui.theme.VYROTheme
import com.example.ui.theme.VyroNeonCyan
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            VYROTheme {
                VyroMainGate()
            }
        }
    }
}

@Composable
fun VyroMainGate() {
    val context = LocalContext.current
    val guestManager = remember { GuestSessionManager(context) }

    var currentUser by remember { mutableStateOf<FirebaseUser?>(Firebase.auth.currentUser) }
    var isGuestMode by remember { mutableStateOf(guestManager.isGuestSessionValid()) }
    var guestRemainingSeconds by remember { mutableLongStateOf(guestManager.getRemainingSeconds()) }
    var showGuestExpiredDialog by remember { mutableStateOf(false) }
    var showUpgradeDialog by remember { mutableStateOf(false) }

    // Listen to Firebase auth state
    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
            if (auth.currentUser != null) {
                // If signed in, end guest session
                guestManager.endGuestSession()
                isGuestMode = false
            }
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    // 1-Hour Guest Session Countdown Timer Loop
    LaunchedEffect(isGuestMode) {
        if (isGuestMode) {
            while (guestManager.isGuestSessionValid()) {
                delay(1000L)
                guestManager.updateRemainingTime()
                guestRemainingSeconds = guestManager.getRemainingSeconds()
            }
            // 1 Hour elapsed!
            guestManager.endGuestSession()
            isGuestMode = false
            showGuestExpiredDialog = true
        }
    }

    // Guest Session Expired Modal
    if (showGuestExpiredDialog) {
        AlertDialog(
            onDismissRequest = { showGuestExpiredDialog = false },
            title = {
                Text(
                    text = "⏱️ Guest Trial Expired (1 Hour Ended)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Your 1-hour guest exploration of VYRO has completed. Sign in with Email, Gmail, or Google to keep watching videos and creating content!",
                        fontSize = 14.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showGuestExpiredDialog = false
                        guestManager.endGuestSession()
                        isGuestMode = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Go to Sign In")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGuestExpiredDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Upgrade Guest Session Dialog (When user clicks sign in from guest mode)
    if (showUpgradeDialog) {
        AlertDialog(
            onDismissRequest = { showUpgradeDialog = false },
            title = { Text("Sign in to VYRO", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Sign in with your Email, Gmail, or Google account to save your profile, videos, and chats permanently.")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showUpgradeDialog = false
                        guestManager.endGuestSession()
                        isGuestMode = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Sign In / Register")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUpgradeDialog = false }) {
                    Text("Continue as Guest")
                }
            }
        )
    }

    if (currentUser == null && !isGuestMode) {
        // Welcome / Sign In Screen with Guest Mode option
        AuthScreen(
            onAuthSuccess = {
                currentUser = Firebase.auth.currentUser
                guestManager.endGuestSession()
                isGuestMode = false
            },
            onGuestModeSelected = {
                guestManager.startGuestSession()
                isGuestMode = true
                guestRemainingSeconds = 3600L
            }
        )
    } else {
        // Active session: Either Google authenticated user OR 1-Hour Guest Session
        val repository = if (currentUser != null) {
            remember(currentUser!!.uid) { VyroRepository(context) }
        } else {
            remember { VyroRepository(context) }
        }

        VyroApp(
            currentUser = currentUser,
            isGuestMode = isGuestMode,
            guestRemainingSeconds = guestRemainingSeconds,
            guestManager = guestManager,
            repository = repository,
            onUpgradeGuest = { showUpgradeDialog = true },
            onSignOut = {
                if (isGuestMode) {
                    guestManager.endGuestSession()
                    isGuestMode = false
                } else {
                    currentUser = null
                }
            }
        )
    }
}

@Composable
fun VyroApp(
    currentUser: FirebaseUser?,
    isGuestMode: Boolean,
    guestRemainingSeconds: Long,
    guestManager: GuestSessionManager,
    repository: VyroRepository,
    onUpgradeGuest: () -> Unit,
    onSignOut: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var currentTab by remember { mutableStateOf(BottomTab.HOME) }
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    val effectiveUserId = currentUser?.uid ?: "guest_user"
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }

    LaunchedEffect(effectiveUserId, isGuestMode) {
        if (isGuestMode) {
            userProfile = UserProfile(
                userId = "guest_user",
                username = guestManager.getGuestUsername(),
                displayName = guestManager.getGuestDisplayName(),
                avatarUrl = guestManager.getGuestAvatar(),
                bio = guestManager.getGuestBio(),
                followersCount = 42L,
                followingCount = 18L,
                postsCount = 3L
            )
        } else if (currentUser != null) {
            coroutineScope.launch {
                val existing = repository.getProfile(currentUser.uid)
                if (existing != null) {
                    userProfile = existing
                } else {
                    val initialUsername = (currentUser.email?.substringBefore("@") ?: "user_${currentUser.uid.take(5)}")
                        .filter { it.isLetterOrDigit() || it == '_' }
                        .take(20)
                        .ifBlank { "user_${currentUser.uid.take(5)}" }

                    val newProfile = UserProfile(
                        userId = currentUser.uid,
                        username = initialUsername,
                        displayName = currentUser.displayName ?: initialUsername,
                        avatarUrl = currentUser.photoUrl?.toString() ?: "",
                        bio = "Creating on VYRO ⚡",
                        followersCount = 0L,
                        followingCount = 0L,
                        postsCount = 0L,
                        isPrivate = false
                    )
                    repository.saveProfile(newProfile)
                    userProfile = newProfile
                }
            }
        }
    }

    val username = userProfile?.username ?: if (isGuestMode) "guest_vyro" else "user"
    val avatarUrl = userProfile?.avatarUrl ?: if (isGuestMode) "https://picsum.photos/seed/guest_avatar/300" else (currentUser?.photoUrl?.toString() ?: "")

    // Back handling for sub-screens
    BackHandler(enabled = currentScreen !is Screen.Home && currentScreen !is Screen.Shorts && currentScreen !is Screen.Subscriptions && currentScreen !is Screen.Profile) {
        currentScreen = when (currentTab) {
            BottomTab.HOME -> Screen.Home
            BottomTab.SHORTS -> Screen.Shorts
            BottomTab.CREATE -> Screen.Create
            BottomTab.SUBSCRIPTIONS -> Screen.Subscriptions
            BottomTab.PROFILE -> Screen.Profile
        }
    }

    val showBottomBar = currentScreen !is Screen.Call && currentScreen !is Screen.StoryView && currentScreen !is Screen.Chat && currentScreen !is Screen.VideoDetail && currentScreen !is Screen.Settings && currentScreen !is Screen.Game && currentScreen !is Screen.AdminDashboard
    val showTopBar = currentScreen is Screen.Profile

    Scaffold(
        topBar = {
            if (showTopBar) {
                VyroTopBar(
                    title = "Profile",
                    isGuestMode = isGuestMode,
                    guestRemainingSeconds = guestRemainingSeconds,
                    onGuestUpgradeClick = onUpgradeGuest,
                    onNotificationsClick = { currentScreen = Screen.Notifications },
                    onSettingsClick = { currentScreen = Screen.Settings }
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                VyroBottomBar(
                    selectedTab = currentTab,
                    onTabSelected = { tab ->
                        currentTab = tab
                        currentScreen = when (tab) {
                            BottomTab.HOME -> Screen.Home
                            BottomTab.SHORTS -> Screen.Shorts
                            BottomTab.CREATE -> Screen.Create
                            BottomTab.SUBSCRIPTIONS -> Screen.Subscriptions
                            BottomTab.PROFILE -> Screen.Profile
                        }
                    }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (showTopBar) innerPadding else androidx.compose.foundation.layout.PaddingValues())
        ) {
            when (val screen = currentScreen) {
                is Screen.Home, is Screen.Feed -> {
                    HomeScreen(
                        repository = repository,
                        currentUserId = effectiveUserId,
                        currentUsername = username,
                        currentAvatarUrl = avatarUrl,
                        onOpenVideoDetail = { post -> currentScreen = Screen.VideoDetail(post) },
                        onOpenShortsTab = {
                            currentTab = BottomTab.SHORTS
                            currentScreen = Screen.Shorts
                        },
                        onOpenCreator = { creatorId ->
                            if (creatorId == effectiveUserId) {
                                currentTab = BottomTab.PROFILE
                                currentScreen = Screen.Profile
                            } else {
                                currentScreen = Screen.CreatorProfile(creatorId)
                            }
                        },
                        onOpenCreate = {
                            currentTab = BottomTab.CREATE
                            currentScreen = Screen.Create
                        },
                        onOpenGame = {
                            currentScreen = Screen.Game
                        }
                    )
                }

                is Screen.Shorts -> {
                    ShortsScreen(
                        repository = repository,
                        currentUserId = effectiveUserId,
                        currentUsername = username,
                        currentAvatarUrl = avatarUrl,
                        onOpenCreator = { creatorId ->
                            if (creatorId == effectiveUserId) {
                                currentTab = BottomTab.PROFILE
                                currentScreen = Screen.Profile
                            } else {
                                currentScreen = Screen.CreatorProfile(creatorId)
                            }
                        },
                        onOpenCreate = {
                            currentTab = BottomTab.CREATE
                            currentScreen = Screen.Create
                        }
                    )
                }

                is Screen.VideoDetail -> {
                    VideoDetailScreen(
                        post = screen.post,
                        repository = repository,
                        currentUserId = effectiveUserId,
                        currentUsername = username,
                        currentAvatarUrl = avatarUrl,
                        onBack = { currentScreen = Screen.Home },
                        onOpenCreator = { creatorId ->
                            if (creatorId == effectiveUserId) {
                                currentTab = BottomTab.PROFILE
                                currentScreen = Screen.Profile
                            } else {
                                currentScreen = Screen.CreatorProfile(creatorId)
                            }
                        },
                        onSelectRelatedVideo = { relPost ->
                            currentScreen = Screen.VideoDetail(relPost)
                        }
                    )
                }

                is Screen.Subscriptions, is Screen.Discover -> {
                    SubscriptionsScreen(
                        repository = repository,
                        currentUserId = effectiveUserId,
                        onOpenVideo = { post -> currentScreen = Screen.VideoDetail(post) },
                        onOpenCreator = { creatorId ->
                            if (creatorId == effectiveUserId) {
                                currentTab = BottomTab.PROFILE
                                currentScreen = Screen.Profile
                            } else {
                                currentScreen = Screen.CreatorProfile(creatorId)
                            }
                        },
                        onOpenChat = { targetId, targetUsername ->
                            currentScreen = Screen.Chat(
                                conversationId = "conv_${listOf(effectiveUserId, targetId).sorted().joinToString("_")}",
                                title = targetUsername,
                                memberUids = listOf(effectiveUserId, targetId)
                            )
                        }
                    )
                }

                is Screen.Create -> {
                    CreateScreen(
                        repository = repository,
                        currentUsername = username,
                        currentAvatarUrl = avatarUrl,
                        isGuestMode = isGuestMode,
                        onUpgradeGuest = onUpgradeGuest,
                        onPublishSuccess = {
                            currentTab = BottomTab.HOME
                            currentScreen = Screen.Home
                        }
                    )
                }

                is Screen.Inbox -> {
                    InboxScreen(
                        repository = repository,
                        currentUserId = effectiveUserId,
                        onOpenChat = { convId, title, members ->
                            currentScreen = Screen.Chat(convId, title, members)
                        },
                        onOpenCalls = {
                            currentScreen = Screen.Call("Recent Call", false)
                        }
                    )
                }

                is Screen.Profile -> {
                    ProfileScreen(
                        userId = effectiveUserId,
                        currentUserId = effectiveUserId,
                        repository = repository,
                        isGuestMode = isGuestMode,
                        guestRemainingSeconds = guestRemainingSeconds,
                        guestDisplayName = userProfile?.displayName ?: "Guest Explorer",
                        guestAvatarUrl = userProfile?.avatarUrl ?: "https://picsum.photos/seed/guest_avatar/300",
                        guestBio = userProfile?.bio ?: "Exploring VYRO in 1-Hour Guest Mode ⚡",
                        onOpenSettings = { currentScreen = Screen.Settings },
                        onNavigateToCreate = {
                            currentTab = BottomTab.CREATE
                            currentScreen = Screen.Create
                        },
                        onUpgradeGuest = onUpgradeGuest
                    )
                }

                is Screen.CreatorProfile -> {
                    ProfileScreen(
                        userId = screen.userId,
                        currentUserId = effectiveUserId,
                        repository = repository,
                        isGuestMode = isGuestMode,
                        guestRemainingSeconds = guestRemainingSeconds,
                        onOpenSettings = { currentScreen = Screen.Settings },
                        onNavigateToCreate = {
                            currentTab = BottomTab.CREATE
                            currentScreen = Screen.Create
                        },
                        onUpgradeGuest = onUpgradeGuest,
                        onBack = {
                            currentScreen = when (currentTab) {
                                BottomTab.HOME -> Screen.Home
                                BottomTab.SHORTS -> Screen.Shorts
                                BottomTab.CREATE -> Screen.Create
                                BottomTab.SUBSCRIPTIONS -> Screen.Subscriptions
                                BottomTab.PROFILE -> Screen.Profile
                            }
                        },
                        onOpenChat = { targetId, targetName, _ ->
                            currentScreen = Screen.Chat(
                                conversationId = "conv_${listOf(effectiveUserId, targetId).sorted().joinToString("_")}",
                                title = targetName,
                                memberUids = listOf(effectiveUserId, targetId)
                            )
                        }
                    )
                }

                is Screen.Chat -> {
                    ChatScreen(
                        conversationId = screen.conversationId,
                        title = screen.title,
                        memberUids = screen.memberUids,
                        currentUserId = effectiveUserId,
                        currentUsername = username,
                        repository = repository,
                        onBack = { currentScreen = Screen.Subscriptions },
                        onStartCall = { target, isVideo ->
                            currentScreen = Screen.Call(target, isVideo)
                        }
                    )
                }

                is Screen.Call -> {
                    CallScreen(
                        targetName = screen.targetName,
                        isVideo = screen.isVideo,
                        onEndCall = { currentScreen = Screen.Inbox }
                    )
                }

                is Screen.StoryView -> {
                    StoryViewerScreen(
                        repository = repository,
                        initialIndex = screen.initialIndex,
                        onClose = { currentScreen = Screen.Feed }
                    )
                }

                is Screen.Notifications -> {
                    NotificationsScreen(
                        currentUserId = effectiveUserId,
                        repository = repository,
                        onBack = { currentScreen = Screen.Feed }
                    )
                }

                is Screen.Settings -> {
                    SettingsScreen(
                        currentUserId = effectiveUserId,
                        repository = repository,
                        isGuestMode = isGuestMode,
                        guestRemainingSeconds = guestRemainingSeconds,
                        onUpgradeGuest = onUpgradeGuest,
                        onBack = { currentScreen = Screen.Profile },
                        onOpenAdmin = { currentScreen = Screen.AdminDashboard },
                        onSignOut = onSignOut
                    )
                }

                is Screen.AdminDashboard -> {
                    AdminDashboardScreen(
                        repository = repository,
                        onBack = { currentScreen = Screen.Settings }
                    )
                }

                is Screen.Game -> {
                    GameScreen(
                        onBack = { currentScreen = Screen.Home }
                    )
                }
            }
        }
    }
}
