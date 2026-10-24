package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Comment
import com.example.data.model.Post
import com.example.data.model.Story
import com.example.data.repository.VyroRepository
import com.example.ui.theme.VyroAccentPink
import com.example.ui.theme.VyroElectricPurple
import com.example.ui.theme.VyroNeonCyan
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    repository: VyroRepository,
    currentUserId: String,
    currentUsername: String,
    currentAvatarUrl: String,
    onOpenStoryViewer: (Int) -> Unit,
    onOpenProfile: (String) -> Unit,
    onOpenCreate: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var stories by remember { mutableStateOf<List<Story>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var selectedPostForComments by remember { mutableStateOf<Post?>(null) }
    var showShareDialogForPost by remember { mutableStateOf<Post?>(null) }

    LaunchedEffect(Unit) {
        // Collect stories
        coroutineScope.launch {
            repository.observeStories().collect { list ->
                stories = list
            }
        }
        // Collect feed posts (only real uploaded posts)
        coroutineScope.launch {
            repository.observePosts().collect { list ->
                posts = list
                isLoading = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("feed_screen")
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = VyroNeonCyan)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Top Stories Tray
                item {
                    StoriesTray(
                        stories = stories,
                        currentAvatarUrl = currentAvatarUrl,
                        onAddStory = {
                            coroutineScope.launch {
                                repository.createStory(
                                    mediaUrl = "https://picsum.photos/seed/story${System.currentTimeMillis()}/600/1000",
                                    caption = "New VYRO moment 🔥",
                                    authorUsername = currentUsername,
                                    avatarUrl = currentAvatarUrl
                                )
                            }
                        },
                        onOpenStory = onOpenStoryViewer
                    )
                }

                if (posts.isEmpty()) {
                    item {
                        EmptyFeedCard(onUploadVideo = onOpenCreate)
                    }
                } else {
                    // Real Video Feed Items
                    items(posts, key = { it.id.ifBlank { it.mediaUrl } }) { post ->
                        FeedVideoCard(
                            post = post,
                            repository = repository,
                            currentUserId = currentUserId,
                            onOpenComments = { selectedPostForComments = post },
                            onShare = { showShareDialogForPost = post },
                            onOpenCreator = { onOpenProfile(post.authorId) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }

        // Comments Bottom Sheet
        if (selectedPostForComments != null) {
            CommentsModalBottomSheet(
                post = selectedPostForComments!!,
                repository = repository,
                currentUsername = currentUsername,
                currentAvatarUrl = currentAvatarUrl,
                onDismiss = { selectedPostForComments = null }
            )
        }

        // Share Alert Dialog
        if (showShareDialogForPost != null) {
            AlertDialog(
                onDismissRequest = { showShareDialogForPost = null },
                title = { Text("Share Post", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Share this VYRO post by @${showShareDialogForPost!!.authorUsername}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "vyro.app/p/${showShareDialogForPost!!.id}",
                            color = VyroNeonCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showShareDialogForPost = null },
                        colors = ButtonDefaults.buttonColors(containerColor = VyroElectricPurple)
                    ) {
                        Text("Copy Link")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showShareDialogForPost = null }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}

@Composable
fun StoriesTray(
    stories: List<Story>,
    currentAvatarUrl: String,
    onAddStory: () -> Unit,
    onOpenStory: (Int) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Your Story item
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onAddStory() }
                    .testTag("add_story_button")
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .border(2.dp, VyroElectricPurple, CircleShape)
                    ) {
                        AsyncImage(
                            model = currentAvatarUrl.ifBlank { "https://picsum.photos/seed/user_me/200" },
                            contentDescription = "Your Story",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(VyroNeonCyan),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Story",
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Your Story",
                    fontSize = 11.sp,
                    color = Color.White,
                    maxLines = 1
                )
            }
        }

        // Other stories
        items(stories.size) { index ->
            val story = stories[index]
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onOpenStory(index) }
                    .testTag("story_item_$index")
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .border(
                            2.dp,
                            Brush.sweepGradient(listOf(VyroElectricPurple, VyroNeonCyan, VyroAccentPink, VyroElectricPurple)),
                            CircleShape
                        )
                        .padding(3.dp)
                        .clip(CircleShape)
                ) {
                    AsyncImage(
                        model = story.authorAvatarUrl.ifBlank { "https://picsum.photos/seed/${story.authorUsername}/200" },
                        contentDescription = story.authorUsername,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = story.authorUsername,
                    fontSize = 11.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 64.dp)
                )
            }
        }
    }
}

