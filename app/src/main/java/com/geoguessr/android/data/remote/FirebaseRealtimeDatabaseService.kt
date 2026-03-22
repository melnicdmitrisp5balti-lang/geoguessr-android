package com.geoguessr.android.data.remote

import com.geoguessr.android.data.remote.model.FirebaseGameRoom
import com.geoguessr.android.data.remote.model.FirebaseGameRound
import com.geoguessr.android.data.remote.model.FirebaseLocation
import com.geoguessr.android.data.remote.model.GameRoomStatus
import com.geoguessr.android.domain.model.Location
import com.geoguessr.android.domain.usecase.ScoreCalculator
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseRealtimeDatabaseService @Inject constructor(
    private val database: FirebaseDatabase
) {

    private val gameRoomsRef = database.getReference("gameRooms")
    private val gameRoundsRef = database.getReference("gameRounds")
    private val leaderboardRef = database.getReference("leaderboard")

    fun createGameRoom(ownerId: String, players: List<String>): Task<String> {
        val roomRef = gameRoomsRef.push()
        val roomId = roomRef.key
            ?: return Tasks.forException(Exception("Failed to generate room ID"))
        val allPlayers = if (players.contains(ownerId)) players else listOf(ownerId) + players
        val room = FirebaseGameRoom(
            roomId = roomId,
            code = generateRoomCode(),
            ownerId = ownerId,
            players = allPlayers,
            status = GameRoomStatus.WAITING,
            currentRound = 0,
            totalRounds = 5,
            createdAt = System.currentTimeMillis()
        )
        return roomRef.setValue(room).continueWith { roomId }
    }

    fun joinGameRoom(roomId: String, userId: String): Task<Void> {
        return gameRoomsRef.child(roomId).get()
            .continueWithTask { task ->
                val snapshot = task.result
                if (!snapshot.exists()) throw Exception("Room not found: $roomId")
                val status = snapshot.child("status").getValue(String::class.java)
                if (status != GameRoomStatus.WAITING) throw Exception("Room is not accepting new players")
                val currentPlayers = snapshot.child("players").children
                    .mapNotNull { it.getValue(String::class.java) }
                    .toMutableList()
                if (!currentPlayers.contains(userId)) {
                    currentPlayers.add(userId)
                }
                gameRoomsRef.child("$roomId/players").setValue(currentPlayers)
            }
    }

    fun getGameRoom(roomId: String): Task<FirebaseGameRoom> {
        return gameRoomsRef.child(roomId).get()
            .continueWith { task ->
                task.result.getValue(FirebaseGameRoom::class.java)
                    ?: throw Exception("Game room not found: $roomId")
            }
    }

    fun updateGameRoomStatus(roomId: String, status: String): Task<Void> {
        return gameRoomsRef.child("$roomId/status").setValue(status)
    }

    fun addGameRound(roomId: String, roundNumber: Int, location: FirebaseLocation): Task<Void> {
        val round = FirebaseGameRound(location = location)
        return gameRoundsRef.child("$roomId/$roundNumber").setValue(round)
    }

    fun submitPlayerSelection(
        roomId: String,
        roundNumber: Int,
        userId: String,
        selection: FirebaseLocation
    ): Task<Void> {
        val selectionData = mapOf(LAT_KEY to selection.latitude, LNG_KEY to selection.longitude)
        return gameRoundsRef
            .child("$roomId/$roundNumber/playerSelections/$userId")
            .setValue(selectionData)
    }

    fun calculateAndSubmitScore(roomId: String, roundNumber: Int, userId: String): Task<Void> {
        return gameRoundsRef.child("$roomId/$roundNumber").get()
            .continueWithTask { task ->
                val snapshot = task.result
                val lat = snapshot.child("location/latitude").getValue(Double::class.java)
                    ?: throw Exception("No latitude data for round $roundNumber in room $roomId")
                val lng = snapshot.child("location/longitude").getValue(Double::class.java)
                    ?: throw Exception("No longitude data for round $roundNumber in room $roomId")
                val selLat = snapshot.child("playerSelections/$userId/$LAT_KEY")
                    .getValue(Double::class.java)
                    ?: throw Exception("No lat selection for user $userId in round $roundNumber")
                val selLng = snapshot.child("playerSelections/$userId/$LNG_KEY")
                    .getValue(Double::class.java)
                    ?: throw Exception("No lng selection for user $userId in round $roundNumber")
                val score = ScoreCalculator.calculateScore(
                    Location(lat, lng),
                    Location(selLat, selLng)
                )
                val updates = mapOf<String, Any>(
                    "gameRounds/$roomId/$roundNumber/scores/$userId" to score,
                    "leaderboard/$roomId/$userId" to ServerValue.increment(score.toLong())
                )
                database.reference.updateChildren(updates)
            }
    }

    fun getLeaderboard(roomId: String): Task<Map<String, Long>> {
        return leaderboardRef.child(roomId).get()
            .continueWith { task ->
                task.result.children.associate { child ->
                    val userId = child.key ?: ""
                    val score = child.getValue(Long::class.java) ?: 0L
                    userId to score
                }
            }
    }

    private fun generateRoomCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { chars.random() }.joinToString("")
    }

    companion object {
        private const val LAT_KEY = "lat"
        private const val LNG_KEY = "lng"
    }
}
