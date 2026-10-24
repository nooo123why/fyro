package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
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
import com.example.data.model.UserProfile
import com.example.data.repository.VyroRepository
import com.example.ui.theme.VyroNeonCyan
import kotlinx.coroutines.launch

@Composable
fun InboxScreen(
    repository: VyroRepository,
    currentUserId: String,
    onOpenChat: (conversationId: String, title: String, memberUids: List<String>) -> Unit,
    onOpenCalls: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var conversations by remember { mutableStateOf<List<Conversation>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var showNewChatDialog by remember { mutableStateOf(false) }
    var availableUsers by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    var newGroupName by remember { mutableStateOf("") }
    var isGroupMode by remember { mutableStateOf(false) }

    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotBlank()) {
            repository.observeConversations(currentUserId).collect { list ->
                conversations = list
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showNewChatDialog = true
                    coroutineScope.launch {
                        availableUsers = repository.searchUsers("").filter { it.userId != currentUserId }
                    }
                },
                containerColor = VyroNeonCyan,
                contentColor = Color.Black,
                modifier = Modifier.testTag("new_chat_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "New Chat")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .testTag("inbox_screen")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Messages",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                IconButton(
                    onClick = onOpenCalls,
                    modifier = Modifier.testTag("calls_history_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Calls",
                        tint = VyroNeonCyan
                    )
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search conversations...", fontSize = 14.sp) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = VyroNeonCyan)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_conversations_input"),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VyroNeonCyan,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            val filteredList = conversations.filter {
                searchQuery.isBlank() ||
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.lastMessageText.contains(searchQuery, ignoreCase = true)
            }

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No conversations yet.",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tap the + button below to start a chat with any creator!",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredList) { conv ->
                        val displayTitle = if (conv.type == "group") {
                            conv.name.ifBlank { "Group Chat" }
                        } else {
                            val otherUid = conv.memberUids.firstOrNull { it != currentUserId } ?: "User"
                            "User ${otherUid.take(6)}"
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .clickable {
                                    onOpenChat(conv.id, displayTitle, conv.memberUids)
                                }
                                .padding(12.dp)
                                .testTag("conversation_item_${conv.id}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(contentAlignment = Alignment.BottomEnd) {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    AsyncImage(
                                        model = "https://picsum.photos/seed/${conv.id.hashCode()}/200",
                                        contentDescription = displayTitle,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                // Online dot
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF22C55E))
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = displayTitle,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = conv.lastMessageText.ifBlank { "No messages yet" },
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // New Chat / Group Chat Dialog
        if (showNewChatDialog) {
            AlertDialog(
                onDismissRequest = { showNewChatDialog = false },
                title = {
                    Text(
                        text = if (isGroupMode) "Create Group Chat" else "Start New Chat",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isGroupMode) "Group Setup" else "Select User",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(onClick = { isGroupMode = !isGroupMode }) {
                                Text(if (isGroupMode) "1-on-1 Chat" else "New Group")
                            }
                        }

                        if (isGroupMode) {
                            OutlinedTextField(
                                value = newGroupName,
                                onValueChange = { newGroupName = it },
                                label = { Text("Group Name") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        Text(
                            text = "Suggested Users:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        val listToPick = if (availableUsers.isNotEmpty()) availableUsers else listOf(
                            UserProfile(userId = "demo_creator_1", username = "sarah_v", displayName = "Sarah Miller"),
                            UserProfile(userId = "demo_creator_2", username = "alex_tech", displayName = "Alex Rivera")
                        )

                        LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                            items(listToPick) { user ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            coroutineScope.launch {
                                                val type = if (isGroupMode) "group" else "direct"
                                                val name = if (isGroupMode) newGroupName.ifBlank { "VYRO Group" } else ""
                                                val result = repository.createConversation(
                                                    otherUserId = user.userId,
                                                    type = type,
                                                    groupName = name
                                                )
                                                showNewChatDialog = false
                                                if (result.isSuccess) {
                                                    val convId = result.getOrThrow()
                                                    val members = listOf(currentUserId, user.userId)
                                                    onOpenChat(convId, user.displayName.ifBlank { user.username }, members)
                                                }
                                            }
                                        }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(VyroNeonCyan.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(user.username.take(1).uppercase(), fontWeight = FontWeight.Bold, color = VyroNeonCyan)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(user.displayName.ifBlank { user.username }, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("@${user.username}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showNewChatDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