@Composable
fun FeedVideoCard(
    post: Post,
    repository: VyroRepository,
    currentUserId: String,
    onOpenComments: () -> Unit,
    onShare: () -> Unit,
    onOpenCreator: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isLiked by remember { mutableStateOf(false) }
    var likesCount by remember { mutableStateOf(post.likesCount) }
    var isPlaying by remember { mutableStateOf(true) }
    var isMuted by remember { mutableStateOf(false) }

    LaunchedEffect(post.id) {
        if (post.id.isNotBlank()) {
            isLiked = repository.isPostLiked(post.id)
        }
    }

    // Music disc animation
    val infiniteTransition = rememberInfiniteTransition(label = "disc")
    val discRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(540.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF12141F))
    ) {
        // High-performance ExoPlayer video item with scrubber, mute HUD, tap-to-pause & heart burst
        com.example.ui.components.VideoPlayerItem(
            mediaUrl = post.mediaUrl,
            thumbnailUrl = post.thumbnailUrl,
            isPlaying = isPlaying,
            isMuted = isMuted,
            onTogglePlay = { isPlaying = !isPlaying },
            onMuteToggle = { isMuted = !isMuted },
            onDoubleTapLike = {
                if (!isLiked) {
                    isLiked = true
                    likesCount += 1
                    coroutineScope.launch {
                        repository.toggleLike(post)
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Bottom Left Details (Creator, Caption, Audio)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.78f)
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onOpenCreator() }
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, VyroNeonCyan, CircleShape)
                ) {
                    AsyncImage(
                        model = post.authorAvatarUrl.ifBlank { "https://picsum.photos/seed/${post.authorUsername}/200" },
                        contentDescription = post.authorUsername,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "@${post.authorUsername}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = post.caption,
                fontSize = 14.sp,
                color = Color.White,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            if (post.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    post.tags.take(3).forEach { tag ->
                        Text(
                            text = "#$tag",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = VyroNeonCyan
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Audio track indicator
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "Music",
                    tint = VyroNeonCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = post.audioTitle,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Right Action Rail (Heart, Comment, Bookmark, Share, Rotating Disc)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Like Button
            ActionRailItem(
                icon = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                tint = if (isLiked) VyroAccentPink else Color.White,
                count = "$likesCount",
                onClick = {
                    isLiked = !isLiked
                    likesCount += if (isLiked) 1 else -1
                    coroutineScope.launch {
                        repository.toggleLike(post)
                    }
                },
                testTag = "like_button_${post.id}"
            )

            // Comment Button
            ActionRailItem(
                icon = Icons.Outlined.ModeComment,
                tint = Color.White,
                count = "${post.commentsCount}",
                onClick = onOpenComments,
                testTag = "comments_button_${post.id}"
            )

            // Share Button
            ActionRailItem(
                icon = Icons.Outlined.Share,
                tint = Color.White,
                count = "Share",
                onClick = onShare,
                testTag = "share_button_${post.id}"
            )

            // Rotating Music Disc
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.8f))
                    .border(2.dp, VyroNeonCyan, CircleShape)
                    .rotate(if (isPlaying) discRotation else 0f),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = VyroElectricPurple,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ActionRailItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    count: String,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.45f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = count,
                tint = tint,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = count,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsModalBottomSheet(
    post: Post,
    repository: VyroRepository,
    currentUsername: String,
    currentAvatarUrl: String,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var commentText by remember { mutableStateOf("") }
    var comments by remember { mutableStateOf<List<Comment>>(emptyList()) }
    val likedCommentIds = remember { mutableStateListOf<String>() }

    LaunchedEffect(post.id) {
        if (post.id.isNotBlank()) {
            repository.observeComments(post.id).collect {
                comments = it
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF161926),
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.fillMaxHeight(0.75f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .imePadding()
        ) {
            // TikTok Header with comment count and close X button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.size(24.dp))
                Text(
                    text = "${comments.size} comments",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(bottom = 8.dp))

            if (comments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("💬", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No comments yet",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Be the first to say something nice!",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(comments) { comment ->
                        val isCommentLiked = likedCommentIds.contains(comment.id)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                            ) {
                                AsyncImage(
                                    model = comment.authorAvatarUrl.ifBlank { "https://picsum.photos/seed/${comment.authorUsername}/200" },
                                    contentDescription = comment.authorUsername,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "@${comment.authorUsername}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (comment.authorId == post.authorId) VyroNeonCyan else Color.White.copy(alpha = 0.9f)
                                    )
                                    if (comment.authorId == post.authorId) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = VyroNeonCyan.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                "Creator",
                                                color = VyroNeonCyan,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = comment.text,
                                    fontSize = 14.sp,
                                    color = Color.White,
                                    lineHeight = 18.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Just now", fontSize = 11.sp, color = Color.Gray)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        "Reply",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Gray,
                                        modifier = Modifier.clickable {
                                            commentText = "@${comment.authorUsername} "
                                        }
                                    )
                                }
                            }

                            // TikTok Heart like action for comment
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable {
                                    if (isCommentLiked) likedCommentIds.remove(comment.id)
                                    else likedCommentIds.add(comment.id)
                                }
                            ) {
                                Icon(
                                    imageVector = if (isCommentLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Like comment",
                                    tint = if (isCommentLiked) VyroAccentPink else Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (isCommentLiked) "1" else "",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }

            // Quick Emoji Reaction Bar (TikTok style!)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                listOf("🔥", "❤️", "😂", "👏", "💯", "🚀", "✨").forEach { emoji ->
                    Text(
                        text = emoji,
                        fontSize = 20.sp,
                        modifier = Modifier
                            .clickable { commentText += emoji }
                            .padding(4.dp)
                    )
                }
            }

            // Bottom Add comment row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                ) {
                    AsyncImage(
                        model = currentAvatarUrl.ifBlank { "https://picsum.photos/seed/$currentUsername/200" },
                        contentDescription = "My avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    placeholder = { Text("Add comment...", fontSize = 14.sp, color = Color.Gray) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("comment_input_field"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VyroNeonCyan,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedContainerColor = Color(0xFF0D0F17),
                        unfocusedContainerColor = Color(0xFF0D0F17),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (commentText.isNotBlank() && post.id.isNotBlank()) {
                            val textToSend = commentText
                            commentText = ""
                            coroutineScope.launch {
                                repository.addComment(post.id, textToSend, currentUsername, currentAvatarUrl)
                            }
                        }
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(VyroAccentPink)
                        .testTag("send_comment_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Send,
                        contentDescription = "Post comment",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyFeedCard(onUploadVideo: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 40.dp)
            .testTag("empty_feed_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, VyroNeonCyan.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(VyroNeonCyan.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = null,
                    tint = VyroNeonCyan,
                    modifier = Modifier.size(38.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Videos Uploaded Yet",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "The feed is currently clean and waiting for the community's first real clips! Share your video or vlog now.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onUploadVideo,
                colors = ButtonDefaults.buttonColors(containerColor = VyroElectricPurple, contentColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                modifier = Modifier.testTag("feed_empty_upload_button")
            ) {
                Icon(imageVector = Icons.Default.VideoCall, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create & Upload Video", fontWeight = FontWeight.Bold)
            }
        }
    }
}

