package com.geoguessr.android.presentation.screen.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geoguessr.android.domain.model.GameRoom
import com.geoguessr.android.domain.model.Location
import com.geoguessr.android.domain.model.RoomStatus
import com.geoguessr.android.domain.repository.AuthRepository
import com.geoguessr.android.domain.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GameUiState(
    val roomId: String = "",
    val currentRoom: GameRoom? = null,
    val currentUserId: String = "",
    val locationGuess: String = "",
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val timeRemaining: Int = 60,
    val roundScore: Int? = null,
    val totalScore: Int = 0,
    val isRoundFinished: Boolean = false,
    val isGameFinished: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class GameViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    fun initGame(roomId: String) {
        val userId = authRepository.currentUser?.uid ?: return
        _uiState.update { it.copy(roomId = roomId, currentUserId = userId, isLoading = true) }
        observeRoom(roomId)
    }

    private fun observeRoom(roomId: String) {
        gameRepository.observeRoom(roomId)
            .onEach { room ->
                _uiState.update { it.copy(currentRoom = room, isLoading = false) }
                handleRoomUpdate(room)
            }
            .launchIn(viewModelScope)
    }

    private fun handleRoomUpdate(room: GameRoom?) {
        room ?: return
        when (room.status) {
            RoomStatus.IN_PROGRESS -> {
                if (!_uiState.value.isRoundFinished) {
                    startTimer(60)
                }
            }
            RoomStatus.FINISHED -> {
                timerJob?.cancel()
                _uiState.update { it.copy(isGameFinished = true) }
            }
            else -> {}
        }
    }

    fun onLocationGuessChange(guess: String) {
        _uiState.update { it.copy(locationGuess = guess) }
    }

    fun submitGuess() {
        val state = _uiState.value
        val roundNumber = state.currentRoom?.currentRound ?: return

        timerJob?.cancel()
        _uiState.update { it.copy(isSubmitting = true, error = null) }

        val parts = state.locationGuess.trim().split(",").map { it.trim() }
        val lat = parts.getOrNull(0)?.toDoubleOrNull()
        val lng = parts.getOrNull(1)?.toDoubleOrNull()

        if (state.locationGuess.isNotBlank() && (lat == null || lng == null)) {
            _uiState.update {
                it.copy(
                    isSubmitting = false,
                    error = "Invalid format. Use: latitude, longitude (e.g. 48.8566, 2.3522)"
                )
            }
            return
        }

        val guessedLocation = if (lat != null && lng != null) Location(lat, lng) else Location()

        viewModelScope.launch {
            gameRepository.submitGuess(state.roomId, state.currentUserId, roundNumber, guessedLocation)
                .onSuccess { score ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            roundScore = score,
                            totalScore = it.totalScore + score,
                            isRoundFinished = true,
                            locationGuess = ""
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isSubmitting = false, error = e.message ?: "Failed to submit guess") }
                }
        }
    }

    fun nextRound() {
        val state = _uiState.value
        val room = state.currentRoom ?: return
        val nextRound = room.currentRound + 1

        _uiState.update { it.copy(isRoundFinished = false, roundScore = null) }

        if (nextRound > room.totalRounds) {
            viewModelScope.launch {
                gameRepository.finishGame(state.roomId)
            }
        } else {
            startTimer(60)
        }
    }

    fun endGame() {
        viewModelScope.launch {
            timerJob?.cancel()
            gameRepository.finishGame(_uiState.value.roomId)
        }
    }

    private fun startTimer(seconds: Int) {
        timerJob?.cancel()
        _uiState.update { it.copy(timeRemaining = seconds) }
        timerJob = viewModelScope.launch {
            for (i in seconds downTo 1) {
                _uiState.update { it.copy(timeRemaining = i) }
                delay(1000)
            }
            _uiState.update { it.copy(timeRemaining = 0) }
            submitGuess()
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
