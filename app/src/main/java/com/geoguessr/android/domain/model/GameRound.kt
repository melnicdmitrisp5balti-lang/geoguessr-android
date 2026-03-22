package com.geoguessr.android.domain.model

data class GameRound(
    val roundNumber: Int = 0,
    val location: Location = Location(),
    val imageUrl: String = "",
    val selectedLocations: Map<String, Location> = emptyMap(),
    val timeLimit: Int = 60
)
