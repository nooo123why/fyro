package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.*
import com.example.data.util.OperationType
import com.example.data.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class VyroRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        try {
            val dbId = context.applicationContext.getString(R.string.firestore_database_id)
            if (dbId.isNotBlank() && dbId != "(default)") {
                FirebaseFirestore.getInstance(dbId)
            } else {
                FirebaseFirestore.getInstance()
            }
        } catch (_: Exception) {
            FirebaseFirestore.getInstance()
        }
    )

    private val auth: com.google.firebase.auth.FirebaseAuth = try {
        com.google.firebase.auth.FirebaseAuth.getInstance(db.app)
    } catch (_: Exception) {
        Firebase.auth
    }
    private val _guestPosts = kotlinx.coroutines.flow.MutableStateFlow<List<Post>>(
        listOf(
            Post(
                id = "spotlight_zaid_1",
                authorId = "creator_zaid",
                authorUsername = "TheDigitalDonZaid",
                authorAvatarUrl = "https://picsum.photos/seed/zaid_don/300",
                mediaUrl = "asset:///zaid_digital_don.mp4",
                thumbnailUrl = "https://picsum.photos/seed/zaid_thumb/720/1280",
                caption = "Rule #1: Loyalty. Never confuse loyalty with obedience. The Don's Rules ☕✨ #TheDigitalDon #Zaid #Shorts",
                audioTitle = "Original Voice - Zaid The Digital Don",
                videoFormat = "short",
                category = "Entertainment",
                durationSeconds = 54L,
                viewsCount = 245000L,
                likesCount = 38400L,
                commentsCount = 1420L,
                tags = listOf("TheDigitalDon", "Zaid", "Shorts", "Loyalty", "Trending")
            ),
            Post(
                id = "seed_short_1",
                authorId = "creator_alex",
                authorUsername = "alex_vyro",
                authorAvatarUrl = "https://picsum.photos/seed/alex_vyro/200",
                mediaUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                thumbnailUrl = "https://picsum.photos/seed/short1/720/1280",
                caption = "Futuristic Cyber Stunts in Neon Alley 🔥 #shorts #trending",
                audioTitle = "Cyber City - Neon Beats",
                videoFormat = "short",
                category = "Entertainment",
                durationSeconds = 15L,
                viewsCount = 38400L,
                likesCount = 4210L,
                commentsCount = 184L,
                tags = listOf("shorts", "trending", "cyber")
            ),
            Post(
                id = "seed_short_2",
                authorId = "creator_maya",
                authorUsername = "maya_vids",
                authorAvatarUrl = "https://picsum.photos/seed/maya_vids/200",
                mediaUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                thumbnailUrl = "https://picsum.photos/seed/short2/720/1280",
                caption = "4K Mountain Escapes & Glacial Lakes 🌲 #shorts #nature",
                audioTitle = "Chill Ambient Waves",
                videoFormat = "short",
                category = "Entertainment",
                durationSeconds = 15L,
                viewsCount = 19200L,
                likesCount = 1980L,
                commentsCount = 76L,
                tags = listOf("shorts", "nature", "4k")
            ),
            Post(
                id = "seed_short_3",
                authorId = "creator_sam",
                authorUsername = "sam_drone",
                authorAvatarUrl = "https://picsum.photos/seed/sam_drone/200",
                mediaUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
                thumbnailUrl = "https://picsum.photos/seed/short3/720/1280",
                caption = "Tokyo Midnight Street Skaters 🛹 #skate #shorts",
                audioTitle = "Tokyo Lo-Fi Drift",
                videoFormat = "short",
                category = "Entertainment",
                durationSeconds = 15L,
                viewsCount = 52100L,
                likesCount = 6120L,
                commentsCount = 312L,
                tags = listOf("shorts", "skate", "tokyo")
            ),
            Post(
                id = "seed_long_1",
                authorId = "creator_maya",
                authorUsername = "maya_vids",
                authorAvatarUrl = "https://picsum.photos/seed/maya_vids/200",
                mediaUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                thumbnailUrl = "https://picsum.photos/seed/long1/1280/720",
                caption = "Big Buck Bunny: The 4K Animation Legend That Defined Open Source CGI",
                audioTitle = "Original Orchestral Score",
                videoFormat = "long",
                category = "Entertainment",
                durationSeconds = 596L,
                viewsCount = 142800L,
                likesCount = 12900L,
                commentsCount = 428L,
                tags = listOf("animation", "4k", "cgi", "blender")
            ),
            Post(
                id = "seed_long_2",
                authorId = "creator_alex",
                authorUsername = "alex_vyro",
                authorAvatarUrl = "https://picsum.photos/seed/alex_vyro/200",
                mediaUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                thumbnailUrl = "https://picsum.photos/seed/long2/1280/720",
                caption = "Inside The Cyber Machine Realm: Futuristic AI Systems and World Construction",
                audioTitle = "Ambient Electronic Synth",
                videoFormat = "long",
                category = "Tech",
                durationSeconds = 654L,
                viewsCount = 89200L,
                likesCount = 7430L,
                commentsCount = 210L,
                tags = listOf("tech", "scifi", "future", "ai")
            ),
            Post(
                id = "seed_long_3",
                authorId = "creator_sam",
                authorUsername = "sam_drone",
                authorAvatarUrl = "https://picsum.photos/seed/sam_drone/200",
                mediaUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4",
                thumbnailUrl = "https://picsum.photos/seed/long3/1280/720",
                caption = "Coast-To-Coast Supercar Rally 2026: 3000 Miles of Raw Horsepower",
                audioTitle = "V8 Engine Symphony",
                videoFormat = "long",
                category = "Gaming",
                durationSeconds = 120L,
                viewsCount = 76500L,
                likesCount = 5890L,
                commentsCount = 195L,
                tags = listOf("cars", "rally", "motorsport")
            )
        )
    )
    val guestPosts = _guestPosts

    fun isUserSignedIn(): Boolean = auth.currentUser != null

    fun getEffectiveUserId(): String = auth.currentUser?.uid ?: "guest_user"

    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before performing this cloud action.")
    }

    suspend fun testFirebaseConnection(): Result<String> {
        return try {
            val startTime = System.currentTimeMillis()
            val snap = db.collection("posts").limit(1).get().await()
            val latency = System.currentTimeMillis() - startTime
            Result.success("Connected to vyro-753ab (${latency}ms latency)")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- PROFILES ---
    suspend fun getProfile(userId: String): UserProfile? {
        val path = "users/$userId"
        return try {
            val doc = db.collection("users").document(userId).get().await()
            doc.toObject(UserProfile::class.java)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, path)
            null
        }
    }

    fun observeProfile(userId: String): Flow<UserProfile?> = flow {
        val path = "users/$userId"
        val docRef = db.collection("users").document(userId)
        docRef.snapshots()
            .map { it.toObject(UserProfile::class.java) }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.GET, path)
                throw e
            }
            .collect { emit(it) }
    }

    suspend fun saveProfile(profile: UserProfile): Result<Unit> {
        val path = "users"
        return try {
            val uid = requireUserId()
            val docRef = db.collection(path).document(uid)
            val existing = docRef.get().await()
            val payload = mutableMapOf<String, Any>(
                "userId" to uid,
                "username" to profile.username,
                "displayName" to profile.displayName,
                "bio" to profile.bio,
                "avatarUrl" to profile.avatarUrl,
                "followersCount" to profile.followersCount,
                "followingCount" to profile.followingCount,
                "postsCount" to profile.postsCount,
                "isPrivate" to profile.isPrivate,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (!existing.exists()) {
                payload["createdAt"] = FieldValue.serverTimestamp()
            }
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun searchUsers(query: String): List<UserProfile> {
        val path = "users"
        return try {
            val snapshot = db.collection(path)
                .limit(50)
                .get()
                .await()
            val list = snapshot.toObjects(UserProfile::class.java)
            val trimmed = query.trim()
            if (trimmed.isBlank()) list else list.filter {
                it.username.contains(trimmed, ignoreCase = true) ||
                it.displayName.contains(trimmed, ignoreCase = true) ||
                it.bio.contains(trimmed, ignoreCase = true)
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            emptyList()
        }
    }

    suspend fun searchPosts(query: String): List<Post> {
        val path = "posts"
        val trimmed = query.trim().removePrefix("#")
        val guestMatches = if (trimmed.isBlank()) _guestPosts.value else _guestPosts.value.filter { post ->
            post.caption.contains(trimmed, ignoreCase = true) ||
            post.authorUsername.contains(trimmed, ignoreCase = true) ||
            post.audioTitle.contains(trimmed, ignoreCase = true) ||
            post.tags.any { it.contains(trimmed, ignoreCase = true) }
        }
        return try {
            val snapshot = db.collection(path)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()
            val list = snapshot.toObjects(Post::class.java)
            val firestoreMatches = if (trimmed.isBlank()) list else list.filter { post ->
                post.caption.contains(trimmed, ignoreCase = true) ||
                post.authorUsername.contains(trimmed, ignoreCase = true) ||
                post.audioTitle.contains(trimmed, ignoreCase = true) ||
                post.tags.any { it.contains(trimmed, ignoreCase = true) }
            }
            (guestMatches + firestoreMatches).distinctBy { it.id.ifBlank { it.mediaUrl } }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            guestMatches
        }
    }

    // --- FEED & POSTS ---
    fun observePosts(): Flow<List<Post>> = flow {
        val path = "posts"
        try {
            db.collection(path)
                .limit(100)
                .snapshots()
                .map { snap ->
                    val firestorePosts = snap.toObjects(Post::class.java)
                    val merged = (_guestPosts.value + firestorePosts).distinctBy { it.id.ifBlank { it.mediaUrl } }
                    // Keep Zaid spotlight post pinned at the top if present, then sort the rest by time
                    val zaidPost = merged.find { it.id == "spotlight_zaid_1" }
                    val others = merged.filter { it.id != "spotlight_zaid_1" }
                        .sortedByDescending { it.createdAt?.toDate()?.time ?: 0L }
                    if (zaidPost != null) listOf(zaidPost) + others else others
                }
                .catch { e ->
                    android.util.Log.w("VyroRepository", "observePosts error: ${e.message}")
                    emit(_guestPosts.value)
                }
                .collect { emit(it) }
        } catch (e: Exception) {
            android.util.Log.w("VyroRepository", "observePosts exception: ${e.message}")
            emit(_guestPosts.value)
        }
    }

    suspend fun createPost(
        caption: String,
        mediaUrl: String,
        thumbnailUrl: String,
        audioTitle: String,
        tags: List<String>,
        authorUsername: String,
        authorAvatarUrl: String,
        videoFormat: String = "short",
        category: String = "Entertainment",
        durationSeconds: Long = 15L
    ): Result<String> {
        val newId = "post_${System.currentTimeMillis()}"
        val uid = auth.currentUser?.uid ?: "guest_user"

        // 1. Instant local optimistic update: video appears immediately!
        val newPost = Post(
            id = newId,
            authorId = uid,
            authorUsername = authorUsername.ifBlank { "creator" },
            authorAvatarUrl = authorAvatarUrl.ifBlank { "https://picsum.photos/seed/$authorUsername/300" },
            mediaUrl = mediaUrl,
            thumbnailUrl = thumbnailUrl.ifBlank { mediaUrl },
            caption = caption,
            audioTitle = audioTitle.ifBlank { "Original Sound - VYRO" },
            videoFormat = videoFormat,
            category = category,
            durationSeconds = durationSeconds,
            likesCount = 0L,
            viewsCount = 1L,
            commentsCount = 0L,
            savesCount = 0L,
            tags = tags
        )
        _guestPosts.value = listOf(newPost) + _guestPosts.value

        // 2. Authoritative background cloud sync to Firestore so all accounts see it
        if (auth.currentUser != null) {
            try {
                val postRef = db.collection("posts").document(newId)
                val payload = mapOf(
                    "authorId" to uid,
                    "authorUsername" to authorUsername.ifBlank { "creator" },
                    "authorAvatarUrl" to authorAvatarUrl.ifBlank { "https://picsum.photos/seed/$authorUsername/300" },
                    "mediaUrl" to mediaUrl,
                    "thumbnailUrl" to thumbnailUrl.ifBlank { mediaUrl },
                    "caption" to caption,
                    "audioTitle" to audioTitle.ifBlank { "Original Sound - VYRO" },
                    "videoFormat" to videoFormat,
                    "category" to category,
                    "durationSeconds" to durationSeconds,
                    "likesCount" to 0L,
                    "viewsCount" to 1L,
                    "dislikesCount" to 0L,
                    "commentsCount" to 0L,
                    "savesCount" to 0L,
                    "tags" to tags,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                postRef.set(payload).await()

                try {
                    db.collection("users").document(uid).update(
                        "postsCount", FieldValue.increment(1),
                        "updatedAt", FieldValue.serverTimestamp()
                    ).await()
                } catch (_: Exception) {
                    // Non-critical if user profile document not initialized yet
                }
            } catch (e: Exception) {
                android.util.Log.e("VyroRepository", "Error syncing post to Firestore: ${e.message}", e)
            }
        }
        return Result.success(newId)
    }

    suspend fun deletePost(postId: String): Result<Unit> {
        return try {
            _guestPosts.value = _guestPosts.value.filter { it.id != postId }
            db.collection("posts").document(postId).delete().await()
            val uid = auth.currentUser?.uid
            if (uid != null) {
                try {
                    db.collection("users").document(uid).update(
                        "postsCount", FieldValue.increment(-1),
                        "updatedAt", FieldValue.serverTimestamp()
                    ).await()
                } catch (_: Exception) {}
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAccountData(userId: String): Result<Unit> {
        return try {
            // 1. Delete all user posts in Firestore
            val userPostsSnap = db.collection("posts")
                .whereEqualTo("authorId", userId)
                .get()
                .await()
            for (doc in userPostsSnap.documents) {
                doc.reference.delete().await()
            }
            // 2. Remove user posts from local in-memory flow
            _guestPosts.value = _guestPosts.value.filter { it.authorId != userId }

            // 3. Delete user profile document
            db.collection("users").document(userId).delete().await()

            // 4. Delete Firebase Auth user credentials
            val user = auth.currentUser
            if (user != null && user.uid == userId) {
                user.delete().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("VyroRepository", "Error deleting account data: ${e.message}", e)
            Result.failure(e)
        }
    }

    // --- FOLLOW / SUBSCRIPTIONS ---
    private val _followingIds = kotlinx.coroutines.flow.MutableStateFlow<Set<String>>(setOf("creator_alex", "creator_maya"))
    val followingIds: Flow<Set<String>> = _followingIds

    fun isFollowing(userId: String): Boolean = _followingIds.value.contains(userId)

    suspend fun toggleFollow(targetUserId: String, targetUsername: String = ""): Boolean {
        val current = _followingIds.value
        val willFollow = !current.contains(targetUserId)
        _followingIds.value = if (willFollow) current + targetUserId else current - targetUserId

        val currentUid = auth.currentUser?.uid
        if (currentUid != null) {
            val followDoc = db.collection("users").document(currentUid).collection("following").document(targetUserId)
            if (willFollow) {
                followDoc.set(mapOf(
                    "followerId" to currentUid,
                    "followingId" to targetUserId,
                    "followingUsername" to targetUsername,
                    "createdAt" to FieldValue.serverTimestamp()
                ))
                db.collection("users").document(targetUserId).update("followersCount", FieldValue.increment(1))
            } else {
                followDoc.delete()
                db.collection("users").document(targetUserId).update("followersCount", FieldValue.increment(-1))
            }
        }
        return willFollow
    }

    suspend fun incrementViewCount(postId: String) {
        val list = _guestPosts.value
        _guestPosts.value = list.map { if (it.id == postId) it.copy(viewsCount = it.viewsCount + 1) else it }
        try {
            db.collection("posts").document(postId).update("viewsCount", FieldValue.increment(1)).await()
        } catch (_: Exception) {}
    }

    suspend fun toggleLike(post: Post): Boolean {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            val current = _guestPosts.value
            val target = current.find { it.id == post.id }
            if (target != null) {
                val newLikes = if (target.likesCount > 0) target.likesCount - 1 else target.likesCount + 1
                _guestPosts.value = current.map { if (it.id == post.id) it.copy(likesCount = newLikes) else it }
                return newLikes > target.likesCount
            }
            return true
        }
        val likeRef = db.collection("posts").document(post.id).collection("likes").document(uid)
        return try {
            val existing = likeRef.get().await()
            if (existing.exists()) {
                likeRef.delete().await()
                db.collection("posts").document(post.id).update(
                    "likesCount", FieldValue.increment(-1)
                ).await()
                false
            } else {
                likeRef.set(
                    mapOf(
                        "postId" to post.id,
                        "userId" to uid,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                db.collection("posts").document(post.id).update(
                    "likesCount", FieldValue.increment(1)
                ).await()
                true
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "posts/${post.id}/likes/$uid")
            false
        }
    }


    suspend fun isPostLiked(postId: String): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return try {
            db.collection("posts").document(postId).collection("likes").document(uid).get().await().exists()
        } catch (_: Exception) {
            false
        }
    }

    fun observeComments(postId: String): Flow<List<Comment>> = flow {
        val path = "posts/$postId/comments"
        db.collection("posts").document(postId).collection("comments")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .snapshots()
            .map { it.toObjects(Comment::class.java) }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.LIST, path)
                throw e
            }
            .collect { emit(it) }
    }

    suspend fun addComment(postId: String, text: String, username: String, avatarUrl: String): Result<Unit> {
        val path = "posts/$postId/comments"
        return try {
            val uid = requireUserId()
            val commentRef = db.collection("posts").document(postId).collection("comments").document()
            commentRef.set(
                mapOf(
                    "postId" to postId,
                    "authorId" to uid,
                    "authorUsername" to username,
                    "authorAvatarUrl" to avatarUrl,
                    "text" to text,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            db.collection("posts").document(postId).update("commentsCount", FieldValue.increment(1)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    // --- CONVERSATIONS & MESSAGES ---
    fun observeConversations(userId: String): Flow<List<Conversation>> = flow {
        val path = "conversations"
        db.collection(path)
            .whereArrayContains("memberUids", userId)
            .snapshots()
            .map { it.toObjects(Conversation::class.java) }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.LIST, path)
                throw e
            }
            .collect { emit(it) }
    }

    suspend fun createConversation(
        otherUserId: String,
        type: String = "direct",
        groupName: String = ""
    ): Result<String> {
        val path = "conversations"
        return try {
            val uid = requireUserId()
            val members = if (type == "direct") listOf(uid, otherUserId).distinct() else listOf(uid, otherUserId)
            val convRef = db.collection(path).document()
            val payload = mapOf(
                "type" to type,
                "name" to groupName,
                "creatorId" to uid,
                "memberUids" to members,
                "lastMessageText" to "Started conversation",
                "lastMessageSenderId" to uid,
                "lastMessageTimestamp" to FieldValue.serverTimestamp(),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            convRef.set(payload).await()
            Result.success(convRef.id)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    fun observeMessages(conversationId: String): Flow<List<Message>> = flow {
        val path = "conversations/$conversationId/messages"
        db.collection("conversations").document(conversationId).collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .snapshots()
            .map { it.toObjects(Message::class.java) }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.LIST, path)
                throw e
            }
            .collect { emit(it) }
    }

    suspend fun sendMessage(
        conversationId: String,
        text: String,
        senderName: String,
        messageType: String = "text",
        mediaUrl: String = "",
        memberUids: List<String>
    ): Result<Unit> {
        val path = "conversations/$conversationId/messages"
        return try {
            val uid = requireUserId()
            val msgRef = db.collection("conversations").document(conversationId).collection("messages").document()
            val payload = mapOf(
                "conversationId" to conversationId,
                "senderId" to uid,
                "senderName" to senderName,
                "text" to text,
                "mediaUrl" to mediaUrl,
                "messageType" to messageType,
                "memberUids" to memberUids,
                "createdAt" to FieldValue.serverTimestamp()
            )
            msgRef.set(payload).await()

            // Update conversation last message preview
            val updatePayload = mapOf<String, Any>(
                "lastMessageText" to text,
                "lastMessageSenderId" to uid,
                "lastMessageTimestamp" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("conversations").document(conversationId).update(updatePayload).await()

            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    // --- STORIES ---
    fun observeStories(): Flow<List<Story>> = flow {
        val path = "stories"
        db.collection(path)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(20)
            .snapshots()
            .map { it.toObjects(Story::class.java) }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.LIST, path)
                throw e
            }
            .collect { emit(it) }
    }

    suspend fun createStory(mediaUrl: String, caption: String, authorUsername: String, avatarUrl: String): Result<Unit> {
        val path = "stories"
        return try {
            val uid = requireUserId()
            val docRef = db.collection(path).document()
            val payload = mapOf(
                "authorId" to uid,
                "authorUsername" to authorUsername,
                "authorAvatarUrl" to avatarUrl,
                "mediaUrl" to mediaUrl,
                "caption" to caption,
                "createdAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    // --- NOTIFICATIONS ---
    fun observeNotifications(userId: String): Flow<List<NotificationItem>> = flow {
        val path = "notifications"
        db.collection(path)
            .whereEqualTo("recipientId", userId)
            .snapshots()
            .map { it.toObjects(NotificationItem::class.java) }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.LIST, path)
                throw e
            }
            .collect { emit(it) }
    }

    // --- REPORTS ---
    suspend fun submitReport(targetType: String, targetId: String, reason: String, description: String): Result<Unit> {
        val path = "reports"
        return try {
            val uid = requireUserId()
            val ref = db.collection(path).document()
            ref.set(
                mapOf(
                    "reporterId" to uid,
                    "targetType" to targetType,
                    "targetId" to targetId,
                    "reason" to reason,
                    "description" to description,
                    "status" to "pending",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }
}
