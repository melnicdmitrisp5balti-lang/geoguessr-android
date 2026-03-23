package com.geoguessr.android.data.repository

import com.geoguessr.android.domain.model.*
import com.geoguessr.android.domain.model.GameSession
import com.geoguessr.android.domain.model.PlayerProfile
import com.geoguessr.android.domain.repository.GameRepository
import com.geoguessr.android.domain.usecase.ScoreCalculator
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class GameRepositoryImpl @Inject constructor(
    private val database: FirebaseDatabase
) : GameRepository {

    private val roomsRef = database.getReference("rooms")

    override suspend fun createRoom(ownerId: String, ownerNickname: String): Result<GameRoom> = runCatching {
        val code = generateRoomCode()
        val roomRef = roomsRef.push()
        val roomId = roomRef.key ?: throw Exception("Failed to create room")
        val playerScore = PlayerScore(userId = ownerId, nickname = ownerNickname)
        val room = GameRoom(
            roomId = roomId,
            code = code,
            ownerId = ownerId,
            players = mapOf(ownerId to playerScore),
            status = RoomStatus.WAITING
        )
        roomRef.setValue(roomToMap(room)).await()
        room
    }

    override suspend fun joinRoom(code: String, userId: String, nickname: String): Result<GameRoom> = runCatching {
        val snapshot = roomsRef.orderByChild("code").equalTo(code).get().await()
        if (!snapshot.exists()) throw Exception("Room not found")
        val roomSnapshot = snapshot.children.first()
        val roomId = roomSnapshot.key ?: throw Exception("Invalid room")
        val status = roomSnapshot.child("status").getValue(String::class.java) ?: ""
        if (status != RoomStatus.WAITING.name) throw Exception("Game already started")
        val playerScore = PlayerScore(userId = userId, nickname = nickname)
        roomsRef.child("$roomId/players/$userId").setValue(playerScoreToMap(playerScore)).await()
        val updatedSnapshot = roomsRef.child(roomId).get().await()
        snapshotToRoom(updatedSnapshot) ?: throw Exception("Failed to get room data")
    }

    override suspend fun leaveRoom(roomId: String, userId: String) {
        roomsRef.child("$roomId/players/$userId").removeValue().await()
    }

    override fun observeRoom(roomId: String): Flow<GameRoom?> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshotToRoom(snapshot))
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        roomsRef.child(roomId).addValueEventListener(listener)
        awaitClose { roomsRef.child(roomId).removeEventListener(listener) }
    }

    override suspend fun startGame(roomId: String): Result<Unit> = runCatching {
        val rounds = (1..5).map { roundNumber ->
            val location = getRandomLocation().getOrThrow()
            mapOf(
                "roundNumber" to roundNumber,
                "location" to mapOf(
                    "latitude" to location.latitude,
                    "longitude" to location.longitude,
                    "panoramaId" to location.panoramaId
                ),
                "timeLimit" to 60
            )
        }
        val updates = mapOf(
            "status" to RoomStatus.IN_PROGRESS.name,
            "currentRound" to 1,
            "rounds" to rounds
        )
        roomsRef.child(roomId).updateChildren(updates).await()
    }

    override suspend fun submitGuess(
        roomId: String,
        userId: String,
        roundNumber: Int,
        guessedLocation: Location
    ): Result<Int> = runCatching {
        val roundSnapshot = roomsRef.child("$roomId/rounds/${roundNumber - 1}/location").get().await()
        val actualLat = roundSnapshot.child("latitude").getValue(Double::class.java) ?: 0.0
        val actualLon = roundSnapshot.child("longitude").getValue(Double::class.java) ?: 0.0
        val actualLocation = Location(actualLat, actualLon)
        val score = ScoreCalculator.calculateScore(actualLocation, guessedLocation)

        val guessData = mapOf(
            "latitude" to guessedLocation.latitude,
            "longitude" to guessedLocation.longitude
        )
        roomsRef.child("$roomId/rounds/${roundNumber - 1}/selectedLocations/$userId").setValue(guessData).await()

        val currentScoresSnapshot = roomsRef.child("$roomId/players/$userId/roundScores").get().await()
        val currentScores = currentScoresSnapshot.children.mapNotNull {
            it.getValue(Int::class.java)
        }.toMutableList()
        while (currentScores.size < roundNumber - 1) currentScores.add(0)
        currentScores.add(score)
        val totalScore = currentScores.sum()
        roomsRef.child("$roomId/players/$userId/roundScores").setValue(currentScores).await()
        roomsRef.child("$roomId/players/$userId/totalScore").setValue(totalScore).await()
        score
    }

    override suspend fun getRandomLocation(): Result<Location> = runCatching {
        val locations = listOf(
            Location(48.8566, 2.3522, ""),    // Paris
            Location(51.5074, -0.1278, ""),   // London
            Location(40.7128, -74.0060, ""),  // New York
            Location(35.6762, 139.6503, ""),  // Tokyo
            Location(-33.8688, 151.2093, ""), // Sydney
            Location(55.7558, 37.6173, ""),   // Moscow
            Location(19.4326, -99.1332, ""),  // Mexico City
            Location(-23.5505, -46.6333, ""), // São Paulo
            Location(1.3521, 103.8198, ""),   // Singapore
            Location(41.9028, 12.4964, "")    // Rome
        )
        locations[Random.nextInt(locations.size)]
    }

    override suspend fun finishGame(roomId: String): Result<Unit> = runCatching {
        roomsRef.child("$roomId/status").setValue(RoomStatus.FINISHED.name).await()
    }

    private fun generateRoomCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { chars[Random.nextInt(chars.length)] }.joinToString("")
    }

    private fun roomToMap(room: GameRoom): Map<String, Any> = mapOf(
        "roomId" to room.roomId,
        "code" to room.code,
        "ownerId" to room.ownerId,
        "status" to room.status.name,
        "totalRounds" to room.totalRounds,
        "currentRound" to room.currentRound,
        "players" to room.players.mapValues { playerScoreToMap(it.value) }
    )

    private fun playerScoreToMap(ps: PlayerScore): Map<String, Any> = mapOf(
        "userId" to ps.userId,
        "nickname" to ps.nickname,
        "roundScores" to ps.roundScores,
        "totalScore" to ps.totalScore
    )

    private fun snapshotToRoom(snapshot: DataSnapshot): GameRoom? {
        if (!snapshot.exists()) return null
        val roomId = snapshot.child("roomId").getValue(String::class.java) ?: return null
        val code = snapshot.child("code").getValue(String::class.java) ?: ""
        val ownerId = snapshot.child("ownerId").getValue(String::class.java) ?: ""
        val statusStr = snapshot.child("status").getValue(String::class.java) ?: "WAITING"
        val status = try { RoomStatus.valueOf(statusStr) } catch (e: IllegalArgumentException) { RoomStatus.WAITING }
        val totalRounds = snapshot.child("totalRounds").getValue(Int::class.java) ?: 5
        val currentRound = snapshot.child("currentRound").getValue(Int::class.java) ?: 0

        val players = snapshot.child("players").children.associate { playerSnap ->
            val userId = playerSnap.child("userId").getValue(String::class.java) ?: ""
            val nickname = playerSnap.child("nickname").getValue(String::class.java) ?: ""
            val scores = playerSnap.child("roundScores").children.mapNotNull {
                it.getValue(Int::class.java)
            }
            val totalScore = playerSnap.child("totalScore").getValue(Int::class.java) ?: 0
            userId to PlayerScore(userId, nickname, scores, totalScore)
        }

        return GameRoom(roomId, code, ownerId, players, status, emptyList(), totalRounds, currentRound)
    }

    override suspend fun saveGameSession(session: GameSession): Result<Unit> = runCatching {
        val userId = session.playerId
        if (userId.isEmpty()) return Result.failure(IllegalArgumentException("Player ID must not be empty"))
        val sessionRef = database.getReference("sessions/$userId").push()
        val data = mapOf(
            "gameType" to session.gameType,
            "totalScore" to session.totalScore,
            "rounds" to session.rounds.size,
            "timestamp" to session.timestamp
        )
        sessionRef.setValue(data).await()
        val profileRef = database.getReference("profiles/$userId")
        val snapshot = profileRef.get().await()
        val currentGames = snapshot.child("gamesPlayed").getValue(Int::class.java) ?: 0
        val currentTotal = snapshot.child("totalScore").getValue(Int::class.java) ?: 0
        val currentBest = snapshot.child("bestScore").getValue(Int::class.java) ?: 0
        val nickname = snapshot.child("nickname").getValue(String::class.java) ?: ""
        profileRef.updateChildren(mapOf(
            "gamesPlayed" to currentGames + 1,
            "totalScore" to currentTotal + session.totalScore,
            "bestScore" to maxOf(currentBest, session.totalScore),
            "nickname" to nickname
        )).await()
    }

    override suspend fun getPlayerProfile(userId: String): PlayerProfile {
        if (userId.isEmpty()) return PlayerProfile()
        return try {
            val snapshot = database.getReference("profiles/$userId").get().await()
            PlayerProfile(
                userId = userId,
                nickname = snapshot.child("nickname").getValue(String::class.java) ?: "",
                totalScore = snapshot.child("totalScore").getValue(Int::class.java) ?: 0,
                gamesPlayed = snapshot.child("gamesPlayed").getValue(Int::class.java) ?: 0,
                bestScore = snapshot.child("bestScore").getValue(Int::class.java) ?: 0
            )
        } catch (e: Exception) {
            PlayerProfile(userId = userId)
        }
    }

    override suspend fun getLeaderboard(): List<Pair<String, Int>> {
        return try {
            val snapshot = database.getReference("profiles").get().await()
            snapshot.children
                .mapNotNull { child ->
                    val nickname = child.child("nickname").getValue(String::class.java) ?: return@mapNotNull null
                    val score = child.child("totalScore").getValue(Int::class.java) ?: 0
                    if (nickname.isBlank()) null else nickname to score
                }
                .sortedByDescending { it.second }
                .take(10)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
