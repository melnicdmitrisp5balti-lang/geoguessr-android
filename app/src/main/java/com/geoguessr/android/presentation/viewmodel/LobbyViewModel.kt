package com.geoguessr.android.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geoguessr.android.domain.model.GameRoom
import com.geoguessr.android.domain.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LobbyViewModel @Inject constructor(
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _lobbyState = MutableLiveData<LobbyState>()
    val lobbyState: LiveData<LobbyState> = _lobbyState

    private val _currentRoom = MutableLiveData<GameRoom?>()
    val currentRoom: LiveData<GameRoom?> = _currentRoom

    fun createRoom(userId: String, nickname: String) {
        _lobbyState.value = LobbyState.Loading
        viewModelScope.launch {
            gameRepository.createRoom(userId, nickname)
                .onSuccess { room ->
                    _currentRoom.value = room
                    _lobbyState.value = LobbyState.RoomCreated(room)
                    observeRoom(room.roomId)
                }
                .onFailure { e -> _lobbyState.value = LobbyState.Error(e.message ?: "Failed to create room") }
        }
    }

    fun joinRoom(code: String, userId: String, nickname: String) {
        _lobbyState.value = LobbyState.Loading
        viewModelScope.launch {
            gameRepository.joinRoom(code, userId, nickname)
                .onSuccess { room ->
                    _currentRoom.value = room
                    _lobbyState.value = LobbyState.RoomJoined(room)
                    observeRoom(room.roomId)
                }
                .onFailure { e -> _lobbyState.value = LobbyState.Error(e.message ?: "Failed to join room") }
        }
    }

    fun startGame(roomId: String) {
        viewModelScope.launch {
            gameRepository.startGame(roomId)
                .onFailure { e -> _lobbyState.value = LobbyState.Error(e.message ?: "Failed to start game") }
        }
    }

    private fun observeRoom(roomId: String) {
        gameRepository.observeRoom(roomId)
            .onEach { room -> _currentRoom.value = room }
            .launchIn(viewModelScope)
    }

    sealed class LobbyState {
        object Loading : LobbyState()
        data class RoomCreated(val room: GameRoom) : LobbyState()
        data class RoomJoined(val room: GameRoom) : LobbyState()
        data class Error(val message: String) : LobbyState()
    }
}
