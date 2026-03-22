package com.geoguessr.android.data.repository

import com.geoguessr.android.domain.model.User
import com.geoguessr.android.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val database: FirebaseDatabase
) : AuthRepository {

    override val currentUser: User?
        get() = firebaseAuth.currentUser?.let { firebaseUser ->
            User(
                uid = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                nickname = firebaseUser.displayName ?: ""
            )
        }

    override fun isLoggedIn(): Boolean = firebaseAuth.currentUser != null

    override suspend fun login(email: String, password: String): Result<User> = runCatching {
        val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        val firebaseUser = result.user ?: throw Exception("Login failed")
        val snapshot = database.getReference("users/${firebaseUser.uid}").get().await()
        val nickname = snapshot.child("nickname").getValue(String::class.java) ?: ""
        val avatar = snapshot.child("avatar").getValue(String::class.java) ?: ""
        User(uid = firebaseUser.uid, email = firebaseUser.email ?: "", nickname = nickname, avatar = avatar)
    }

    override suspend fun register(email: String, password: String, nickname: String): Result<User> = runCatching {
        val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        val firebaseUser = result.user ?: throw Exception("Registration failed")
        val user = User(uid = firebaseUser.uid, email = email, nickname = nickname)
        database.getReference("users/${firebaseUser.uid}").setValue(
            mapOf("uid" to user.uid, "email" to user.email, "nickname" to user.nickname, "avatar" to user.avatar)
        ).await()
        user
    }

    override suspend fun logout() {
        firebaseAuth.signOut()
    }

    override fun getUserFlow(): Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                trySend(User(uid = firebaseUser.uid, email = firebaseUser.email ?: ""))
            } else {
                trySend(null)
            }
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }
}
