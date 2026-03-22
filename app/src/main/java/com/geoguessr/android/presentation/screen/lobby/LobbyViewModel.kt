package com.geoguessr.android.presentation.screen.lobby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geoguessr.android.domain.model.GameRoom
import com.geoguessr.android.domain.model.User
import com.geoguessr.android.domain.repository.AuthRepository
import com.geoguessr.android.domain.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class GameMode {
    CLASSIC, TIME_ATTACK, MULTIPLAYER
}

data class LobbyUiState(
    val currentUser: User? = null,
    val selectedMode: GameMode = GameMode.CLASSIC,
    val isLoading: Boolean = false,
    val error: String? = null,
    val createdRoom: GameRoom? = null
)

@HiltViewModel
class LobbyViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LobbyUiState())
    val uiState: StateFlow<LobbyUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        _uiState.update { it.copy(currentUser = authRepository.currentUser) }
    }

    fun onGameModeSelected(mode: GameMode) {
        _uiState.update { it.copy(selectedMode = mode) }
    }

    fun startGame() {
        val user = _uiState.value.currentUser ?: return
        val mode = _uiState.value.selectedMode
        if (mode == GameMode.MULTIPLAYER) return

        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            gameRepository.createRoom(user.uid, user.nickname.ifBlank { user.email })
                .onSuccess { room ->
                    _uiState.update { it.copy(isLoading = false, createdRoom = room) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to start game") }
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearCreatedRoom() {
        _uiState.update { it.copy(createdRoom = null) }
    }
}
