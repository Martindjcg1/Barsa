package com.example.barsa.data.retrofit.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.barsa.Stores.TokenManager
import com.example.barsa.data.retrofit.models.ChangePasswordResponse

import com.example.barsa.data.retrofit.models.LoginResponse
import com.example.barsa.data.retrofit.models.UsuarioInfoResponse
import com.example.barsa.data.retrofit.models.LogoutResponse
import com.example.barsa.data.retrofit.models.RefreshResponse
import com.example.barsa.data.retrofit.models.RegisterResponse
import com.example.barsa.data.retrofit.models.UserDetailResponse
import com.example.barsa.data.retrofit.models.UserProfile
import com.example.barsa.data.retrofit.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
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
        object Initial : LoginState()
    }

    sealed class RegisterState {
        object Loading : RegisterState()
        data class Success(val response: RegisterResponse) : RegisterState()
        data class Error(val message: String) : RegisterState()
    }

    sealed class LogoutState {
        object Loading : LogoutState()
        data class Success(val response: LogoutResponse) : LogoutState()
        data class Error(val message: String) : LogoutState()
    }

    sealed class RefreshState {
        object Loading : RefreshState()
        data class Success(val response: RefreshResponse) : RefreshState()
        data class Error(val message: String) : RefreshState()
    }

    sealed class ChangePasswordState {
        object Loading : ChangePasswordState()
        data class Success(val response: ChangePasswordResponse) : ChangePasswordState()
        data class Error(val message: String) : ChangePasswordState()
    }

    sealed class GetUsersState {
        object Loading : GetUsersState()
        data class Success(val users: List<UserProfile>) : GetUsersState()
        data class Error(val message: String) : GetUsersState()
        object Initial : GetUsersState()
    }

    // NUEVO ESTADO PARA INFORMACIÓN DETALLADA DE USUARIO
    sealed class GetUserDetailState {
        object Loading : GetUserDetailState()
        data class Success(val user: UserDetailResponse) : GetUserDetailState()
        data class Error(val message: String) : GetUserDetailState()
        object Initial : GetUserDetailState()
    }

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Initial)
    val loginState: StateFlow<LoginState> = _loginState

    private val _registerState = MutableStateFlow<RegisterState?>(null)
    val registerState: StateFlow<RegisterState?> = _registerState

    private val _logoutState = MutableStateFlow<LogoutState?>(null)
    val logoutState: StateFlow<LogoutState?> = _logoutState

    private val _refreshState = MutableStateFlow<RefreshState?>(null)
    val refreshState: StateFlow<RefreshState?> = _refreshState

    private val _changePasswordState = MutableStateFlow<ChangePasswordState?>(null)
    val changePasswordState: StateFlow<ChangePasswordState?> = _changePasswordState

    private val _getUsersState = MutableStateFlow<GetUsersState>(GetUsersState.Initial)
    val getUsersState: StateFlow<GetUsersState> = _getUsersState

    private val _getUserDetailState = MutableStateFlow<GetUserDetailState>(GetUserDetailState.Initial)
    val getUserDetailState: StateFlow<GetUserDetailState> = _getUserDetailState

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

    private val _infoUsuarioResult = MutableStateFlow<Result<UsuarioInfoResponse>?>(null)
    val infoUsuarioResult: StateFlow<Result<UsuarioInfoResponse>?> = _infoUsuarioResult

    fun resetInfoUsuarioResult() {
        _infoUsuarioResult.value = null
    }

    fun obtenerInfoUsuarioPersonal() {
        viewModelScope.launch {
            val result = userRepository.obtenerInfoUsuarioPersonal()
            _infoUsuarioResult.value = result

            result.onSuccess { response ->
                tokenManager.saveUsuarioInfo(response.nombre, response.nombreUsuario, response.rol)
               // Log.d("","${response.nombre}, ${response.nombreUsuario}, ${response.rol}")
            }

            result.onFailure { error ->
                Log.d("obtenerInfoUsuarioVM Error", error.message ?: "Error al obtener info del usuario")
            }
        }
    }

    fun register(
        nombre: String,
        apellidos: String?,
        nombreUsuario: String,
        email: String?,
        password: String,
        rolString: String
    ) {
        viewModelScope.launch {
            _registerState.value = RegisterState.Loading
            try {
                val result = userRepository.register(nombre, apellidos, nombreUsuario, email, password, rolString)
                result.onSuccess { response ->
                    _registerState.value = RegisterState.Success(response)
                }.onFailure { error ->
                    _registerState.value = RegisterState.Error(error.message ?: "Error desconocido")
                }
            } catch (e: Exception) {
                _registerState.value = RegisterState.Error("Error inesperado")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _logoutState.value = LogoutState.Loading
            try {
                val result = userRepository.logout()
                result.onSuccess { response ->
                    tokenManager.clearTokens()
                    _loginState.value = LoginState.Initial
                    _logoutState.value = LogoutState.Success(response)
                }.onFailure { error ->
                    _logoutState.value = LogoutState.Error(error.message ?: "Error desconocido")
                }
            } catch (e: Exception) {
                _logoutState.value = LogoutState.Error("Error inesperado")
            }
        }
    }

    fun refreshToken() {
        viewModelScope.launch {
            _refreshState.value = RefreshState.Loading
            try {
                val result = userRepository.refreshToken()
                result.onSuccess { response ->
                    tokenManager.saveTokens(response.access_token, response.refresh_token)
                    _refreshState.value = RefreshState.Success(response)
                }.onFailure { error ->
                    _refreshState.value = RefreshState.Error(error.message ?: "Error desconocido")
                }
            } catch (e: Exception) {
                _refreshState.value = RefreshState.Error("Error inesperado")
            }
        }
    }

    fun changePassword(oldPassword: String, newPassword: String) {
        viewModelScope.launch {
            _changePasswordState.value = ChangePasswordState.Loading
            try {
                val result = userRepository.changePassword(oldPassword, newPassword)
                result.onSuccess { response ->
                    _changePasswordState.value = ChangePasswordState.Success(response)
                }.onFailure { error ->
                    _changePasswordState.value = ChangePasswordState.Error(error.message ?: "Error desconocido")
                }
            } catch (e: Exception) {
                _changePasswordState.value = ChangePasswordState.Error("Error inesperado")
            }
        }
    }

    fun getUsers(
        nombre: String? = null,
        nombreUsuario: String? = null,
        email: String? = null,
        rol: String? = null,
        estado: String? = null
    ) {
        viewModelScope.launch {
            Log.d("UserViewModel", "Starting getUsers request")
            _getUsersState.value = GetUsersState.Loading
            try {
                val result = userRepository.getUsers(nombre, nombreUsuario, email, rol, estado)
                result.onSuccess { users ->
                    Log.d("UserViewModel", "GetUsers success in ViewModel - ${users.size} users")
                    _getUsersState.value = GetUsersState.Success(users)
                }.onFailure { error ->
                    Log.e("UserViewModel", "GetUsers failure: ${error.message}")
                    _getUsersState.value = GetUsersState.Error(error.message ?: "Error desconocido")
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Exception in getUsers ViewModel", e)
                _getUsersState.value = GetUsersState.Error("Error inesperado: ${e.message}")
            }
        }
    }

    // NUEVO MÉTODO PARA OBTENER INFORMACIÓN DETALLADA DE USUARIO
    fun getUserDetail(userId: String) {
        viewModelScope.launch {
            Log.d("UserViewModel", "Starting getUserDetail request for ID: $userId")
            _getUserDetailState.value = GetUserDetailState.Loading
            try {
                val result = userRepository.getUserDetail(userId)
                result.onSuccess { user ->
                    Log.d("UserViewModel", "GetUserDetail success in ViewModel for user: ${user.nombreUsuario}")
                    _getUserDetailState.value = GetUserDetailState.Success(user)
                }.onFailure { error ->
                    Log.e("UserViewModel", "GetUserDetail failure: ${error.message}")
                    _getUserDetailState.value = GetUserDetailState.Error(error.message ?: "Error desconocido")
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Exception in getUserDetail ViewModel", e)
                _getUserDetailState.value = GetUserDetailState.Error("Error inesperado: ${e.message}")
            }
        }
    }

    fun checkAndRefreshToken() {
        viewModelScope.launch {
            val accessToken = tokenManager.accessTokenFlow.firstOrNull()
            val refreshToken = tokenManager.refreshTokenFlow.firstOrNull()

            if (accessToken.isNullOrEmpty() && !refreshToken.isNullOrEmpty()) {
                refreshToken()
            }
        }
    }

    fun resetLoginState() {
        _loginState.value = LoginState.Initial
    }

    fun clearAllStates() {
        _loginState.value = LoginState.Initial
        _registerState.value = null
        _logoutState.value = null
        _refreshState.value = null
        _changePasswordState.value = null
        _getUsersState.value = GetUsersState.Initial
        _getUserDetailState.value = GetUserDetailState.Initial
    }
}
