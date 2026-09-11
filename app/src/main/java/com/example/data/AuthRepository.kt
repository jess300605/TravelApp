package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

data class AuthUser(
    val uid: String,
    val email: String,
    val displayName: String = "",
    val isDemo: Boolean = false
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

        // If no Firebase user, check local saved session
        if (_currentUser.value == null) {
            val savedUid = prefs.getString("saved_uid", null)
            val savedEmail = prefs.getString("saved_email", null)
            if (savedUid != null && savedEmail != null) {
                _currentUser.value = AuthUser(
                    uid = savedUid,
                    email = savedEmail,
                    displayName = prefs.getString("saved_name", "Agente Turístico") ?: "Agente",
                    isDemo = prefs.getBoolean("is_demo", false)
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
                // If Firebase fails due to network or missing config, check local credential or return failure
                Result.failure(e)
            }
        } else {
            // Local mode fallback
            val user = AuthUser(
                uid = "agent_" + email.hashCode().toString(),
                email = email,
                displayName = email.substringBefore("@").replaceFirstChar { it.uppercase() },
                isDemo = false
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
            // Local mode register
            val user = AuthUser(
                uid = "agent_" + email.hashCode().toString(),
                email = email,
                displayName = "Agente " + email.substringBefore("@"),
                isDemo = false
            )
            saveSession(user)
            Result.success(user)
        }
    }

    fun loginDemo(): AuthUser {
        val demoUser = AuthUser(
            uid = "demo_agent_01",
            email = "agente.demo@viajes.com",
            displayName = "Agente Principal",
            isDemo = true
        )
        saveSession(demoUser)
        return demoUser
    }

    private fun saveSession(user: AuthUser) {
        _currentUser.value = user
        prefs.edit()
            .putString("saved_uid", user.uid)
            .putString("saved_email", user.email)
            .putString("saved_name", user.displayName)
            .putBoolean("is_demo", user.isDemo)
            .apply()
    }

    fun logout() {
        try {
            firebaseAuth?.signOut()
        } catch (_: Exception) {}
        prefs.edit().clear().apply()
        _currentUser.value = null
    }
}
