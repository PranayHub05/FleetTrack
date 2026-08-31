package com.pranay.fleettrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pranay.fleettrack.data.AuthRepository
import com.pranay.fleettrack.model.User
import com.pranay.fleettrack.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    init {
        checkExistingSession()
    }

    fun signIn(email: String, password: String, expectedRole: UserRole) {
        if (email.isBlank()) {
            _errorMessage.value = "Please enter your email address"
            return
        }
        if (password.isBlank()) {
            _errorMessage.value = "Please enter your password"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = authRepository.signIn(email.trim(), password)
                val user = result.getOrElse { e ->
                    _errorMessage.value = e.message ?: "Sign in failed. Please try again."
                    _currentUser.value = null
                    _isLoggedIn.value = false
                    return@launch
                }
                if (user.role != expectedRole) {
                    authRepository.signOut()
                    val roleName = when (expectedRole) {
                        UserRole.ADMIN -> "admin"
                        UserRole.DRIVER -> "driver"
                    }
                    _errorMessage.value = "This account doesn't have $roleName access"
                    _currentUser.value = null
                    _isLoggedIn.value = false
                } else {
                    _currentUser.value = user
                    _isLoggedIn.value = true
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Sign in failed. Please try again."
                _currentUser.value = null
                _isLoggedIn.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loginDriverWithCode(code: String) {
        if (code.isBlank()) {
            _errorMessage.value = "Please enter your 6-letter driver code"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = authRepository.loginDriverWithCode(code.trim().uppercase())
                val user = result.getOrElse { e ->
                    _errorMessage.value = e.message ?: "Invalid code. Please try again."
                    _currentUser.value = null
                    _isLoggedIn.value = false
                    return@launch
                }
                
                _currentUser.value = user
                _isLoggedIn.value = true
            } catch (e: Exception) {
                _errorMessage.value = "Sign in failed. Please try again."
                _currentUser.value = null
                _isLoggedIn.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                authRepository.signOut()
            } catch (_: Exception) {
                // Silently handle sign-out errors
            } finally {
                _currentUser.value = null
                _isLoggedIn.value = false
                _errorMessage.value = null
            }
        }
    }

    fun checkExistingSession() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = authRepository.getCurrentUser()
                if (user != null) {
                    _currentUser.value = user
                    _isLoggedIn.value = true
                } else {
                    _currentUser.value = null
                    _isLoggedIn.value = false
                }
            } catch (_: Exception) {
                _currentUser.value = null
                _isLoggedIn.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                    return AuthViewModel(AuthRepository.instance) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
}
