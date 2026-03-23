package com.geoguessr.android.presentation.screen.multiplayer

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

data class MultiplayerLobbyUiState(
    val isLoading: Boolean = false,
    val createdRoomId: String? = null,
    val createdRoomCode: String? = null,
    val joinCode: String = "",
    val playersInRoom: Int = 1,
    val startedRoomId: String? = null,
    val error: String? = null
)

@HiltViewModel
class MultiplayerLobbyViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MultiplayerLobbyUiState())
    val uiState: StateFlow<MultiplayerLobbyUiState> = _uiState.asStateFlow()

    fun createRoom() {
        val user = authRepository.currentUser ?: return
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            gameRepository.createRoom(user.uid, user.nickname.ifBlank { user.email })
                .onSuccess { room ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            createdRoomId = room.roomId,
                            createdRoomCode = room.code
                        )
                    }
                    observeRoom(room.roomId)
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to create room") }
                }
        }
    }

    private fun observeRoom(roomId: String) {
        viewModelScope.launch {
            gameRepository.observeRoom(roomId).collect { room ->
                room?.let {
                    _uiState.update { state ->
                        state.copy(playersInRoom = room.players.size)
                    }
                }
            }
        }
    }

    fun joinRoom() {
        val user = authRepository.currentUser ?: return
        val code = _uiState.value.joinCode.trim().uppercase()
        if (code.isEmpty()) return
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            gameRepository.joinRoom(code, user.uid, user.nickname.ifBlank { user.email })
                .onSuccess { room ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            createdRoomId = room.roomId,
                            createdRoomCode = room.code
                        )
                    }
                    observeRoom(room.roomId)
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to join room") }
                }
        }
    }

    fun startMultiplayerGame() {
        val roomId = _uiState.value.createdRoomId ?: return
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            gameRepository.startGame(roomId)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, startedRoomId = roomId) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to start game") }
                }
        }
    }

    fun onJoinCodeChange(code: String) {
        _uiState.update { it.copy(joinCode = code.uppercase()) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
