package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

data class AuthUser(
    val uid: String,
    val email: String,
    val displayName: String = ""
)

class AuthRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    private var firebaseAuth: FirebaseAuth? = null

    init {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                firebaseAuth = FirebaseAuth.getInstance()
                val currentFbUser = firebaseAuth?.currentUser
                if (currentFbUser != null) {
                    _currentUser.value = AuthUser(
                        uid = currentFbUser.uid,
                        email = currentFbUser.email ?: "",
                        displayName = currentFbUser.displayName ?: "Agente Turístico"
                    )
                }
            }
        } catch (e: Exception) {
            firebaseAuth = null
        }

        // Check local saved session if no active Firebase user in memory
        if (_currentUser.value == null) {
            val savedUid = prefs.getString("saved_uid", null)
            val savedEmail = prefs.getString("saved_email", null)
            if (savedUid != null && savedEmail != null) {
                _currentUser.value = AuthUser(
                    uid = savedUid,
                    email = savedEmail,
                    displayName = prefs.getString("saved_name", "Agente Turístico") ?: "Agente"
                )
            }
        }
    }

    fun isFirebaseAvailable(): Boolean {
        return try {
            FirebaseApp.getApps(context).isNotEmpty() && firebaseAuth != null
        } catch (e: Exception) {
            false
        }
    }

    suspend fun login(email: String, pass: String): Result<AuthUser> {
        val auth = firebaseAuth
        return if (auth != null) {
            try {
                val authResult = auth.signInWithEmailAndPassword(email, pass).await()
                val user = authResult.user
                val loggedUser = AuthUser(
                    uid = user?.uid ?: "fb_user",
                    email = user?.email ?: email,
                    displayName = user?.displayName ?: "Agente Turístico"
                )
                saveSession(loggedUser)
                Result.success(loggedUser)
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            // Local fallback when firebase is not initialized
            val storedPassword = prefs.getString("pass_$email", null)
            if (storedPassword != null && storedPassword != pass) {
                return Result.failure(Exception("Contraseña incorrecta para el usuario."))
            }
            val user = AuthUser(
                uid = "agent_" + email.hashCode().toString(),
                email = email,
                displayName = email.substringBefore("@").replaceFirstChar { it.uppercase() }
            )
            saveSession(user)
            Result.success(user)
        }
    }

    suspend fun register(email: String, pass: String): Result<AuthUser> {
        val auth = firebaseAuth
        return if (auth != null) {
            try {
                val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
                val user = authResult.user
                val newUser = AuthUser(
                    uid = user?.uid ?: "fb_user",
                    email = user?.email ?: email,
                    displayName = "Agente " + email.substringBefore("@")
                )
                saveSession(newUser)
                Result.success(newUser)
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            // Local fallback register
            prefs.edit().putString("pass_$email", pass).apply()
            val user = AuthUser(
                uid = "agent_" + email.hashCode().toString(),
                email = email,
                displayName = "Agente " + email.substringBefore("@")
            )
            saveSession(user)
            Result.success(user)
        }
    }

    suspend fun signInWithGoogleCredential(
        idToken: String,
        email: String? = null,
        displayName: String? = null
    ): Result<AuthUser> {
        val auth = firebaseAuth
        return if (auth != null) {
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(credential).await()
                val user = authResult.user
                val loggedUser = AuthUser(
                    uid = user?.uid ?: "google_user",
                    email = user?.email ?: (email ?: "usuario.google@gmail.com"),
                    displayName = user?.displayName ?: (displayName ?: "Usuario Google")
                )
                saveSession(loggedUser)
                Result.success(loggedUser)
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            val user = AuthUser(
                uid = "google_" + (email ?: "account").hashCode().toString(),
                email = email ?: "usuario.google@gmail.com",
                displayName = displayName ?: "Usuario Google"
            )
            saveSession(user)
            Result.success(user)
        }
    }

    private fun saveSession(user: AuthUser) {
        _currentUser.value = user
        prefs.edit()
            .putString("saved_uid", user.uid)
            .putString("saved_email", user.email)
            .putString("saved_name", user.displayName)
            .apply()
    }

    fun logout() {
        try {
            firebaseAuth?.signOut()
        } catch (_: Exception) {}
        prefs.edit()
            .remove("saved_uid")
            .remove("saved_email")
            .remove("saved_name")
            .apply()
        _currentUser.value = null
    }
}
