package com.example.data.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GuestSessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("vyro_guest_session", Context.MODE_PRIVATE)

    private val _isGuestMode = MutableStateFlow(isGuestSessionValid())
    val isGuestMode: StateFlow<Boolean> = _isGuestMode.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(getRemainingSeconds())
    val remainingSeconds: StateFlow<Long> = _remainingSeconds.asStateFlow()

    fun startGuestSession() {
        val now = System.currentTimeMillis()
        val oneHourLater = now + (60 * 60 * 1000L) // Exactly 1 hour
        prefs.edit()
            .putBoolean(KEY_GUEST_ACTIVE, true)
            .putLong(KEY_EXPIRY_TIME, oneHourLater)
            .putString(KEY_GUEST_NAME, "Guest Explorer")
            .putString(KEY_GUEST_USERNAME, "guest_vyro")
            .putString(KEY_GUEST_AVATAR, "https://picsum.photos/seed/guest_avatar/300")
            .apply()
        _isGuestMode.value = true
        _remainingSeconds.value = 3600L
    }

    fun endGuestSession() {
        prefs.edit().clear().apply()
        _isGuestMode.value = false
        _remainingSeconds.value = 0L
    }

    fun isGuestSessionValid(): Boolean {
        val active = prefs.getBoolean(KEY_GUEST_ACTIVE, false)
        if (!active) return false
        val expiry = prefs.getLong(KEY_EXPIRY_TIME, 0L)
        return System.currentTimeMillis() < expiry
    }

    fun getRemainingSeconds(): Long {
        if (!prefs.getBoolean(KEY_GUEST_ACTIVE, false)) return 0L
        val expiry = prefs.getLong(KEY_EXPIRY_TIME, 0L)
        val diff = (expiry - System.currentTimeMillis()) / 1000L
        return if (diff > 0) diff else 0L
    }

    fun updateRemainingTime() {
        val remaining = getRemainingSeconds()
        _remainingSeconds.value = remaining
        if (remaining <= 0 && _isGuestMode.value) {
            _isGuestMode.value = false
        }
    }

    fun getGuestDisplayName(): String {
        return prefs.getString(KEY_GUEST_NAME, "Guest Explorer") ?: "Guest Explorer"
    }

    fun getGuestUsername(): String {
        return prefs.getString(KEY_GUEST_USERNAME, "guest_vyro") ?: "guest_vyro"
    }

    fun getGuestAvatar(): String {
        return prefs.getString(KEY_GUEST_AVATAR, "https://picsum.photos/seed/guest_avatar/300")
            ?: "https://picsum.photos/seed/guest_avatar/300"
    }

    fun updateGuestProfile(displayName: String, bio: String) {
        prefs.edit()
            .putString(KEY_GUEST_NAME, displayName)
            .putString(KEY_GUEST_BIO, bio)
            .apply()
    }

    fun getGuestBio(): String {
        return prefs.getString(KEY_GUEST_BIO, "Exploring VYRO in 1-Hour Guest Mode ⚡")
            ?: "Exploring VYRO in 1-Hour Guest Mode ⚡"
    }

    companion object {
        private const val KEY_GUEST_ACTIVE = "guest_active"
        private const val KEY_EXPIRY_TIME = "guest_expiry_time"
        private const val KEY_GUEST_NAME = "guest_name"
        private const val KEY_GUEST_USERNAME = "guest_username"
        private const val KEY_GUEST_AVATAR = "guest_avatar"
        private const val KEY_GUEST_BIO = "guest_bio"
    }
}
