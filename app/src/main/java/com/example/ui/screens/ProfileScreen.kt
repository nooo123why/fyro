package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Post
import com.example.data.model.UserProfile
import com.example.data.repository.VyroRepository
import com.example.ui.theme.VyroAccentPink
import com.example.ui.theme.VyroElectricPurple
import com.example.ui.theme.VyroNeonCyan
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userId: String,
    currentUserId: String,
    repository: VyroRepository?,
    isGuestMode: Boolean = false,
    guestRemainingSeconds: Long = 0L,
    guestDisplayName: String = "Guest Explorer",
    guestAvatarUrl: String = "https://picsum.photos/seed/guest_avatar/300",
    guestBio: String = "Exploring VYRO in 1-Hour Guest Mode ⚡",
    onOpenSettings: () -> Unit,
    onNavigateToCreate: () -> Unit = {},
    onUpgradeGuest: () -> Unit = {},
    onBack: (() -> Unit)? = null,
    onOpenChat: ((String, String, List<String>) -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    var userPosts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var selectedViewMode by remember { mutableIntStateOf(0) } // 0: Video List, 1: Video Grid, 2: Bookmarks
    var showEditProfileSheet by remember { mutableStateOf(false) }
    var selectedVideoForPlayback by remember { mutableStateOf<Post?>(null) }
    var showAvatarDialog by remember { mutableStateOf(false) }

    val isSelf = (userId == currentUserId && !isGuestMode) || (isGuestMode && userId == "guest_user")

    // Load profile and user's uploaded videos (real user posts only)
    LaunchedEffect(userId, isGuestMode) {
        if (isGuestMode) {
            userProfile = UserProfile(
                userId = "guest_user",
                username = "guest_vyro",
                displayName = guestDisplayName,
                bio = guestBio,
                avatarUrl = guestAvatarUrl,
                followersCount = 0L,
                followingCount = 0L,
                postsCount = 0L,
                isPrivate = false
            )
            if (repository != null) {
                coroutineScope.launch {
                    repository.observePosts().collect { allPosts ->
                        userPosts = allPosts.filter { it.authorId == "guest_user" }
                        userProfile = userProfile?.copy(postsCount = userPosts.size.toLong())
                    }
                }
            }
        } else if (repository != null) {
            coroutineScope.launch {
                val p = repository.getProfile(userId)
                userProfile = p ?: UserProfile(
                    userId = userId,
                    username = "creator_${userId.take(5)}",
                    displayName = "VYRO Creator",
                    bio = "Creating moments on VYRO ⚡",
                    followersCount = 0L,
                    followingCount = 0L,
                    postsCount = 0L
                )
            }
            coroutineScope.launch {
                repository.observePosts().collect { allPosts ->
                    userPosts = allPosts.filter { it.authorId == userId }
                }
            }
        }
    }

    val displayName = userProfile?.displayName?.ifBlank { "VYRO Creator" } ?: "VYRO Creator"
    val avatarUrl = userProfile?.avatarUrl?.ifBlank { "https://picsum.photos/seed/${userProfile?.username}/300" }
        ?: "https://picsum.photos/seed/user/300"

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.testTag("profile_screen")
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // Top Profile Header (Avatar, Display Name, Bio, Stats)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (onBack != null) {
                            IconButton(onClick = onBack, modifier = Modifier.testTag("profile_back_button")) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.size(48.dp))
                        }

                        if (isSelf) {
                            IconButton(onClick = onOpenSettings, modifier = Modifier.testTag("profile_settings_button")) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.size(48.dp))
                        }
                    }

                    // Avatar with Gradient Ring
                    Box(
                        modifier = Modifier
                            .size(104.dp)
                            .clip(CircleShape)
                            .border(3.dp, Brush.linearGradient(listOf(VyroElectricPurple, VyroNeonCyan)), CircleShape)
                            .padding(4.dp)
                            .clip(CircleShape)
                            .clickable { showAvatarDialog = true }
                            .testTag("profile_avatar_image")
                    ) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = "User Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Display Name
                    Text(
                        text = displayName,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Username handle
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "@${userProfile?.username ?: "creator"}",
                            fontSize = 14.sp,
                            color = VyroNeonCyan,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verified",
                            tint = VyroNeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Bio
                    if (!userProfile?.bio.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = userProfile!!.bio,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Follower & Video Counters
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ProfileStatItem(count = "${userProfile?.followersCount ?: 0}", label = "Followers")
                        Divider(modifier = Modifier.height(28.dp).width(1.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        ProfileStatItem(count = "${userProfile?.followingCount ?: 0}", label = "Following")
                        Divider(modifier = Modifier.height(28.dp).width(1.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        ProfileStatItem(count = "${userPosts.size}", label = "Videos")
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (isSelf) {
                            OutlinedButton(
                                onClick = { showEditProfileSheet = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("edit_profile_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Edit Profile", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            if (onOpenChat != null) {
                                Button(
                                    onClick = {
                                        onOpenChat("creator_zaid", "TheDigitalDonZaid", listOf(currentUserId, "creator_zaid"))
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .testTag("profile_messages_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = VyroAccentPink)
                                ) {
                                    Icon(imageVector = Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Messages", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        } else {
                            val followingIds by repository?.followingIds?.collectAsState(initial = emptySet()) ?: remember { mutableStateOf(emptySet()) }
                            val isFollowing = followingIds.contains(userId)

                            Button(
                                onClick = {
                                    if (repository != null) {
                                        coroutineScope.launch {
                                            repository.toggleFollow(userId, userProfile?.username ?: "")
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("profile_follow_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isFollowing) MaterialTheme.colorScheme.surfaceVariant else VyroNeonCyan,
                                    contentColor = if (isFollowing) MaterialTheme.colorScheme.onSurface else Color.Black
                                )
                            ) {
                                Text(if (isFollowing) "Following ✓" else "Follow", fontWeight = FontWeight.Bold)
                            }

                            if (onOpenChat != null) {
                                OutlinedButton(
                                    onClick = {
                                        if (!isFollowing && repository != null) {
                                            // Automatically follow when tapping message to satisfy rule
                                            coroutineScope.launch {
                                                repository.toggleFollow(userId, userProfile?.username ?: "")
                                            }
                                        }
                                        val convId = "dm_${listOf(currentUserId, userId).sorted().joinToString("_")}"
                                        val creatorTitle = userProfile?.displayName?.ifBlank { userProfile?.username } ?: "Creator"
                                        onOpenChat(convId, creatorTitle, listOf(currentUserId, userId))
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp)
                                        .testTag("profile_message_button"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isFollowing) Icons.Default.Chat else Icons.Default.Lock,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isFollowing) "Message" else "Follow to Chat", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Section Heading: Uploaded Videos
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Uploaded Videos (${userPosts.size})",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        // View mode toggle: Detailed List vs Grid
                        Row {
                            IconButton(onClick = { selectedViewMode = 0 }) {
                                Icon(
                                    imageVector = Icons.Default.ViewList,
                                    contentDescription = "List View",
                                    tint = if (selectedViewMode == 0) VyroNeonCyan else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { selectedViewMode = 1 }) {
                                Icon(
                                    imageVector = Icons.Default.GridOn,
                                    contentDescription = "Grid View",
                                    tint = if (selectedViewMode == 1) VyroNeonCyan else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Uploaded Videos Content: List Mode
            if (selectedViewMode == 0) {
                if (userPosts.isEmpty()) {
                    item {
                        EmptyVideosPlaceholder(onUpload = onNavigateToCreate)
                    }
                } else {
                    items(userPosts, key = { it.id.ifBlank { it.mediaUrl } }) { post ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                            UploadedVideoListItem(
                                post = post,
                                onClick = { selectedVideoForPlayback = post }
                            )
                        }
                    }
                }
            } else {
                // Uploaded Videos Content: Grid Mode
                item {
                    if (userPosts.isEmpty()) {
                        EmptyVideosPlaceholder(onUpload = onNavigateToCreate)
                    } else {
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            val rows = userPosts.chunked(3)
                            rows.forEach { rowPosts ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    rowPosts.forEach { post ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(0.75f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                                .clickable { selectedVideoForPlayback = post }
                                        ) {
                                            AsyncImage(
                                                model = post.mediaUrl,
                                                contentDescription = post.caption,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(
                                                        Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f)))
                                                    )
                                            )
                                            Row(
                                                modifier = Modifier
                                                    .align(Alignment.BottomStart)
                                                    .padding(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PlayArrow,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text(
                                                    text = "${post.likesCount}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                    // Filler for 3-column alignment if last row has fewer items
                                    repeat(3 - rowPosts.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Full Video Viewer Dialog
    if (selectedVideoForPlayback != null) {
        VideoDetailDialog(
            post = selectedVideoForPlayback!!,
            onDismiss = { selectedVideoForPlayback = null }
        )
    }

    // Avatar preview dialog
    if (showAvatarDialog) {
        AlertDialog(
            onDismissRequest = { showAvatarDialog = false },
            title = { Text(displayName, fontWeight = FontWeight.Bold) },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = "Full Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showAvatarDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Edit Profile Modal Bottom Sheet
    if (showEditProfileSheet && userProfile != null) {
        EditProfileBottomSheet(
            initialProfile = userProfile!!,
            onSave = { updated ->
                userProfile = updated
                showEditProfileSheet = false
                if (!isGuestMode && repository != null) {
                    coroutineScope.launch {
                        repository.saveProfile(updated)
                    }
                }
            },
            onDismiss = { showEditProfileSheet = false }
        )
    }
}

@Composable
fun UploadedVideoListItem(
    post: Post,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("uploaded_video_item_${post.id}"),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Video Thumbnail
            Box(
                modifier = Modifier
                    .size(width = 80.dp, height = 110.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = post.mediaUrl,
                    contentDescription = post.caption,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Center play overlay icon
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play video",
                        tint = VyroNeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Video Details (Caption, Audio, Likes, Comments)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = post.caption.ifBlank { "Untitled Video" },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = VyroNeonCyan,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = post.audioTitle,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Stats row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Favorite, contentDescription = null, tint = VyroAccentPink, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "${post.likesCount}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.ModeComment, contentDescription = null, tint = VyroNeonCyan, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "${post.commentsCount}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                    }

                    Text(
                        text = "9:16 HD",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyVideosPlaceholder(onUpload: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Videocam,
            contentDescription = null,
            tint = VyroNeonCyan,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "No videos uploaded yet",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Record or select a video to share with your audience on VYRO.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onUpload,
            colors = ButtonDefaults.buttonColors(containerColor = VyroElectricPurple),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Upload Video Now")
        }
    }
}

@Composable
fun VideoDetailDialog(
    post: Post,
    onDismiss: () -> Unit
) {
    var isPlaying by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = post.caption.take(24) + if (post.caption.length > 24) "..." else "",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black)
                ) {
                    com.example.ui.components.VideoPlayerItem(
                        mediaUrl = post.mediaUrl,
                        thumbnailUrl = post.thumbnailUrl,
                        isPlaying = isPlaying,
                        onTogglePlay = { isPlaying = !isPlaying },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = post.caption,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Text("❤️ ${post.likesCount} Likes", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("💬 ${post.commentsCount} Comments", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun ProfileStatItem(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileBottomSheet(
    initialProfile: UserProfile,
    onSave: (UserProfile) -> Unit,
    onDismiss: () -> Unit
) {
    var displayName by remember { mutableStateOf(initialProfile.displayName) }
    var username by remember { mutableStateOf(initialProfile.username) }
    var bio by remember { mutableStateOf(initialProfile.bio) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = "Edit Profile",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("Display Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_display_name_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username Handle") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_username_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Bio") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_bio_input"),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    onSave(
                        initialProfile.copy(
                            displayName = displayName,
                            username = username,
                            bio = bio
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_profile_button"),
                colors = ButtonDefaults.buttonColors(containerColor = VyroNeonCyan, contentColor = Color.Black)
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        }
    }
}

