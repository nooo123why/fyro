package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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

@Composable
fun DiscoverScreen(
    repository: VyroRepository,
    currentUserId: String = "",
    onOpenCreator: (String) -> Unit,
    onOpenChat: ((String, String, List<String>) -> Unit)? = null,
    onOpenCreate: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0: Creators, 1: Videos, 2: Hashtags
    var selectedTag by remember { mutableStateOf("All") }

    var creatorResults by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    var allPosts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var isLoadingCreators by remember { mutableStateOf(false) }

    val tabs = listOf("Creators", "Videos", "Hashtags")

    // Collect real posts live
    LaunchedEffect(Unit) {
        repository.observePosts().collect { list ->
            allPosts = list
        }
    }

    // Query creators when searchQuery changes or on initial launch
    LaunchedEffect(searchQuery) {
        isLoadingCreators = true
        creatorResults = repository.searchUsers(searchQuery)
        isLoadingCreators = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .testTag("discover_screen")
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar with neon border
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search creators, videos, hashtags...", fontSize = 14.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = VyroNeonCyan
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }, modifier = Modifier.testTag("discover_clear_search")) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("discover_search_bar"),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VyroNeonCyan,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Multi-Tab Selection
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = VyroNeonCyan,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = VyroNeonCyan,
                    height = 3.dp
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("discover_tabs")
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTabIndex == index
                Tab(
                    selected = isSelected,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp,
                            color = if (isSelected) VyroNeonCyan else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.testTag("discover_tab_$title")
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tab Content
        when (selectedTabIndex) {
            0 -> {
                // Tab 0: Creators / Users Search
                CreatorsSearchTab(
                    creators = creatorResults,
                    isLoading = isLoadingCreators,
                    searchQuery = searchQuery,
                    currentUserId = currentUserId,
                    onOpenCreator = onOpenCreator,
                    onOpenChat = onOpenChat
                )
            }
            1 -> {
                // Tab 1: Real Videos Search
                VideosSearchTab(
                    allPosts = allPosts,
                    searchQuery = searchQuery,
                    onOpenCreator = onOpenCreator,
                    onOpenCreate = onOpenCreate
                )
            }
            2 -> {
                // Tab 2: Hashtags Discovery
                HashtagsSearchTab(
                    allPosts = allPosts,
                    selectedTag = selectedTag,
                    onSelectTag = { selectedTag = it },
                    onOpenCreator = onOpenCreator,
                    onOpenCreate = onOpenCreate
                )
            }
        }
    }
}

@Composable
fun CreatorsSearchTab(
    creators: List<UserProfile>,
    isLoading: Boolean,
    searchQuery: String,
    currentUserId: String,
    onOpenCreator: (String) -> Unit,
    onOpenChat: ((String, String, List<String>) -> Unit)?
) {
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = VyroNeonCyan)
        }
    } else if (creators.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = VyroNeonCyan,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (searchQuery.isNotBlank()) "No creators found matching '$searchQuery'" else "No creators registered yet",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Creators will appear here as users sign in and participate.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        Text(
            text = if (searchQuery.isNotBlank()) "Creators (${creators.size})" else "Community Creators (${creators.size})",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 90.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(creators, key = { it.userId }) { user ->
                CreatorCard(
                    user = user,
                    currentUserId = currentUserId,
                    onOpenCreator = { onOpenCreator(user.userId) },
                    onOpenChat = onOpenChat
                )
            }
        }
    }
}

