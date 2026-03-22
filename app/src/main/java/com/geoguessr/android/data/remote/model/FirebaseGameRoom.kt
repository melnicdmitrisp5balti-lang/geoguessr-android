package com.geoguessr.android.data.remote.model

object GameRoomStatus {
    const val WAITING = "WAITING"
    const val IN_PROGRESS = "IN_PROGRESS"
    const val FINISHED = "FINISHED"
}

data class FirebaseGameRoom(
    val roomId: String = "",
    val code: String = "",
    val ownerId: String = "",
    val players: List<String> = emptyList(),
    val status: String = GameRoomStatus.WAITING,
    val currentRound: Int = 0,
    val totalRounds: Int = 5,
    val createdAt: Long = 0L
)
