package com.geoguessr.android.domain.repository

import com.geoguessr.android.domain.model.GameRoom
import com.geoguessr.android.domain.model.GameSession
import com.geoguessr.android.domain.model.Location
import com.geoguessr.android.domain.model.PlayerProfile
import kotlinx.coroutines.flow.Flow

interface GameRepository {
    suspend fun createRoom(ownerId: String, ownerNickname: String): Result<GameRoom>
    suspend fun joinRoom(code: String, userId: String, nickname: String): Result<GameRoom>
    suspend fun leaveRoom(roomId: String, userId: String)
    fun observeRoom(roomId: String): Flow<GameRoom?>
    suspend fun startGame(roomId: String): Result<Unit>
    suspend fun submitGuess(
        roomId: String,
        userId: String,
        roundNumber: Int,
        guessedLocation: Location
    ): Result<Int>
    suspend fun getRandomLocation(): Result<Location>
    suspend fun finishGame(roomId: String): Result<Unit>
    suspend fun saveGameSession(session: GameSession): Result<Unit>
    suspend fun getPlayerProfile(userId: String): PlayerProfile
    suspend fun getLeaderboard(): List<Pair<String, Int>>
}
