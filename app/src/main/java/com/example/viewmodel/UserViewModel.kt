package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.User
import com.example.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = UserRepository(application)

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    fun loadOrCreateUser(uid: String, displayName: String, schoolClass: Int = 4) {
        viewModelScope.launch {
            val user = repository.ensureUserExists(uid, displayName, schoolClass)
            _currentUser.value = user

            repository.observeUser(uid).collect { updated ->
                if (updated != null) {
                    _currentUser.value = updated
                }
            }
        }
    }

    fun updateClass(newClass: Int) {
        val uid = _currentUser.value?.uid ?: return
        val displayName = _currentUser.value?.displayName ?: "Quiz Champion"
        val avatar = _currentUser.value?.avatar ?: "mascot_star"

        viewModelScope.launch {
            repository.updateUserProfile(newClass, displayName, avatar)
        }
    }

    fun addXpAndCheckStreak(xpGained: Int, starsGained: Int) {
        viewModelScope.launch {
            repository.addXpAndCheckStreak(xpGained, starsGained)
        }
    }
}
