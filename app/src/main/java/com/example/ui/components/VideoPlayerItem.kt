package com.example.ui.components

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.ui.theme.VyroAccentPink
import com.example.ui.theme.VyroNeonCyan
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerItem(
    mediaUrl: String,
    thumbnailUrl: String = "",
    modifier: Modifier = Modifier,
    isMuted: Boolean = false,
    isPlaying: Boolean = true,
    videoFormat: String = "short",
    isFullscreen: Boolean = false,
    onTogglePlay: () -> Unit = {},
    onDoubleTapLike: () -> Unit = {},
    onMuteToggle: () -> Unit = {},
    onToggleFullscreen: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var isVideoFormat by remember(mediaUrl) {
        val lower = mediaUrl.lowercase()
        val isVideo = lower.startsWith("asset://") || lower.endsWith(".mp4") || lower.endsWith(".webm") || lower.endsWith(".mov") ||
                lower.endsWith(".mkv") || lower.contains("video") || lower.startsWith("content://") ||
                (lower.startsWith("file://") && !lower.endsWith(".jpg") && !lower.endsWith(".png"))
        mutableStateOf(isVideo)
    }

    var isPlayerReady by remember { mutableStateOf(false) }
    var hasPlaybackError by remember { mutableStateOf(false) }
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(1L) }
    var isScrubbing by remember { mutableStateOf(false) }
    var scrubProgress by remember { mutableFloatStateOf(0f) }

    // Heart bursting animation state for double tap
    var showBurstHeart by remember { mutableStateOf(false) }
    var playPauseFeedbackIcon by remember { mutableStateOf<Boolean?>(null) } // true: play, false: pause

    // ExoPlayer Lifecycle instance
    val exoPlayer = remember(mediaUrl, isVideoFormat) {
        if (!isVideoFormat) null
        else {
            try {
                ExoPlayer.Builder(context).build().apply {
                    val uri = Uri.parse(mediaUrl)
                    setMediaItem(MediaItem.fromUri(uri))
                    repeatMode = Player.REPEAT_MODE_ONE
                    prepare()
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    // Progress polling loop
    LaunchedEffect(exoPlayer, isPlaying, isScrubbing) {
        if (exoPlayer != null) {
            while (true) {
                if (!isScrubbing) {
                    val pos = exoPlayer.currentPosition.coerceAtLeast(0L)
                    val dur = exoPlayer.duration.coerceAtLeast(1L)
                    currentPositionMs = pos
                    durationMs = dur
                }
                delay(200L)
            }
        }
    }

    // Synchronize play/pause and volume states
    LaunchedEffect(isPlaying, exoPlayer) {
        exoPlayer?.playWhenReady = isPlaying
    }

    LaunchedEffect(isMuted, exoPlayer) {
        exoPlayer?.volume = if (isMuted) 0f else 1f
    }

    // Attach Player Listener
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                isPlayerReady = playbackState == Player.STATE_READY || playbackState == Player.STATE_BUFFERING
            }

            override fun onPlayerError(error: PlaybackException) {
                hasPlaybackError = true
            }
        }
        exoPlayer?.addListener(listener)
        onDispose {
            exoPlayer?.removeListener(listener)
            exoPlayer?.release()
        }
    }

    // Heart burst animation timer
    LaunchedEffect(showBurstHeart) {
        if (showBurstHeart) {
            delay(800L)
            showBurstHeart = false
        }
    }

    // Play/Pause icon feedback timer
    LaunchedEffect(playPauseFeedbackIcon) {
        if (playPauseFeedbackIcon != null) {
            delay(700L)
            playPauseFeedbackIcon = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(mediaUrl) {
                detectTapGestures(
                    onTap = {
                        onTogglePlay()
                        playPauseFeedbackIcon = !isPlaying
                    },
                    onDoubleTap = {
                        showBurstHeart = true
                        onDoubleTapLike()
                    }
                )
            }
            .testTag("feed_video_player_box")
    ) {
        if (isVideoFormat && !hasPlaybackError && exoPlayer != null) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false
                        resizeMode = if (videoFormat == "long") {
                            AspectRatioFrameLayout.RESIZE_MODE_FIT
                        } else {
                            AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        }
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                update = { view ->
                    view.player = exoPlayer
                    view.resizeMode = if (videoFormat == "long") {
                        AspectRatioFrameLayout.RESIZE_MODE_FIT
                    } else {
                        AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Static image fallback for image posts or error
            AsyncImage(
                model = if (mediaUrl.isNotBlank()) mediaUrl else thumbnailUrl,
                contentDescription = "Post Media",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Dark gradient overlay for bottom readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.4f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.85f)
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )

        // Top Status HUD (Mute Toggle & Audio Indicator)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onMuteToggle,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f))
                    .testTag("video_mute_toggle_button")
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                    contentDescription = if (isMuted) "Unmute" else "Mute",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isVideoFormat) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = formatTime(if (isScrubbing) (scrubProgress * durationMs).toLong() else currentPositionMs) +
                                    " / " + formatTime(durationMs),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                if (onToggleFullscreen != null) {
                    IconButton(
                        onClick = onToggleFullscreen,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.55f))
                            .testTag("fullscreen_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = if (isFullscreen) "Exit Fullscreen" else "Enter Fullscreen",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Center Tap Feedback Badge (Play / Pause Indicator)
        AnimatedVisibility(
            visible = playPauseFeedbackIcon != null,
            enter = fadeIn() + scaleIn(initialScale = 0.7f),
            exit = fadeOut() + scaleOut(targetScale = 1.3f),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.65f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (playPauseFeedbackIcon == true) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }
        }

        // Center Burst Heart Animation on Double-Tap
        AnimatedVisibility(
            visible = showBurstHeart,
            enter = fadeIn() + scaleIn(initialScale = 0.3f),
            exit = fadeOut() + scaleOut(targetScale = 1.5f),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Liked",
                tint = VyroAccentPink,
                modifier = Modifier.size(110.dp)
            )
        }

        // Bottom Interactive Video Progress Scrubber
        if (isVideoFormat && durationMs > 0L) {
            val progressFraction = (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 4.dp)
            ) {
                Slider(
                    value = if (isScrubbing) scrubProgress else progressFraction,
                    onValueChange = { newValue ->
                        isScrubbing = true
                        scrubProgress = newValue
                    },
                    onValueChangeFinished = {
                        val seekTargetMs = (scrubProgress * durationMs).toLong()
                        exoPlayer?.seekTo(seekTargetMs)
                        isScrubbing = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .padding(horizontal = 12.dp)
                        .testTag("video_progress_scrubber"),
                    colors = SliderDefaults.colors(
                        thumbColor = VyroNeonCyan,
                        activeTrackColor = VyroNeonCyan,
                        inactiveTrackColor = Color.White.copy(alpha = 0.25f)
                    )
                )
            }
        }
    }
}

private fun formatTime(millis: Long): String {
    if (millis <= 0) return "0:00"
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}
