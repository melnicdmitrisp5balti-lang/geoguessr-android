package com.geoguessr.android.domain.model

data class RoundResult(
    val roundNumber: Int = 0,
    val actualLocation: Location = Location(),
    val guessedLocation: Location = Location(),
    val score: Int = 0,
    val distanceKm: Double = 0.0
)

data class GameSession(
    val id: String = "",
    val playerId: String = "",
    val gameType: String = "CLASSIC",
    val rounds: List<RoundResult> = emptyList(),
    val totalScore: Int = 0,
    val timestamp: Long = 0L
)
