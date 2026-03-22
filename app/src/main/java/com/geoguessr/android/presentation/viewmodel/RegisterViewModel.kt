package com.geoguessr.android.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geoguessr.android.domain.model.User
import com.geoguessr.android.domain.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _registerState = MutableLiveData<RegisterState>()
    val registerState: LiveData<RegisterState> = _registerState

    fun register(email: String, password: String, nickname: String) {
        _registerState.value = RegisterState.Loading
        viewModelScope.launch {
            registerUseCase(email, password, nickname)
                .onSuccess { user -> _registerState.value = RegisterState.Success(user) }
                .onFailure { e -> _registerState.value = RegisterState.Error(e.message ?: "Registration failed") }
        }
    }

    sealed class RegisterState {
        object Loading : RegisterState()
        data class Success(val user: User) : RegisterState()
        data class Error(val message: String) : RegisterState()
    }
}
