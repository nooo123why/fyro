package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.UserProfile
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

class VyroRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun saveProfile_authenticatedUser_succeeds() {
        runBlocking {
            val uid = signInTestUser(ALICE_EMAIL)
            val repository = VyroRepository(firestore)

            val profile = UserProfile(
                userId = uid,
                username = "alice_test",
                displayName = "Alice Wonderland",
                bio = "Hello VYRO",
                followersCount = 0L,
                followingCount = 0L,
                postsCount = 0L,
                isPrivate = false
            )

            val result = withTimeout(DEFAULT_TIMEOUT_MS) {
                repository.saveProfile(profile)
            }
            assertTrue(result.isSuccess)

            val fetched = withTimeout(DEFAULT_TIMEOUT_MS) {
                repository.getProfile(uid)
            }
            assertNotNull(fetched)
            assertEquals("alice_test", fetched?.username)
        }
    }

    @Test
    fun createPost_authenticatedUser_createsDocument() {
        runBlocking {
            signInTestUser(ALICE_EMAIL)
            val repository = VyroRepository(firestore)

            val result = withTimeout(DEFAULT_TIMEOUT_MS) {
                repository.createPost(
                    caption = "Testing Vyro #social",
                    mediaUrl = "https://example.com/video.mp4",
                    thumbnailUrl = "https://example.com/thumb.jpg",
                    audioTitle = "Vyro Sound Beat",
                    tags = listOf("social", "trending"),
                    authorUsername = "alice_test",
                    authorAvatarUrl = "https://example.com/alice.jpg"
                )
            }
            assertTrue(result.isSuccess)
            assertNotNull(result.getOrNull())
        }
    }

    @Test
    fun unauthenticatedAccess_fails() {
        runBlocking {
            auth.signOut()
            val repository = VyroRepository(firestore)
            assertThrows(IllegalStateException::class.java) {
                repository.requireUserId()
            }
        }
    }

    private companion object {
        val ALICE_EMAIL = "alice_${UUID.randomUUID()}@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
    }
}
