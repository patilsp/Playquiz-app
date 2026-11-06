package com.example.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.User
import com.example.data.repository.AuthRepository
import com.example.data.repository.UserRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data class Authenticated(val user: FirebaseUser, val userProfile: User?) : AuthUiState
    data class Unauthenticated(val errorMessage: String? = null) : AuthUiState
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = AuthRepository()
    private val userRepository = UserRepository(application)

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { firebaseUser ->
                if (firebaseUser != null) {
                    val profile = userRepository.ensureUserExists(
                        uid = firebaseUser.uid,
                        displayName = firebaseUser.displayName ?: "Quiz Champion",
                        schoolClass = 4
                    )
                    _uiState.value = AuthUiState.Authenticated(firebaseUser, profile)
                } else {
                    _uiState.value = AuthUiState.Unauthenticated()
                }
            }
        }
    }

    fun checkAutoSignIn(context: Context) {
        val credentialManager = CredentialManager.create(context)
        authRepository.attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = { firebaseUser ->
                onSignInSuccess(firebaseUser)
            },
            onUnauthenticated = {
                _uiState.value = AuthUiState.Unauthenticated()
            },
            scope = viewModelScope
        )
    }

    fun signInWithGoogle(activity: Activity, onError: (String) -> Unit) {
        val credentialManager = CredentialManager.create(activity)
        _uiState.value = AuthUiState.Loading
        authRepository.signInWithGoogle(
            activity = activity,
            credentialManager = credentialManager,
            onAuthSuccess = { firebaseUser ->
                onSignInSuccess(firebaseUser)
            },
            onAuthError = { errorMsg ->
                _uiState.value = AuthUiState.Unauthenticated(errorMsg)
                onError(errorMsg)
            },
            scope = viewModelScope,
            onAuthCancelled = {
                _uiState.value = AuthUiState.Unauthenticated()
            }
        )
    }

    private fun onSignInSuccess(firebaseUser: FirebaseUser) {
        viewModelScope.launch {
            try {
                // Ensure initial user profile is stored in Firestore with user name, class level, and points (xp & stars)
                val userProfile = userRepository.ensureUserExists(
                    uid = firebaseUser.uid,
                    displayName = firebaseUser.displayName ?: "Quiz Champion",
                    schoolClass = 4
                )
                _uiState.value = AuthUiState.Authenticated(firebaseUser, userProfile)
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Authenticated(firebaseUser, null)
            }
        }
    }

    fun signOut(context: Context) {
        val credentialManager = CredentialManager.create(context)
        authRepository.signOut(credentialManager, {
            _uiState.value = AuthUiState.Unauthenticated()
        }, viewModelScope)
    }
}