@Composable
fun CreatorCard(
    user: UserProfile,
    currentUserId: String,
    onOpenCreator: () -> Unit,
    onOpenChat: ((String, String, List<String>) -> Unit)?
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onOpenCreator() }
            .testTag("creator_card_${user.userId}"),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .border(2.dp, VyroNeonCyan, CircleShape)
            ) {
                AsyncImage(
                    model = user.avatarUrl.ifBlank { "https://picsum.photos/seed/${user.username}/200" },
                    contentDescription = user.username,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.displayName.ifBlank { user.username },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verified",
                        tint = VyroNeonCyan,
                        modifier = Modifier.size(14.dp)
                    )
                }

                Text(
                    text = "@${user.username}",
                    fontSize = 12.sp,
                    color = VyroNeonCyan,
                    fontWeight = FontWeight.Medium
                )

                if (user.bio.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = user.bio,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "${user.followersCount} followers",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${user.postsCount} videos",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action Buttons
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = onOpenCreator,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VyroElectricPurple, contentColor = Color.White),
                    modifier = Modifier.testTag("view_profile_btn_${user.userId}")
                ) {
                    Text("Profile", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                if (onOpenChat != null && user.userId != currentUserId) {
                    OutlinedButton(
                        onClick = {
                            val convId = "dm_${listOf(currentUserId, user.userId).sorted().joinToString("_")}"
                            val title = user.displayName.ifBlank { user.username }
                            onOpenChat(convId, title, listOf(currentUserId, user.userId))
                        },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("message_user_btn_${user.userId}")
                    ) {
                        Icon(imageVector = Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Chat", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun VideosSearchTab(
    allPosts: List<Post>,
    searchQuery: String,
    onOpenCreator: (String) -> Unit,
    onOpenCreate: () -> Unit
) {
    val trimmed = searchQuery.trim().removePrefix("#")
    val filteredPosts = if (trimmed.isBlank()) {
        allPosts
    } else {
        allPosts.filter { post ->
            post.caption.contains(trimmed, ignoreCase = true) ||
            post.authorUsername.contains(trimmed, ignoreCase = true) ||
            post.audioTitle.contains(trimmed, ignoreCase = true) ||
            post.tags.any { it.contains(trimmed, ignoreCase = true) }
        }
    }

    if (allPosts.isEmpty()) {
        // Clean zero-state when database has no uploaded videos
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, VyroNeonCyan.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(VyroNeonCyan.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = null,
                        tint = VyroNeonCyan,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "No Videos Uploaded Yet",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "There are currently no videos uploaded to the system. Create and publish the very first video clip!",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onOpenCreate,
                    colors = ButtonDefaults.buttonColors(containerColor = VyroElectricPurple, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.VideoCall, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Upload First Video", fontWeight = FontWeight.Bold)
                }
            }
        }
    } else if (filteredPosts.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No videos matching '$searchQuery'",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    } else {
        Text(
            text = "Videos (${filteredPosts.size})",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            items(filteredPosts, key = { it.id }) { post ->
                DiscoverVideoTile(post = post, onClick = { onOpenCreator(post.authorId) })
            }
        }
    }
}

@Composable
fun HashtagsSearchTab(
    allPosts: List<Post>,
    selectedTag: String,
    onSelectTag: (String) -> Unit,
    onOpenCreator: (String) -> Unit,
    onOpenCreate: () -> Unit
) {
    // Collect unique tags from real posts plus standard starter tags
    val extractedTags = remember(allPosts) {
        val postTags = allPosts.flatMap { it.tags }.map { it.lowercase() }.distinct()
        val defaultTags = listOf("all", "trending", "vyro", "tech", "music", "dance", "gaming", "vlog")
        (listOf("All") + (postTags + defaultTags).distinct()).take(12)
    }

    val displayPosts = if (selectedTag == "All") {
        allPosts
    } else {
        allPosts.filter { it.tags.any { t -> t.equals(selectedTag, ignoreCase = true) } }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Horizontal Chip list of Hashtags
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(extractedTags) { tag ->
                val isSelected = selectedTag.equals(tag, ignoreCase = true)
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectTag(tag) },
                    label = { Text(if (tag == "All") "🔥 All Trends" else "#$tag") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = VyroElectricPurple,
                        selectedLabelColor = Color.White
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) VyroNeonCyan else MaterialTheme.colorScheme.outline
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (displayPosts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(36.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (selectedTag == "All") "No videos uploaded yet." else "No videos tagged #$selectedTag yet.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = onOpenCreate,
                        colors = ButtonDefaults.buttonColors(containerColor = VyroElectricPurple),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (selectedTag == "All") "Upload Video" else "Be first to post #$selectedTag")
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                items(displayPosts, key = { it.id }) { post ->
                    DiscoverVideoTile(post = post, onClick = { onOpenCreator(post.authorId) })
                }
            }
        }
    }
}

@Composable
fun DiscoverVideoTile(
    post: Post,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .testTag("discover_post_${post.id}")
    ) {
        AsyncImage(
            model = post.mediaUrl,
            contentDescription = post.caption,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp)
        ) {
            Text(
                text = "@${post.authorUsername}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = post.caption,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = VyroNeonCyan,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "${post.likesCount}",
                    fontSize = 11.sp,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
