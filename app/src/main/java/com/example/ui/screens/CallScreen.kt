package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.VyroDeepCharcoal
import com.example.ui.theme.VyroElectricPurple
import com.example.ui.theme.VyroNeonCyan
import kotlinx.coroutines.delay

@Composable
fun CallScreen(
    targetName: String,
    isVideo: Boolean,
    onEndCall: () -> Unit
) {
    var isMuted by remember { mutableStateOf(false) }
    var isCameraOff by remember { mutableStateOf(false) }
    var isSpeakerOn by remember { mutableStateOf(true) }
    var callSeconds by remember { mutableIntStateOf(0) }
    var callState by remember { mutableStateOf("Connecting...") }

    // Call timer simulation
    LaunchedEffect(Unit) {
        delay(1500)
        callState = "Connected"
        while (true) {
            delay(1000)
            callSeconds += 1
        }
    }

    val minutes = callSeconds / 60
    val seconds = callSeconds % 60
    val durationText = String.format("%02d:%02d", minutes, seconds)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VyroDeepCharcoal)
            .testTag("call_screen")
    ) {
        // Video mode preview simulation
        if (isVideo && !isCameraOff) {
            AsyncImage(
                model = "https://picsum.photos/seed/${targetName.hashCode()}/720/1280",
                contentDescription = "Video stream",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Self PIP camera box in upper corner
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 40.dp, end = 20.dp)
                    .size(width = 100.dp, height = 150.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.DarkGray)
                    .border(2.dp, VyroNeonCyan, RoundedCornerShape(14.dp))
            ) {
                AsyncImage(
                    model = "https://picsum.photos/seed/self_cam/300/450",
                    contentDescription = "Self preview",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // Dark gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent, Color.Black.copy(alpha = 0.85f))
                    )
                )
        )

        // Caller Info Header
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isVideo || isCameraOff) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .border(3.dp, VyroNeonCyan, CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    AsyncImage(
                        model = "https://picsum.photos/seed/${targetName.hashCode()}/200",
                        contentDescription = targetName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            Text(
                text = targetName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (callState == "Connected") durationText else callState,
                fontSize = 15.sp,
                color = if (callState == "Connected") Color(0xFF22C55E) else VyroNeonCyan
            )

            Text(
                text = if (isVideo) "End-to-End Encrypted Video Call" else "End-to-End Encrypted Voice Call",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Call Action Buttons Rail at bottom
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 50.dp, start = 24.dp, end = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mute Button
            IconButton(
                onClick = { isMuted = !isMuted },
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(if (isMuted) Color.White else Color.Black.copy(alpha = 0.5f))
                    .testTag("call_mute_button")
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Mute",
                    tint = if (isMuted) Color.Black else Color.White
                )
            }

            if (isVideo) {
                // Camera Toggle
                IconButton(
                    onClick = { isCameraOff = !isCameraOff },
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(if (isCameraOff) Color.White else Color.Black.copy(alpha = 0.5f))
                        .testTag("call_camera_button")
                ) {
                    Icon(
                        imageVector = if (isCameraOff) Icons.Default.VideocamOff else Icons.Default.Videocam,
                        contentDescription = "Camera",
                        tint = if (isCameraOff) Color.Black else Color.White
                    )
                }

                // Switch Camera
                IconButton(
                    onClick = { /* Flip camera */ },
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .testTag("call_switch_camera_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.FlipCameraAndroid,
                        contentDescription = "Flip camera",
                        tint = Color.White
                    )
                }
            } else {
                // Speaker Toggle
                IconButton(
                    onClick = { isSpeakerOn = !isSpeakerOn },
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(if (isSpeakerOn) VyroNeonCyan else Color.Black.copy(alpha = 0.5f))
                        .testTag("call_speaker_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Speaker",
                        tint = if (isSpeakerOn) Color.Black else Color.White
                    )
                }
            }

            // End Call Button (Red)
            IconButton(
                onClick = onEndCall,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444))
                    .testTag("end_call_button")
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "End Call",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
