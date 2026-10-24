package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val username: String = "",
    val displayName: String = "",
    val bio: String = "",
    val avatarUrl: String = "",
    val followersCount: Long = 0L,
    val followingCount: Long = 0L,
    val postsCount: Long = 0L,
    val isPrivate: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Post(
    @DocumentId val id: String = "",
    val authorId: String = "",
    val authorUsername: String = "",
    val authorAvatarUrl: String = "",
    val mediaUrl: String = "",
    val thumbnailUrl: String = "",
    val caption: String = "",
    val audioTitle: String = "Original Sound - VYRO",
    val videoFormat: String = "short", // "short" (9:16) or "long" (16:9)
    val category: String = "Entertainment",
    val durationSeconds: Long = 15L,
    val viewsCount: Long = 0L,
    val likesCount: Long = 0L,
    val dislikesCount: Long = 0L,
    val commentsCount: Long = 0L,
    val savesCount: Long = 0L,
    val tags: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FollowRecord(
    @DocumentId val id: String = "",
    val followerId: String = "",
    val followingId: String = "",
    val followingUsername: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Comment(
    @DocumentId val id: String = "",
    val postId: String = "",
    val authorId: String = "",
    val authorUsername: String = "",
    val authorAvatarUrl: String = "",
    val text: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class LikeRecord(
    @DocumentId val id: String = "",
    val postId: String = "",
    val userId: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class SavedPost(
    @DocumentId val id: String = "",
    val userId: String = "",
    val postId: String = "",
    @ServerTimestamp val savedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Conversation(
    @DocumentId val id: String = "",
    val type: String = "direct", // "direct" or "group"
    val name: String = "",
    val creatorId: String = "",
    val memberUids: List<String> = emptyList(),
    val lastMessageText: String = "",
    val lastMessageSenderId: String = "",
    @ServerTimestamp val lastMessageTimestamp: Timestamp? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Message(
    @DocumentId val id: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val mediaUrl: String = "",
    val messageType: String = "text", // "text", "image", "video", "audio", "file"
    val memberUids: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Story(
    @DocumentId val id: String = "",
    val authorId: String = "",
    val authorUsername: String = "",
    val authorAvatarUrl: String = "",
    val mediaUrl: String = "",
    val caption: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class NotificationItem(
    @DocumentId val id: String = "",
    val recipientId: String = "",
    val senderId: String = "",
    val senderUsername: String = "",
    val type: String = "like", // "like", "comment", "follow", "message"
    val targetId: String = "",
    val content: String = "",
    val read: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ReportItem(
    @DocumentId val id: String = "",
    val reporterId: String = "",
    val targetType: String = "post", // "post", "comment", "user", "message"
    val targetId: String = "",
    val reason: String = "",
    val description: String = "",
    val status: String = "pending", // "pending", "reviewed", "dismissed"
    @ServerTimestamp val createdAt: Timestamp? = null
)
