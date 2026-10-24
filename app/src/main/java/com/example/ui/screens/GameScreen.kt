package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VyroNeonCyan
import com.example.ui.theme.VyroAccentPink
import kotlinx.coroutines.delay
import kotlin.random.Random

enum class TargetType(val emoji: String, val points: Int, val isGlitch: Boolean) {
    LIKE("❤️", 100, false),
    FIRE("🔥", 200, false),
    ROCKET("🚀", 300, false),
    STAR("⭐", 500, false),
    GLITCH("💀", -150, true)
}

data class ActiveTarget(
    val id: Long,
    val type: TargetType,
    val xFraction: Float, // 0.1f to 0.85f
    val yFraction: Float, // 0.1f to 0.85f
    val expireTimeMs: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("vyro_game_prefs", Context.MODE_PRIVATE) }
    var highScore by remember { mutableIntStateOf(sharedPrefs.getInt("high_score", 0)) }

    var isGameRunning by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }
    var timeLeftSeconds by remember { mutableIntStateOf(30) }
    var score by remember { mutableIntStateOf(0) }
    var streak by remember { mutableIntStateOf(0) }
    var maxStreak by remember { mutableIntStateOf(0) }
    var targets by remember { mutableStateOf<List<ActiveTarget>>(emptyList()) }
    var lastFeedbackText by remember { mutableStateOf<String?>(null) }
    var lastFeedbackColor by remember { mutableStateOf(VyroNeonCyan) }

    // Multiplier calculation based on streak
    val multiplier = when {
        streak >= 15 -> 5
        streak >= 8 -> 3
        streak >= 4 -> 2
        else -> 1
    }

    // Game loop
    LaunchedEffect(isGameRunning) {
        if (isGameRunning) {
            timeLeftSeconds = 30
            score = 0
            streak = 0
            maxStreak = 0
            isGameOver = false
            targets = emptyList()

            // Main timer loop (30 seconds)
            while (timeLeftSeconds > 0 && isGameRunning) {
                delay(1000L)
                timeLeftSeconds -= 1
            }

            // Game Ended
            isGameRunning = false
            isGameOver = true
            if (score > highScore) {
                highScore = score
                sharedPrefs.edit().putInt("high_score", highScore).apply()
            }
        }
    }

    // Dynamic target spawner loop
    LaunchedEffect(isGameRunning) {
        var targetCounter = 0L
        while (isGameRunning) {
            val now = System.currentTimeMillis()
            // Remove expired targets
            targets = targets.filter { it.expireTimeMs > now }

            // Spawn 1-2 new targets if below capacity
            if (targets.size < 4) {
                val randVal = Random.nextInt(100)
                val type = when {
                    randVal < 35 -> TargetType.LIKE
                    randVal < 60 -> TargetType.FIRE
                    randVal < 80 -> TargetType.ROCKET
                    randVal < 92 -> TargetType.STAR
                    else -> TargetType.GLITCH
                }
                val newTarget = ActiveTarget(
                    id = ++targetCounter,
                    type = type,
                    xFraction = Random.nextFloat().coerceIn(0.12f, 0.78f),
                    yFraction = Random.nextFloat().coerceIn(0.12f, 0.78f),
                    expireTimeMs = now + Random.nextLong(1200L, 2200L)
                )
                targets = targets + newTarget
            }
            delay(500L)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("⚡ Viral Tap Reflex", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("game_back_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Stats Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // High Score Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("👑 High Score: ", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$highScore", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = VyroNeonCyan)
                    }
                }

                // Time remaining chip
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (timeLeftSeconds <= 5 && isGameRunning) Color(0xFFEF4444).copy(alpha = 0.2f) else VyroNeonCyan.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⏳ ", fontSize = 14.sp)
                        Text(
                            "${timeLeftSeconds}s",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (timeLeftSeconds <= 5 && isGameRunning) Color(0xFFEF4444) else VyroNeonCyan
                        )
                    }
                }
            }

            // Score and Combo Multiplier HUD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("SCORE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$score", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = VyroAccentPink)
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("COMBO MULTIPLIER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${multiplier}X", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = if (multiplier > 1) VyroNeonCyan else MaterialTheme.colorScheme.onSurface)
                            if (streak > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("🔥 $streak streak", fontSize = 12.sp, color = Color(0xFFF59E0B), fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // Visual feedback text (+300 2X!)
            Box(modifier = Modifier.height(30.dp), contentAlignment = Alignment.Center) {
                if (lastFeedbackText != null) {
                    Text(
                        text = lastFeedbackText ?: "",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = lastFeedbackColor
                    )
                }
            }

            // Game Play Arena
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                            )
                        )
                    )
                    .testTag("game_arena")
            ) {
                val arenaWidth = maxWidth
                val arenaHeight = maxHeight

                if (!isGameRunning && !isGameOver) {
                    // Start Screen
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("🔥 TAP TO GO VIRAL", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = VyroNeonCyan)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "Tap viral icons fast to score points and build combos!\nAvoid the Glitch Skull 💀 or your streak breaks!",
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            TargetGuideItem("❤️", "+100")
                            TargetGuideItem("🔥", "+200")
                            TargetGuideItem("🚀", "+300")
                            TargetGuideItem("⭐", "+500")
                            TargetGuideItem("💀", "-150")
                        }
                        Spacer(modifier = Modifier.height(26.dp))
                        Button(
                            onClick = { isGameRunning = true },
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .height(52.dp)
                                .testTag("start_game_button"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VyroNeonCyan, contentColor = Color.Black)
                        ) {
                            Text("Start 30s Challenge 🚀", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                } else if (isGameOver) {
                    // Game Over Screen
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        val isNewRecord = score == highScore && score > 0
                        Text(if (isNewRecord) "🎉 NEW HIGH SCORE!" else "⏱️ TIME'S UP!", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = if (isNewRecord) Color(0xFFF59E0B) else VyroNeonCyan)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Final Score: $score pts", fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, color = VyroAccentPink)
                        Text("Max Combo: ${maxStreak}x streak", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Spacer(modifier = Modifier.height(20.dp))

                        val rank = when {
                            score >= 12000 -> "🏆 Viral Legend"
                            score >= 7000 -> "🚀 Trending Creator"
                            score >= 3500 -> "⭐ Rising Star"
                            else -> "🌱 Fresh Talent"
                        }

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = VyroNeonCyan.copy(alpha = 0.15f)
                        ) {
                            Text(
                                "Creator Rank: $rank",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                fontWeight = FontWeight.Bold,
                                color = VyroNeonCyan
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, "I just scored $score pts on VYRO's Viral Tap Challenge! Can you beat my score? 🚀⚡")
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share Score"))
                                },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share", fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { isGameRunning = true },
                                modifier = Modifier.weight(1f).height(48.dp).testTag("play_again_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = VyroNeonCyan, contentColor = Color.Black)
                            ) {
                                Text("Play Again 🔄", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    // Active Game: Render Floating Targets
                    targets.forEach { target ->
                        val targetX = arenaWidth * target.xFraction
                        val targetY = arenaHeight * target.yFraction

                        Box(
                            modifier = Modifier
                                .offset(x = targetX, y = targetY)
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(
                                    if (target.type.isGlitch) Color(0xFFEF4444).copy(alpha = 0.25f)
                                    else MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                                )
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = false, radius = 32.dp)
                                ) {
                                    // Hit target
                                    targets = targets.filter { it.id != target.id }
                                    if (target.type.isGlitch) {
                                        streak = 0
                                        score = (score + target.type.points).coerceAtLeast(0)
                                        lastFeedbackText = "💀 GLITCH! -150"
                                        lastFeedbackColor = Color(0xFFEF4444)
                                    } else {
                                        val gained = target.type.points * multiplier
                                        score += gained
                                        streak += 1
                                        if (streak > maxStreak) maxStreak = streak
                                        lastFeedbackText = "+$gained ${if (multiplier > 1) "${multiplier}x!" else ""}"
                                        lastFeedbackColor = if (multiplier >= 3) VyroAccentPink else VyroNeonCyan
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = target.type.emoji,
                                fontSize = 32.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun TargetGuideItem(emoji: String, pts: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 22.sp)
        Text(pts, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
