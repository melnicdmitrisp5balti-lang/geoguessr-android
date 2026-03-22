package com.geoguessr.android.domain.model

data class PlayerScore(
    val userId: String = "",
    val nickname: String = "",
    val roundScores: List<Int> = emptyList(),
    val totalScore: Int = 0
)
