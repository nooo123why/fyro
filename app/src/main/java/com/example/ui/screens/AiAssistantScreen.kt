package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VyroAccentPink
import com.example.ui.theme.VyroElectricPurple
import com.example.ui.theme.VyroNeonCyan
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class AiChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantScreen(
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var inputPrompt by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }

    val messages = remember {
        mutableStateListOf(
            AiChatMessage(
                isUser = false,
                text = "Hey! I'm **Vyro AI Assistant** ⚡. I can help you create viral short scripts, generate catchy video titles, find trending hashtags, and grow your channel. What are you working on today?"
            )
        )
    }

    val quickPrompts = listOf(
        "🎬 5 Viral Short Ideas for Me",
        "✍️ Catchy Title & Hashtags for my video",
        "📈 How do I get my first 10K views?",
        "🪙 How to earn Vyro Coins quickly?",
        "🔥 Script a 30s High-Energy Motivation Short"
    )

    fun handleSend(prompt: String) {
        val trimmed = prompt.trim()
        if (trimmed.isBlank() || isGenerating) return

        messages.add(AiChatMessage(isUser = true, text = trimmed))
        inputPrompt = ""
        isGenerating = true

        coroutineScope.launch {
            listState.animateScrollToItem(messages.size - 1)
            delay(1000L) // natural thinking pause

            val response = generateCreatorResponse(trimmed)
            messages.add(AiChatMessage(isUser = false, text = response))
            isGenerating = false
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(VyroAccentPink, VyroElectricPurple))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Vyro AI Assistant", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                            Text("Creator Intelligence & Video Ideas", fontSize = 11.sp, color = VyroNeonCyan)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("ai_assistant_back_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D0F17))
            )
        },
        containerColor = Color(0xFF090A10)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
        ) {
            // Chat conversation messages
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    AiChatBubble(message = msg)
                }

                if (isGenerating) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = VyroNeonCyan,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Vyro AI is crafting recommendations...", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }

            // Quick Prompt Suggestions Carousel
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(quickPrompts) { prompt ->
                    Surface(
                        modifier = Modifier.clickable { handleSend(prompt) },
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF1E2130),
                        border = androidx.compose.foundation.BorderStroke(1.dp, VyroNeonCyan.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = prompt,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            // Bottom Input Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF0D0F17)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputPrompt,
                        onValueChange = { inputPrompt = it },
                        placeholder = { Text("Ask anything about videos or ideas...", fontSize = 14.sp, color = Color.Gray) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ai_assistant_input_field"),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VyroNeonCyan,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            focusedContainerColor = Color(0xFF161926),
                            unfocusedContainerColor = Color(0xFF161926),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { handleSend(inputPrompt) },
                        enabled = inputPrompt.isNotBlank() && !isGenerating,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (inputPrompt.isNotBlank()) VyroAccentPink else Color.White.copy(alpha = 0.15f))
                            .testTag("ai_assistant_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Send,
                            contentDescription = "Send prompt",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AiChatBubble(message: AiChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(VyroElectricPurple),
                contentAlignment = Alignment.Center
            ) {
                Text("⚡", fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(10.dp))
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isUser) 16.dp else 4.dp,
                bottomEnd = if (message.isUser) 4.dp else 16.dp
            ),
            color = if (message.isUser) VyroAccentPink.copy(alpha = 0.25f) else Color(0xFF1E2130),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (message.isUser) VyroAccentPink.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.12f)
            ),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Text(
                text = message.text,
                color = Color.White,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                modifier = Modifier.padding(14.dp)
            )
        }
    }
}

private fun generateCreatorResponse(prompt: String): String {
    val lower = prompt.lowercase()
    return when {
        lower.contains("idea") || lower.contains("viral") -> {
            """🚀 Here are 5 High-Retention Short Concepts for VYRO:

1. **The 3-Second Rule Hook**: "Most creators make this mistake in their first 3 seconds..." Show common blunder vs the fix.
2. **Behind-The-Scenes Contrast**: What people think editing/content creation is like vs the real chaos.
3. **The Unpopular Opinion**: "Why 90% of advice on getting views is actually wrong..."
4. **Speed Tutorial**: Teach one powerful hack in under 25 seconds with fast cuts and sound effects.
5. **The Transformation Arc**: 0 days vs 30 days progress breakdown.

💡 **Pro Tip**: Always put your strongest visual punch within the first 1.5 seconds!"""
        }

        lower.contains("title") || lower.contains("hashtag") -> {
            """✍️ High-Click Title & Hashtag Blueprint:

**Titles**:
• "Stop Doing This If You Want Followers in 2026"
• "The 60-Second Routine That Changed Everything"
• "I Tested This Secret Feature For 7 Days..."

**Trending Hashtags**:
`#VYRO #CreatorEconomy #ViralShorts #Trending2026 #BehindTheScenes #FYP #ForYou`"""
        }

        lower.contains("coin") -> {
            """🪙 **How to Earn & Use Vyro Coins**:

• **Viral Tap Reflex Game**: Play the mini-game on your Home screen! Earn 1 Coin for every 150 points scored.
• **Daily Watch Rewards**: Earn +5 Coins automatically as you watch trending videos.
• **Publishing Videos**: Get +50 Coins every time you upload an authentic short or long-form video.
• **Creator Tipping**: Send gifts (Super Like, Flame Rocket, Diamond Trophy) to your favorite creators!"""
        }

        lower.contains("view") || lower.contains("grow") -> {
            """📈 **Algorithm Growth Strategy for VYRO**:

1. **Watch Time & Completion Rate**: Short videos between 15s–35s have the highest loop completion rate.
2. **First 2 Seconds**: Use visual motion or an intriguing statement before introducing yourself.
3. **Engage with Comments**: Pin the most interesting comment and reply to the first 10 viewers.
4. **Post Consistently**: Upload 1–2 Shorts per day to maintain momentum."""
        }

        else -> {
            """⚡ **Vyro Creator Insight**:

Great question! In modern video platforms like VYRO, the winning formula is:
1. **Hook**: Catch attention instantly.
2. **Story**: Deliver clear value without fluff.
3. **Call-To-Action**: Ask viewers to follow or drop their thoughts in the comments.

Would you like me to write a custom script, generate hashtags, or break down analytics for your next upload?"""
        }
    }
}
