package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Conversation
import com.example.data.model.Post
import com.example.data.model.UserProfile
import com.example.data.repository.VyroRepository
import com.example.ui.theme.VyroAccentPink
import com.example.ui.theme.VyroElectricPurple
import com.example.ui.theme.VyroNeonCyan
import kotlinx.coroutines.launch

@Composable
fun SubscriptionsScreen(
    repository: VyroRepository,
    currentUserId: String,
    onOpenVideo: (Post) -> Unit,
    onOpenCreator: (String) -> Unit,
    onOpenChat: (recipientId: String, recipientUsername: String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Subscriptions, 1: Messages / Chats
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    val followingIds by repository.followingIds.collectAsState(initial = emptySet())
    var subscriptionPosts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var conversations by remember { mutableStateOf<List<Conversation>>(emptyList()) }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            isSearching = true
            searchResults = repository.searchUsers(searchQuery)
            isSearching = false
        } else {
            searchResults = emptyList()
        }
    }

    LaunchedEffect(followingIds) {
        repository.observePosts().collect { allPosts ->
            subscriptionPosts = allPosts.filter { followingIds.contains(it.authorId) }
        }
    }

    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotBlank()) {
            repository.observeConversations(currentUserId).collect { list ->
                conversations = list
            }
        }
    }

    val suggestedCreators = remember {
        listOf(
            UserProfile(userId = "creator_zaid", username = "TheDigitalDonZaid", displayName = "Zaid | The Digital Don", bio = "Rule #1: Loyalty. The Don's Rules ☕✨", followersCount = 245000L),
            UserProfile(userId = "creator_alex", username = "alex_vyro", displayName = "Alex Rivera", bio = "Futuristic tech & sci-fi world building 🚀", followersCount = 48200L),
            UserProfile(userId = "creator_maya", username = "maya_vids", displayName = "Maya Chen", bio = "4K Cinematic escapes & animations 🌲", followersCount = 112000L),
            UserProfile(userId = "creator_sam", username = "sam_drone", displayName = "Sam Skater", bio = "Supercars & Midnight Tokyo drifting 🔥", followersCount = 38900L)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090A10))
            .statusBarsPadding()
            .testTag("subscriptions_screen")
    ) {
        // Top Header
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Channels & Messages",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Keep up with your subscribed creators and direct chats",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Segmented Top Tab (Channels vs Direct Messages)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF161926)
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Button(
                        onClick = { selectedTab = 0 },
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == 0) VyroAccentPink else Color.Transparent,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Subscriptions, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Subscriptions (${followingIds.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Button(
                        onClick = { selectedTab = 1 },
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == 1) VyroNeonCyan else Color.Transparent,
                            contentColor = if (selectedTab == 1) Color.Black else Color.White
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Direct Chats", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        if (selectedTab == 0) {
            // --- TAB 0: SUBSCRIBED CREATORS & CHANNELS ---
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Search Bar
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search creators by name or username...", color = Color.Gray, fontSize = 13.sp) },
                        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = Color.Gray) },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("creator_search_field"),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VyroNeonCyan,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            focusedContainerColor = Color(0xFF161926),
                            unfocusedContainerColor = Color(0xFF161926),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                }

                // Search results
                if (searchQuery.isNotBlank()) {
                    item {
                        Text(
                            text = "Search Results (${searchResults.size})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = VyroNeonCyan
                        )
                    }

                    items(searchResults) { creator ->
                        CreatorSubscriptionCard(
                            creator = creator,
                            isFollowing = followingIds.contains(creator.userId),
                            onToggleFollow = {
                                coroutineScope.launch {
                                    repository.toggleFollow(creator.userId, creator.username)
                                }
                            },
                            onOpenCreator = { onOpenCreator(creator.userId) },
                            onOpenChat = { onOpenChat(creator.userId, creator.username) }
                        )
                    }
                }

                // Subscribed Channels Horizontal Avatars
                item {
                    Text(
                        text = "Subscribed Creators",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(suggestedCreators.filter { followingIds.contains(it.userId) || followingIds.isEmpty() }) { creator ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(72.dp)
                                    .clickable { onOpenCreator(creator.userId) }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, VyroAccentPink, CircleShape)
                                ) {
                                    AsyncImage(
                                        model = "https://picsum.photos/seed/${creator.username}/200",
                                        contentDescription = creator.displayName,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = creator.username,
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Suggested Creators Section
                item {
                    Text(
                        text = "Recommended Channels",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                items(suggestedCreators) { creator ->
                    CreatorSubscriptionCard(
                        creator = creator,
                        isFollowing = followingIds.contains(creator.userId),
                        onToggleFollow = {
                            coroutineScope.launch {
                                repository.toggleFollow(creator.userId, creator.username)
                            }
                        },
                        onOpenCreator = { onOpenCreator(creator.userId) },
                        onOpenChat = { onOpenChat(creator.userId, creator.username) }
                    )
                }
            }
        } else {
            // --- TAB 1: DIRECT MESSAGES & CHATS ---
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (conversations.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF161926))
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("💬", fontSize = 36.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No Active Chats Yet", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Tap any creator you are subscribed to start messaging!",
                                    color = Color.Gray,
                                    fontSize = 13.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // Suggested creators to message
                item {
                    Text("Start a Chat", fontWeight = FontWeight.Bold, color = VyroNeonCyan, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(suggestedCreators) { creator ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenChat(creator.userId, creator.username) },
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF161926)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(46.dp).clip(CircleShape)) {
                                AsyncImage(
                                    model = "https://picsum.photos/seed/${creator.username}/200",
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(creator.displayName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                Text("@${creator.username}", color = Color.Gray, fontSize = 12.sp)
                            }
                            IconButton(
                                onClick = { onOpenChat(creator.userId, creator.username) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(VyroAccentPink.copy(alpha = 0.2f))
                            ) {
                                Icon(imageVector = Icons.Default.Chat, contentDescription = "Message", tint = VyroAccentPink, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreatorSubscriptionCard(
    creator: UserProfile,
    isFollowing: Boolean,
    onToggleFollow: () -> Unit,
    onOpenCreator: () -> Unit,
    onOpenChat: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF161926)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .clickable { onOpenCreator() }
            ) {
                AsyncImage(
                    model = "https://picsum.photos/seed/${creator.username}/200",
                    contentDescription = creator.displayName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onOpenCreator() }
            ) {
                Text(
                    text = creator.displayName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "@${creator.username}",
                    fontSize = 12.sp,
                    color = VyroNeonCyan
                )
                if (creator.bio.isNotBlank()) {
                    Text(
                        text = creator.bio,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                // Direct Message Icon Button
                IconButton(
                    onClick = onOpenChat,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF21263A))
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "Message",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Subscribe / Unsubscribe Button
                Button(
                    onClick = onToggleFollow,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFollowing) Color(0xFF2E354F) else VyroAccentPink,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isFollowing) "Subscribed" else "Subscribe",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
