package com.geoguessr.android.domain.usecase

import com.geoguessr.android.domain.model.User
import com.geoguessr.android.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        nickname: String
    ): Result<User> {
        if (email.isBlank() || password.isBlank() || nickname.isBlank()) {
            return Result.failure(IllegalArgumentException("All fields are required"))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters"))
        }
        return authRepository.register(email.trim(), password, nickname.trim())
    }
}
