package com.geoguessr.android.presentation.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geoguessr.android.domain.repository.AuthRepository
import com.geoguessr.android.domain.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val playerName: String = "Explorer",
    val totalScore: Int = 0,
    val gamesPlayed: Int = 0,
    val bestScore: Int = 0,
    val topPlayers: List<Pair<String, Int>> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val user = authRepository.currentUser
        val name = user?.nickname?.ifBlank { user.email.substringBefore("@") } ?: "Explorer"
        _uiState.update { it.copy(playerName = name) }

        viewModelScope.launch {
            try {
                val uid = user?.uid ?: return@launch
                val profile = gameRepository.getPlayerProfile(uid)
                _uiState.update {
                    it.copy(
                        totalScore = profile.totalScore,
                        gamesPlayed = profile.gamesPlayed,
                        bestScore = profile.bestScore
                    )
                }
            } catch (_: Exception) {}

            try {
                val leaders = gameRepository.getLeaderboard()
                _uiState.update { it.copy(topPlayers = leaders) }
            } catch (_: Exception) {}
        }
    }

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }
}
