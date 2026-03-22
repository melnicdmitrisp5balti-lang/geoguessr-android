package com.geoguessr.android.presentation.screen.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geoguessr.android.domain.model.User
import com.geoguessr.android.domain.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nickname: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val registeredUser: User? = null
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, error = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, error = null) }
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = confirmPassword, error = null) }
    }

    fun onNicknameChange(nickname: String) {
        _uiState.update { it.copy(nickname = nickname, error = null) }
    }

    fun register() {
        val state = _uiState.value
        when {
            state.email.isBlank() || state.password.isBlank() || state.nickname.isBlank() -> {
                _uiState.update { it.copy(error = "All fields are required") }
                return
            }
            state.password.length < 6 -> {
                _uiState.update { it.copy(error = "Password must be at least 6 characters") }
                return
            }
            state.password != state.confirmPassword -> {
                _uiState.update { it.copy(error = "Passwords do not match") }
                return
            }
        }
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            registerUseCase(state.email.trim(), state.password, state.nickname.trim())
                .onSuccess { user ->
                    _uiState.update { it.copy(isLoading = false, registeredUser = user) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Registration failed") }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
