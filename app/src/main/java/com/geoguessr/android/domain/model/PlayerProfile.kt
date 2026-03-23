package com.geoguessr.android.domain.model

data class PlayerProfile(
    val userId: String = "",
    val nickname: String = "",
    val totalScore: Int = 0,
    val gamesPlayed: Int = 0,
    val bestScore: Int = 0,
    val gameHistory: List<GameSession> = emptyList()
)
