package com.geoguessr.android.data.remote

import com.geoguessr.android.data.remote.model.FirebaseUser
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthService @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val database: FirebaseDatabase
) {

    fun signUp(email: String, password: String, nickname: String): Task<String> {
        return firebaseAuth.createUserWithEmailAndPassword(email, password)
            .continueWithTask { task ->
                if (!task.isSuccessful) throw task.exception ?: Exception("Sign up failed")
                val uid = task.result.user?.uid ?: throw Exception("User ID is null")
                val user = FirebaseUser(email = email, nickname = nickname)
                database.getReference("users/$uid").setValue(user)
                    .continueWith { uid }
            }
    }

    fun signIn(email: String, password: String): Task<AuthResult> {
        return firebaseAuth.signInWithEmailAndPassword(email, password)
    }

    fun signOut() {
        firebaseAuth.signOut()
    }

    fun getCurrentUser(): FirebaseUser? {
        val user = firebaseAuth.currentUser ?: return null
        return FirebaseUser(email = user.email ?: "")
    }

    fun resetPassword(email: String): Task<Void> {
        return firebaseAuth.sendPasswordResetEmail(email)
    }
}
