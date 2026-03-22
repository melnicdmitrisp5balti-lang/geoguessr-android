package com.geoguessr.android.domain.repository

import com.geoguessr.android.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: User?
    fun isLoggedIn(): Boolean
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(email: String, password: String, nickname: String): Result<User>
    suspend fun logout()
    fun getUserFlow(): Flow<User?>
}
