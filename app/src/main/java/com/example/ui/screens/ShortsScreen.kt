package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Post
import com.example.data.repository.VyroRepository
import com.example.ui.components.VideoPlayerItem
import com.example.ui.theme.VyroAccentPink
import com.example.ui.theme.VyroElectricPurple
import com.example.ui.theme.VyroNeonCyan
import kotlinx.coroutines.launch

@Composable
fun ShortsScreen(
    repository: VyroRepository,
    currentUserId: String,
    currentUsername: String,
    currentAvatarUrl: String,
    onOpenCreator: (String) -> Unit,
    onOpenCreate: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var shortsList by remember { mutableStateOf<List<Post>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedPostForComments by remember { mutableStateOf<Post?>(null) }
    var showShareDialogForPost by remember { mutableStateOf<Post?>(null) }
    var isMuted by remember { mutableStateOf(false) }

    val followingIds by repository.followingIds.collectAsState(initial = emptySet())

    LaunchedEffect(Unit) {
        repository.observePosts().collect { allPosts ->
            // Filter short-form videos or show vertical videos
            val filtered = allPosts.filter { it.videoFormat == "short" || it.videoFormat.isBlank() }
            shortsList = if (filtered.isNotEmpty()) filtered else allPosts
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("shorts_screen")
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = VyroNeonCyan)
            }
        } else if (shortsList.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = VyroNeonCyan,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No Shorts Yet",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Be the first creator to share a 9:16 vertical Short on VYRO!",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onOpenCreate,
                    colors = ButtonDefaults.buttonColors(containerColor = VyroElectricPurple),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Upload First Short", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            val pagerState = rememberPagerState(pageCount = { shortsList.size })

            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val shortPost = shortsList[page]
                val isPageActive = pagerState.currentPage == page

                ShortVideoPageItem(
                    post = shortPost,
                    repository = repository,
                    isPageActive = isPageActive,
                    isMuted = isMuted,
                    isFollowing = followingIds.contains(shortPost.authorId),
                    onToggleMute = { isMuted = !isMuted },
                    onOpenCreator = { onOpenCreator(shortPost.authorId) },
                    onOpenComments = { selectedPostForComments = shortPost },
                    onShare = { showShareDialogForPost = shortPost },
                    onToggleFollow = {
                        coroutineScope.launch {
                            repository.toggleFollow(shortPost.authorId, shortPost.authorUsername)
                        }
                    }
                )
            }
        }

        // Top App Bar Header Overlay (Shorts branding + sound indicator)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "VYRO",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = VyroNeonCyan.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VyroNeonCyan)
                ) {
                    Text(
                        text = "SHORTS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = VyroNeonCyan,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = onOpenCreate,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Camera", tint = Color.White, modifier = Modifier.size(18.dp))
                }

                IconButton(
                    onClick = { isMuted = !isMuted },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = "Mute",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
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

        // Share Dialog
        if (showShareDialogForPost != null) {
            AlertDialog(
                onDismissRequest = { showShareDialogForPost = null },
                title = { Text("Share Short", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Share this short by @${showShareDialogForPost!!.authorUsername}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "vyro.app/s/${showShareDialogForPost!!.id}",
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
fun ShortVideoPageItem(
    post: Post,
    repository: VyroRepository,
    isPageActive: Boolean,
    isMuted: Boolean,
    isFollowing: Boolean,
    onToggleMute: () -> Unit,
    onOpenCreator: () -> Unit,
    onOpenComments: () -> Unit,
    onShare: () -> Unit,
    onToggleFollow: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isLiked by remember { mutableStateOf(false) }
    var likesCount by remember { mutableStateOf(post.likesCount) }
    var isPlaying by remember { mutableStateOf(true) }

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
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Fullscreen 9:16 Video Player
        VideoPlayerItem(
            mediaUrl = post.mediaUrl,
            thumbnailUrl = post.thumbnailUrl,
            isPlaying = isPageActive && isPlaying,
            isMuted = isMuted,
            onTogglePlay = { isPlaying = !isPlaying },
            onMuteToggle = onToggleMute,
            onDoubleTapLike = {
                if (!isLiked) {
                    isLiked = true
                    likesCount += 1
                    coroutineScope.launch { repository.toggleLike(post) }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Gradient overlay for readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.35f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.88f)
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )

        // Bottom Left Creator Info & Caption
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.78f)
                .padding(start = 16.dp, bottom = 40.dp, end = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onOpenCreator() }
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
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

                if (!isFollowing) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = VyroNeonCyan,
                        modifier = Modifier.clickable { onToggleFollow() }
                    ) {
                        Text(
                            text = "+ Follow",
                            color = Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = post.caption,
                fontSize = 14.sp,
                color = Color.White,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            if (post.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
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

            // Audio Attribution Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.4f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "Music",
                    tint = VyroNeonCyan,
                    modifier = Modifier.size(14.dp)
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

        // Right Action Rail (Follow, Like, Comments, Share, Disc)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Creator Avatar with follow (+) badge
            Box(contentAlignment = Alignment.BottomCenter) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, Color.White, CircleShape)
                        .clickable { onOpenCreator() }
                ) {
                    AsyncImage(
                        model = post.authorAvatarUrl.ifBlank { "https://picsum.photos/seed/${post.authorUsername}/200" },
                        contentDescription = post.authorUsername,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                if (!isFollowing) {
                    Box(
                        modifier = Modifier
                            .offset(y = 6.dp)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(VyroAccentPink)
                            .clickable { onToggleFollow() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Follow", tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Like Action
            ActionRailItem(
                icon = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                tint = if (isLiked) VyroAccentPink else Color.White,
                count = "$likesCount",
                onClick = {
                    isLiked = !isLiked
                    likesCount += if (isLiked) 1 else -1
                    coroutineScope.launch { repository.toggleLike(post) }
                },
                testTag = "short_like_button_${post.id}"
            )

            // Comment Action
            ActionRailItem(
                icon = Icons.Outlined.ModeComment,
                tint = Color.White,
                count = "${post.commentsCount}",
                onClick = onOpenComments,
                testTag = "short_comments_button_${post.id}"
            )

            // Share Action
            ActionRailItem(
                icon = Icons.Outlined.Share,
                tint = Color.White,
                count = "Share",
                onClick = onShare,
                testTag = "short_share_button_${post.id}"
            )

            // Rotating Vinyl Sound Disc
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black)
                    .border(2.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                    .rotate(discRotation),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(VyroNeonCyan)
                )
            }
        }
    }
}
