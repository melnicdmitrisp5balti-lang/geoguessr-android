package com.geoguessr.android.domain.model

enum class RoomStatus {
    WAITING, IN_PROGRESS, FINISHED
}

data class GameRoom(
    val roomId: String = "",
    val code: String = "",
    val ownerId: String = "",
    val players: Map<String, PlayerScore> = emptyMap(),
    val status: RoomStatus = RoomStatus.WAITING,
    val rounds: List<GameRound> = emptyList(),
    val totalRounds: Int = 5,
    val currentRound: Int = 0
)
