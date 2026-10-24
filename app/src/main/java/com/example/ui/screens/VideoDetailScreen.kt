package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Send
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
import com.example.data.model.Comment
import com.example.data.model.Post
import com.example.data.repository.VyroRepository
import com.example.ui.components.VideoPlayerItem
import com.example.ui.theme.VyroAccentPink
import com.example.ui.theme.VyroElectricPurple
import com.example.ui.theme.VyroNeonCyan
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoDetailScreen(
    post: Post,
    repository: VyroRepository,
    currentUserId: String,
    currentUsername: String,
    currentAvatarUrl: String,
    onBack: () -> Unit,
    onOpenCreator: (String) -> Unit,
    onSelectRelatedVideo: (Post) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isPlaying by remember { mutableStateOf(true) }
    var isMuted by remember { mutableStateOf(false) }
    var isLiked by remember { mutableStateOf(false) }
    var isDisliked by remember { mutableStateOf(false) }
    var likesCount by remember { mutableStateOf(post.likesCount) }
    var isDescriptionExpanded by remember { mutableStateOf(false) }

    val followingIds by repository.followingIds.collectAsState(initial = emptySet())
    val isSubscribed = followingIds.contains(post.authorId)

    var comments by remember { mutableStateOf<List<Comment>>(emptyList()) }
    var commentInput by remember { mutableStateOf("") }
    var relatedVideos by remember { mutableStateOf<List<Post>>(emptyList()) }
    var showShareModal by remember { mutableStateOf(false) }

    LaunchedEffect(post.id) {
        if (post.id.isNotBlank()) {
            isLiked = repository.isPostLiked(post.id)
            repository.observeComments(post.id).collect { list ->
                comments = list
            }
        }
    }

    LaunchedEffect(post.category) {
        repository.observePosts().collect { all ->
            relatedVideos = all.filter { it.id != post.id }.take(6)
        }
    }

    var isFullscreen by remember { mutableStateOf(false) }

    BackHandler(enabled = isFullscreen) {
        isFullscreen = false
    }

    if (isFullscreen) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .testTag("fullscreen_video_view")
        ) {
            VideoPlayerItem(
                mediaUrl = post.mediaUrl,
                thumbnailUrl = post.thumbnailUrl,
                isPlaying = isPlaying,
                isMuted = isMuted,
                videoFormat = "long",
                isFullscreen = true,
                onTogglePlay = { isPlaying = !isPlaying },
                onMuteToggle = { isMuted = !isMuted },
                onToggleFullscreen = { isFullscreen = false },
                modifier = Modifier.fillMaxSize()
            )
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "Watch Video", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showShareModal = true }) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF0D0F17),
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            },
            containerColor = Color(0xFF090A10)
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .testTag("video_detail_screen")
            ) {
                // 16:9 Dedicated Player
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .background(Color.Black)
                    ) {
                        VideoPlayerItem(
                            mediaUrl = post.mediaUrl,
                            thumbnailUrl = post.thumbnailUrl,
                            isPlaying = isPlaying,
                            isMuted = isMuted,
                            videoFormat = "long",
                            isFullscreen = false,
                            onTogglePlay = { isPlaying = !isPlaying },
                            onMuteToggle = { isMuted = !isMuted },
                            onToggleFullscreen = { isFullscreen = true },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

            // Title & Stats
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = post.caption,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "${formatViewCount(post.viewsCount)} views  •  Category: ${post.category}",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Channel Row + Subscribe Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onOpenCreator(post.authorId) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                            ) {
                                AsyncImage(
                                    model = post.authorAvatarUrl.ifBlank { "https://picsum.photos/seed/${post.authorUsername}/200" },
                                    contentDescription = post.authorUsername,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = post.authorUsername,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = if (isSubscribed) "Subscribed" else "Creator",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    repository.toggleFollow(post.authorId, post.authorUsername)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSubscribed) Color(0xFF25293A) else Color.White,
                                contentColor = if (isSubscribed) Color.White else Color.Black
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.testTag("subscribe_button")
                        ) {
                            Text(
                                text = if (isSubscribed) "Subscribed ✓" else "Subscribe",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action buttons pill row (Like, Dislike, Share, Save)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Like Button
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF1E2130),
                            modifier = Modifier.clickable {
                                isLiked = !isLiked
                                if (isLiked) isDisliked = false
                                likesCount += if (isLiked) 1 else -1
                                coroutineScope.launch { repository.toggleLike(post) }
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isLiked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                                    contentDescription = "Like",
                                    tint = if (isLiked) VyroNeonCyan else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "$likesCount",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Dislike Button
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF1E2130),
                            modifier = Modifier.clickable {
                                isDisliked = !isDisliked
                                if (isDisliked && isLiked) {
                                    isLiked = false
                                    likesCount = (likesCount - 1).coerceAtLeast(0)
                                }
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isDisliked) Icons.Filled.ThumbDown else Icons.Outlined.ThumbDown,
                                    contentDescription = "Dislike",
                                    tint = if (isDisliked) VyroAccentPink else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Share Button
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF1E2130),
                            modifier = Modifier.clickable { showShareModal = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Expandable Description Box
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF161926),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isDescriptionExpanded = !isDescriptionExpanded }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Description",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = post.caption + "\n\nAudio: ${post.audioTitle}\nTags: ${post.tags.joinToString(" ") { "#$it" }}",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f),
                                maxLines = if (isDescriptionExpanded) Int.MAX_VALUE else 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 16.sp
                            )
                            Text(
                                text = if (isDescriptionExpanded) "Show less" else "...more",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = VyroNeonCyan,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            // Comments Section Header & Input
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Divider(color = Color.White.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Comments (${comments.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = currentAvatarUrl,
                            contentDescription = currentUsername,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        OutlinedTextField(
                            value = commentInput,
                            onValueChange = { commentInput = it },
                            placeholder = { Text("Add a comment...", fontSize = 13.sp, color = Color.Gray) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("comment_input_field"),
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VyroNeonCyan,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            trailingIcon = {
                                if (commentInput.isNotBlank()) {
                                    IconButton(onClick = {
                                        val text = commentInput.trim()
                                        commentInput = ""
                                        coroutineScope.launch {
                                            repository.addComment(post.id, text, currentUsername, currentAvatarUrl)
                                        }
                                    }) {
                                        Icon(imageVector = Icons.AutoMirrored.Outlined.Send, contentDescription = "Send", tint = VyroNeonCyan)
                                    }
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Recent Comments List (take up to 3)
                    comments.take(3).forEach { comment ->
                        Row(modifier = Modifier.padding(vertical = 6.dp)) {
                            AsyncImage(
                                model = comment.authorAvatarUrl.ifBlank { "https://picsum.photos/seed/${comment.authorUsername}/100" },
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = "@${comment.authorUsername}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(text = comment.text, fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color.White.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Up Next",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Up Next / Related Videos
            items(relatedVideos, key = { it.id.ifBlank { it.mediaUrl } }) { relPost ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectRelatedVideo(relPost) }
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(130.dp)
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E2130))
                    ) {
                        AsyncImage(
                            model = relPost.thumbnailUrl.ifBlank { relPost.mediaUrl },
                            contentDescription = relPost.caption,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = relPost.caption,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = relPost.authorUsername,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "${formatViewCount(relPost.viewsCount)} views",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.4f)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        if (showShareModal) {
            AlertDialog(
                onDismissRequest = { showShareModal = false },
                title = { Text("Share Video", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Share \"${post.caption.take(30)}\"")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("vyro.app/v/${post.id}", color = VyroNeonCyan, fontWeight = FontWeight.Medium)
                    }
                },
                confirmButton = {
                    Button(onClick = { showShareModal = false }, colors = ButtonDefaults.buttonColors(containerColor = VyroElectricPurple)) {
                        Text("Copy Link")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showShareModal = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}
}

