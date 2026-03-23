package com.geoguessr.android.presentation.screen.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geoguessr.android.data.local.LocationDatabase
import com.geoguessr.android.domain.model.GameSession
import com.geoguessr.android.domain.model.Location
import com.geoguessr.android.domain.model.RoundResult
import com.geoguessr.android.domain.repository.AuthRepository
import com.geoguessr.android.domain.repository.GameRepository
import com.geoguessr.android.domain.usecase.ScoreCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ClassicGamePhase { LOADING, PLAYING, ROUND_RESULT, GAME_OVER }

data class ClassicGameUiState(
    val phase: ClassicGamePhase = ClassicGamePhase.LOADING,
    val currentRound: Int = 1,
    val totalRounds: Int = 5,
    val currentLocation: Location? = null,
    val guessedLocation: Location? = null,
    val timeRemaining: Int = 90,
    val roundScore: Int = 0,
    val roundDistance: Double = 0.0,
    val totalScore: Int = 0,
    val roundResults: List<RoundResult> = emptyList(),
    val locations: List<Location> = emptyList(),
    val isMapExpanded: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ClassicGameViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClassicGameUiState())
    val uiState: StateFlow<ClassicGameUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    fun startGame(gameType: String = "CLASSIC") {
        val locations = generateLocations(5)
        _uiState.update {
            it.copy(
                phase = ClassicGamePhase.PLAYING,
                locations = locations,
                currentRound = 1,
                currentLocation = locations[0],
                totalScore = 0,
                roundResults = emptyList(),
                guessedLocation = null
            )
        }
        startTimer()
    }

    private fun generateLocations(count: Int): List<Location> {
        return LocationDatabase.locations.shuffled().take(count)
    }

    fun onMapTap(latitude: Double, longitude: Double) {
        if (_uiState.value.phase == ClassicGamePhase.PLAYING) {
            _uiState.update { it.copy(guessedLocation = Location(latitude, longitude)) }
        }
    }

    fun toggleMapExpanded() {
        _uiState.update { it.copy(isMapExpanded = !it.isMapExpanded) }
    }

    fun submitGuess() {
        val state = _uiState.value
        if (state.phase != ClassicGamePhase.PLAYING) return
        timerJob?.cancel()

        val guessed = state.guessedLocation ?: Location(0.0, 0.0)
        val actual = state.currentLocation ?: return
        val distance = ScoreCalculator.haversineDistance(actual, guessed)
        val score = ScoreCalculator.calculateScore(actual, guessed)

        val roundResult = RoundResult(
            roundNumber = state.currentRound,
            actualLocation = actual,
            guessedLocation = guessed,
            score = score,
            distanceKm = distance
        )

        _uiState.update {
            it.copy(
                phase = ClassicGamePhase.ROUND_RESULT,
                roundScore = score,
                roundDistance = distance,
                totalScore = it.totalScore + score,
                roundResults = it.roundResults + roundResult,
                guessedLocation = guessed
            )
        }
    }

    fun nextRound() {
        val state = _uiState.value
        val nextRound = state.currentRound + 1

        if (nextRound > state.totalRounds) {
            GameResultCache.store(state.roundResults, state.totalScore)
            saveSession(state)
            _uiState.update { it.copy(phase = ClassicGamePhase.GAME_OVER) }
        } else {
            _uiState.update {
                it.copy(
                    phase = ClassicGamePhase.PLAYING,
                    currentRound = nextRound,
                    currentLocation = it.locations.getOrNull(nextRound - 1),
                    guessedLocation = null,
                    timeRemaining = 90
                )
            }
            startTimer()
        }
    }

    private fun saveSession(state: ClassicGameUiState) {
        val userId = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            val session = GameSession(
                playerId = userId,
                gameType = "CLASSIC",
                rounds = state.roundResults,
                totalScore = state.totalScore,
                timestamp = System.currentTimeMillis()
            )
            gameRepository.saveGameSession(session)
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        _uiState.update { it.copy(timeRemaining = 90) }
        timerJob = viewModelScope.launch {
            for (i in 90 downTo 1) {
                _uiState.update { it.copy(timeRemaining = i) }
                delay(1000)
            }
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
