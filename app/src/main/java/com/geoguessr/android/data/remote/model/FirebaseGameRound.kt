package com.geoguessr.android.data.remote.model

data class FirebaseGameRound(
    val location: FirebaseLocation = FirebaseLocation(),
    val playerSelections: Map<String, Map<String, Double>> = emptyMap(),
    val scores: Map<String, Int> = emptyMap()
)
