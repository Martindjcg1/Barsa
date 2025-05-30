package com.example.barsa.data.retrofit.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.barsa.Stores.TokenManager
import com.example.barsa.data.retrofit.models.LoginResponse
import com.example.barsa.data.retrofit.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository,
    val tokenManager: TokenManager
) : ViewModel() {
    sealed class LoginState {
        object Loading : LoginState()
        data class Success(val response: LoginResponse) : LoginState()
        data class Error(val message: String) : LoginState()
    }

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Loading)
    val loginState: StateFlow<LoginState> = _loginState

    fun login(nombreUsuario: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val result = userRepository.login(nombreUsuario, password)
                result.onSuccess { response ->
                    tokenManager.saveTokens(response.access_token, response.refresh_token)
                    _loginState.value = LoginState.Success(response)
                }.onFailure { error ->
                    _loginState.value = LoginState.Error(error.message ?: "Error desconocido")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error("Error inesperado")
            }
        }
    }
}