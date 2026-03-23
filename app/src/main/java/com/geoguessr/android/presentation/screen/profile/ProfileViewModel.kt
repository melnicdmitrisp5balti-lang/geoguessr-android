package com.geoguessr.android.presentation.screen.profile

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

data class ProfileUiState(
    val nickname: String = "",
    val email: String = "",
    val totalScore: Int = 0,
    val gamesPlayed: Int = 0,
    val bestScore: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        val user = authRepository.currentUser ?: return
        _uiState.update {
            it.copy(
                nickname = user.nickname.ifBlank { user.email.substringBefore("@") },
                email = user.email
            )
        }
        viewModelScope.launch {
            val profile = gameRepository.getPlayerProfile(user.uid)
            _uiState.update {
                it.copy(
                    totalScore = profile.totalScore,
                    gamesPlayed = profile.gamesPlayed,
                    bestScore = profile.bestScore,
                    isLoading = false
                )
            }
        }
    }
}
