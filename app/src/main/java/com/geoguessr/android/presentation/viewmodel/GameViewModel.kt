package com.geoguessr.android.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geoguessr.android.domain.model.GameRoom
import com.geoguessr.android.domain.model.Location
import com.geoguessr.android.domain.model.RoomStatus
import com.geoguessr.android.domain.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _gameState = MutableLiveData<GameState>(GameState.Idle)
    val gameState: LiveData<GameState> = _gameState

    private val _currentRoom = MutableLiveData<GameRoom?>()
    val currentRoom: LiveData<GameRoom?> = _currentRoom

    private val _timeRemaining = MutableLiveData(60)
    val timeRemaining: LiveData<Int> = _timeRemaining

    private val _lastScore = MutableLiveData<Int>()
    val lastScore: LiveData<Int> = _lastScore

    private var timerJob: Job? = null
    private var currentRoomId: String = ""
    private var currentUserId: String = ""

    fun startObservingRoom(roomId: String, userId: String) {
        currentRoomId = roomId
        currentUserId = userId
        gameRepository.observeRoom(roomId)
            .onEach { room ->
                _currentRoom.value = room
                handleRoomUpdate(room)
            }
            .launchIn(viewModelScope)
    }

    private fun handleRoomUpdate(room: GameRoom?) {
        room ?: return
        when (room.status) {
            RoomStatus.IN_PROGRESS -> {
                val currentRound = room.currentRound
                if (currentRound > 0) {
                    _gameState.value = GameState.RoundInProgress(currentRound)
                    startTimer(60)
                }
            }
            RoomStatus.FINISHED -> {
                timerJob?.cancel()
                _gameState.value = GameState.GameFinished(room)
            }
            else -> {}
        }
    }

    fun submitGuess(guessedLocation: Location, roundNumber: Int) {
        timerJob?.cancel()
        _gameState.value = GameState.Submitting
        viewModelScope.launch {
            gameRepository.submitGuess(currentRoomId, currentUserId, roundNumber, guessedLocation)
                .onSuccess { score ->
                    _lastScore.value = score
                    _gameState.value = GameState.RoundFinished(score, roundNumber)
                }
                .onFailure { e ->
                    _gameState.value = GameState.Error(e.message ?: "Failed to submit guess")
                }
        }
    }

    fun nextRound(roomId: String, nextRoundNumber: Int, totalRounds: Int) {
        if (nextRoundNumber > totalRounds) {
            viewModelScope.launch {
                gameRepository.finishGame(roomId)
            }
        } else {
            _gameState.value = GameState.RoundInProgress(nextRoundNumber)
            startTimer(60)
        }
    }

    private fun startTimer(seconds: Int) {
        timerJob?.cancel()
        _timeRemaining.value = seconds
        timerJob = viewModelScope.launch {
            for (i in seconds downTo 0) {
                _timeRemaining.value = i
                if (i == 0) break
                delay(1000)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    sealed class GameState {
        object Idle : GameState()
        object Submitting : GameState()
        data class RoundInProgress(val roundNumber: Int) : GameState()
        data class RoundFinished(val score: Int, val roundNumber: Int) : GameState()
        data class GameFinished(val room: GameRoom) : GameState()
        data class Error(val message: String) : GameState()
    }
}
