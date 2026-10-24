package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.example.data.model.Message
import com.example.data.repository.VyroRepository
import com.example.ui.theme.VyroDeepCharcoal
import com.example.ui.theme.VyroElectricPurple
import com.example.ui.theme.VyroNeonCyan
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    conversationId: String,
    title: String,
    memberUids: List<String>,
    currentUserId: String,
    currentUsername: String,
    repository: VyroRepository,
    onBack: () -> Unit,
    onStartCall: (targetName: String, isVideo: Boolean) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var messageText by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    val listState = rememberLazyListState()
    var isRecordingAudio by remember { mutableStateOf(false) }

    val quickReactions = listOf("❤️", "🔥", "😂", "👍", "🚀", "👏")

    LaunchedEffect(conversationId) {
        repository.observeMessages(conversationId).collect { list ->
            messages = list
            if (list.isNotEmpty()) {
                listState.animateScrollToItem(list.size - 1)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(VyroNeonCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(title.take(1).uppercase(), fontWeight = FontWeight.Bold, color = VyroNeonCyan)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("Online", fontSize = 11.sp, color = Color(0xFF22C55E))
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("chat_back_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Voice Call
                    IconButton(
                        onClick = { onStartCall(title, false) },
                        modifier = Modifier.testTag("chat_audio_call_button")
                    ) {
                        Icon(imageVector = Icons.Default.Phone, contentDescription = "Voice Call", tint = VyroNeonCyan)
                    }
                    // Video Call
                    IconButton(
                        onClick = { onStartCall(title, true) },
                        modifier = Modifier.testTag("chat_video_call_button")
                    ) {
                        Icon(imageVector = Icons.Default.Videocam, contentDescription = "Video Call", tint = VyroNeonCyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("chat_screen")
        ) {
            // Messages list
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (messages.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No messages here yet. Say hello!",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(messages) { msg ->
                            val isMe = msg.senderId == currentUserId
                            MessageBubble(message = msg, isMe = isMe)
                        }
                    }
                }
            }

            // Quick emoji reactions bar
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(quickReactions) { emoji ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                coroutineScope.launch {
                                    repository.sendMessage(
                                        conversationId = conversationId,
                                        text = emoji,
                                        senderName = currentUsername,
                                        messageType = "text",
                                        memberUids = memberUids
                                    )
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(text = emoji, fontSize = 16.sp)
                    }
                }
            }

            // Check if current user is following the recipient
            val otherMemberUid = memberUids.firstOrNull { it != currentUserId } ?: ""
            val followingIds by repository.followingIds.collectAsState(initial = emptySet())
            val isFollowingRecipient = otherMemberUid.isBlank() || followingIds.contains(otherMemberUid)

            if (!isFollowingRecipient) {
                // Locked: Must follow creator to send messages
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF161926),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VyroNeonCyan.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = VyroNeonCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Messaging is locked", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                Text("You can only message $title if you follow them", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                            }
                        }
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    repository.toggleFollow(otherMemberUid, title)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = VyroNeonCyan, contentColor = Color.Black),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Follow to Chat", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            } else {
                // Unlocked Composer bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Send Photo quick attach
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                repository.sendMessage(
                                    conversationId = conversationId,
                                    text = "📷 Shared photo",
                                    senderName = currentUsername,
                                    messageType = "image",
                                    mediaUrl = "https://picsum.photos/seed/chat${System.currentTimeMillis()}/600/600",
                                    memberUids = memberUids
                                )
                            }
                        },
                        modifier = Modifier.testTag("attach_photo_button")
                    ) {
                        Icon(imageVector = Icons.Default.Image, contentDescription = "Photo", tint = VyroNeonCyan)
                    }

                    // Voice note recorder button
                    IconButton(
                        onClick = {
                            isRecordingAudio = !isRecordingAudio
                            if (!isRecordingAudio) {
                                coroutineScope.launch {
                                    repository.sendMessage(
                                        conversationId = conversationId,
                                        text = "🎤 Voice message (0:14)",
                                        senderName = currentUsername,
                                        messageType = "audio",
                                        memberUids = memberUids
                                    )
                                }
                            }
                        },
                        modifier = Modifier.testTag("voice_note_button")
                    ) {
                        Icon(
                            imageVector = if (isRecordingAudio) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = "Voice note",
                            tint = if (isRecordingAudio) Color.Red else VyroNeonCyan
                        )
                    }

                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = {
                            Text(if (isRecordingAudio) "Recording voice note..." else "Type a message...", fontSize = 14.sp)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("message_input_field"),
                        maxLines = 4,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VyroNeonCyan,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                val textToSend = messageText
                                messageText = ""
                                coroutineScope.launch {
                                    repository.sendMessage(
                                        conversationId = conversationId,
                                        text = textToSend,
                                        senderName = currentUsername,
                                        messageType = "text",
                                        memberUids = memberUids
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(VyroNeonCyan)
                            .testTag("send_message_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message, isMe: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isMe) 16.dp else 4.dp,
                        bottomEnd = if (isMe) 4.dp else 16.dp
                    )
                )
                .background(
                    if (isMe) {
                        Brush.linearGradient(listOf(VyroElectricPurple, VyroElectricPurple.copy(alpha = 0.85f)))
                    } else {
                        Brush.linearGradient(listOf(Color(0xFF23283E), Color(0xFF1E2235)))
                    }
                )
                .padding(12.dp)
        ) {
            Column {
                if (!isMe) {
                    Text(
                        text = message.senderName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = VyroNeonCyan,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                if (message.messageType == "image" && message.mediaUrl.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        AsyncImage(
                            model = message.mediaUrl,
                            contentDescription = "Image attachment",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Text(
                    text = message.text,
                    fontSize = 14.sp,
                    color = Color.White
                )

                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Just now",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    if (isMe) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Read",
                            tint = VyroNeonCyan,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
