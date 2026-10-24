package com.example.ui.screens

import androidx.compose.foundation.background
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
import com.example.data.model.Post
import com.example.data.repository.VyroRepository
import com.example.ui.theme.VyroElectricPurple
import com.example.ui.theme.VyroNeonCyan
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    repository: VyroRepository,
    currentUserId: String,
    currentUsername: String,
    currentAvatarUrl: String,
    onOpenVideoDetail: (Post) -> Unit,
    onOpenShortsTab: () -> Unit,
    onOpenCreator: (String) -> Unit,
    onOpenCreate: () -> Unit,
    onOpenGame: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    var allPosts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf("All") }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    val categories = listOf("All", "Trending", "Tech", "Gaming", "Entertainment", "Shorts", "Music")

    LaunchedEffect(Unit) {
        repository.observePosts().collect { list ->
            allPosts = list
            isLoading = false
        }
    }

    val longVideos = remember(allPosts, selectedCategory, searchQuery) {
        var filtered = allPosts.filter { it.videoFormat == "long" || it.videoFormat.isBlank() }
        if (selectedCategory != "All" && selectedCategory != "Trending" && selectedCategory != "Shorts") {
            filtered = filtered.filter { it.category.equals(selectedCategory, ignoreCase = true) }
        }
        if (searchQuery.isNotBlank()) {
            filtered = filtered.filter {
                it.caption.contains(searchQuery, ignoreCase = true) ||
                        it.authorUsername.contains(searchQuery, ignoreCase = true) ||
                        it.tags.any { tag -> tag.contains(searchQuery, ignoreCase = true) }
            }
        }
        filtered
    }

    val shortVideos = remember(allPosts) {
        allPosts.filter { it.videoFormat == "short" }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0D0F17))
                    .statusBarsPadding()
            ) {
                // Top Brand Bar + Search Input
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(VyroElectricPurple),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "VYRO",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = Color.White
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = { isSearchActive = !isSearchActive }) {
                            Icon(
                                imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color.White
                            )
                        }

                        IconButton(onClick = onOpenCreate) {
                            Icon(imageVector = Icons.Default.VideoCall, contentDescription = "Upload", tint = VyroNeonCyan)
                        }
                    }
                }

                // Search Bar Expandable
                if (isSearchActive) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search videos, creators, tags...", color = Color.Gray, fontSize = 13.sp) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VyroNeonCyan,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = Color.White)
                                }
                            }
                        }
                    )
                }

                // Category Filter Pills
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = {
                                Text(
                                    text = category,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VyroNeonCyan,
                                selectedLabelColor = Color.Black,
                                containerColor = Color(0xFF1E2130),
                                labelColor = Color.White
                            ),
                            border = null
                        )
                    }
                }
            }
        },
        containerColor = Color(0xFF0A0C14)
    ) { innerPadding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = VyroNeonCyan)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .testTag("home_feed_list"),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Horizontal Shorts Shelf (TikTok carousel on YouTube Home)
                if (shortVideos.isNotEmpty() && (selectedCategory == "All" || selectedCategory == "Shorts")) {
                    item {
                        ShortsShelf(
                            shorts = shortVideos,
                            onOpenShortsTab = onOpenShortsTab,
                            onSelectShort = onOpenVideoDetail
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // Viral Tap Mini-Game Challenge Banner
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clickable { onOpenGame() }
                            .testTag("home_viral_game_banner"),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF1E2130),
                        border = androidx.compose.foundation.BorderStroke(1.dp, VyroNeonCyan.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Text("⚡", fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Viral Tap Reflex Challenge", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                    Text("Tap viral targets, build combos & hit high scores!", color = Color.Gray, fontSize = 12.sp)
                                }
                            }
                            Button(
                                onClick = onOpenGame,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = VyroNeonCyan, contentColor = Color.Black),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text("Play", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (longVideos.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No long-form videos found", color = Color.White, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = onOpenCreate,
                                colors = ButtonDefaults.buttonColors(containerColor = VyroElectricPurple)
                            ) {
                                Text("Upload a Video")
                            }
                        }
                    }
                } else {
                    items(longVideos, key = { it.id.ifBlank { it.mediaUrl } }) { post ->
                        LongVideoCard(
                            post = post,
                            onClick = {
                                coroutineScope.launch { repository.incrementViewCount(post.id) }
                                onOpenVideoDetail(post)
                            },
                            onOpenCreator = { onOpenCreator(post.authorId) }
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun LongVideoCard(
    post: Post,
    onClick: () -> Unit,
    onOpenCreator: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("long_video_card_${post.id}")
    ) {
        // 16:9 Thumbnail Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(Color(0xFF141724))
        ) {
            AsyncImage(
                model = post.thumbnailUrl.ifBlank { post.mediaUrl },
                contentDescription = post.caption,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Duration Badge (e.g. "9:56")
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color.Black.copy(alpha = 0.85f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
            ) {
                Text(
                    text = formatVideoDuration(post.durationSeconds),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        // Metadata row (Channel Avatar, Title, Stats)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable { onOpenCreator() }
            ) {
                AsyncImage(
                    model = post.authorAvatarUrl.ifBlank { "https://picsum.photos/seed/${post.authorUsername}/200" },
                    contentDescription = post.authorUsername,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = post.caption,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = post.authorUsername,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "•  ${formatViewCount(post.viewsCount)} views  •  ${post.category}",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun ShortsShelf(
    shorts: List<Post>,
    onOpenShortsTab: () -> Unit,
    onSelectShort: (Post) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF10131E))
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = VyroNeonCyan, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Shorts", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.White)
            }

            Text(
                text = "View all",
                color = VyroNeonCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onOpenShortsTab() }
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(shorts.take(6)) { short ->
                Box(
                    modifier = Modifier
                        .width(130.dp)
                        .height(210.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E2130))
                        .clickable { onSelectShort(short) }
                ) {
                    AsyncImage(
                        model = short.thumbnailUrl.ifBlank { short.mediaUrl },
                        contentDescription = short.caption,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = short.caption,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${formatViewCount(short.viewsCount)} views",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

fun formatVideoDuration(seconds: Long): String {
    if (seconds <= 0) return "0:15"
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%d:%02d", m, s)
}

fun formatViewCount(views: Long): String {
    return when {
        views >= 1_000_000 -> String.format("%.1fM", views / 1_000_000.0)
        views >= 1_000 -> String.format("%.1fK", views / 1_000.0)
        else -> views.toString()
    }
}
