package com.example.ui.navigation

import com.example.data.model.Post

sealed class Screen {
    object Home : Screen() // YouTube-style Long-form video feed
    object Shorts : Screen() // TikTok-style Fullscreen vertical shorts
    object Feed : Screen() // Compatibility alias
    object Create : Screen()
    object Subscriptions : Screen() // Followed creators & user search
    object Discover : Screen()
    object Inbox : Screen()
    object Profile : Screen()
    data class VideoDetail(val post: Post) : Screen() // YouTube watch screen
    data class Chat(val conversationId: String, val title: String, val memberUids: List<String>) : Screen()
    data class Call(val targetName: String, val isVideo: Boolean) : Screen()
    data class StoryView(val initialIndex: Int = 0) : Screen()
    data class CreatorProfile(val userId: String) : Screen()
    object Notifications : Screen()
    object Settings : Screen()
    object AdminDashboard : Screen()
    object Game : Screen() // Viral Tap Reflex Challenge
    object AiAssistant : Screen() // AI Assistant for creators
}

enum class BottomTab {
    HOME, SHORTS, CREATE, SUBSCRIPTIONS, PROFILE
}
