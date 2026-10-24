package com.example.data.util

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class VyroCoinsManager(context: Context) {
    private val prefs = context.getSharedPreferences("vyro_coins_prefs", Context.MODE_PRIVATE)
    private val _coinsFlow = MutableStateFlow(prefs.getInt("coins_balance", 500))
    val coinsFlow: StateFlow<Int> = _coinsFlow

    fun getCoinsBalance(): Int = _coinsFlow.value

    fun addCoins(amount: Int, reason: String = "") {
        val newBalance = _coinsFlow.value + amount
        prefs.edit().putInt("coins_balance", newBalance).apply()
        _coinsFlow.value = newBalance
    }

    fun spendCoins(amount: Int, reason: String = ""): Boolean {
        if (_coinsFlow.value >= amount) {
            val newBalance = _coinsFlow.value - amount
            prefs.edit().putInt("coins_balance", newBalance).apply()
            _coinsFlow.value = newBalance
            return true
        }
        return false
    }

    fun awardGameReward(score: Int): Int {
        val coinsEarned = (score / 150).coerceAtLeast(1)
        addCoins(coinsEarned, "Viral Tap Game reward")
        return coinsEarned
    }

    fun awardWatchReward(): Int {
        val coinsEarned = 5
        addCoins(coinsEarned, "Watching video reward")
        return coinsEarned
    }

    fun awardUploadReward(): Int {
        val coinsEarned = 50
        addCoins(coinsEarned, "Published new video")
        return coinsEarned
    }
}
