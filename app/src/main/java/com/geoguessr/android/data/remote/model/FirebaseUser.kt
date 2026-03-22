package com.geoguessr.android.data.remote.model

data class FirebaseUser(
    val email: String = "",
    val nickname: String = "",
    val avatar: String = "",
    val totalScore: Int = 0,
    val gamesPlayed: Int = 0
)
