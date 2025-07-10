package com.example.barsa.data.retrofit.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.barsa.Stores.TokenManager
import com.example.barsa.data.retrofit.models.ChangePasswordResponse
import com.example.barsa.data.retrofit.models.ListadoInventario
import com.example.barsa.data.retrofit.models.ListadoProduccion

import com.example.barsa.data.retrofit.models.LoginResponse
import com.example.barsa.data.retrofit.models.UsuarioInfoResponse
import com.example.barsa.data.retrofit.models.LogoutResponse
import com.example.barsa.data.retrofit.models.RefreshResponse
import com.example.barsa.data.retrofit.models.RegisterResponse
import com.example.barsa.data.retrofit.models.ToggleUserStatusResponse
import com.example.barsa.data.retrofit.models.UpdatePersonalInfoResponse
import com.example.barsa.data.retrofit.models.UpdateUserRequest
import com.example.barsa.data.retrofit.models.UpdateUserResponse
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

    sealed class GetUserDetailState {
        object Loading : GetUserDetailState()
        data class Success(val user: UserDetailResponse) : GetUserDetailState()
        data class Error(val message: String) : GetUserDetailState()
        object Initial : GetUserDetailState()
    }

    // NUEVO: Estado para actualizar información personal
    sealed class UpdatePersonalInfoState {
        object Idle : UpdatePersonalInfoState()
        object Loading : UpdatePersonalInfoState()
        data class Success(val response: UpdatePersonalInfoResponse) : UpdatePersonalInfoState()
        data class Error(val message: String) : UpdatePersonalInfoState()
    }

    // Estado para actualizar usuario
    sealed class UpdateUserState {
        object Idle : UpdateUserState()
        object Loading : UpdateUserState()
        data class Success(val response: UpdateUserResponse) : UpdateUserState()
        data class Error(val message: String) : UpdateUserState()
    }

    // Estado para activar/desactivar usuario
    sealed class ToggleUserStatusState {
        object Idle : ToggleUserStatusState()
        object Loading : ToggleUserStatusState()
        data class Success(val response: ToggleUserStatusResponse) : ToggleUserStatusState()
        data class Error(val message: String) : ToggleUserStatusState()
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

    private val _infoUsuarioResult = MutableStateFlow<Result<UsuarioInfoResponse>?>(null)
    val infoUsuarioResult: StateFlow<Result<UsuarioInfoResponse>?> = _infoUsuarioResult

    // NUEVO: StateFlow para actualizar información personal
    private val _updatePersonalInfoState = MutableStateFlow<UpdatePersonalInfoState>(UpdatePersonalInfoState.Idle)
    val updatePersonalInfoState: StateFlow<UpdatePersonalInfoState> = _updatePersonalInfoState

    // StateFlow para actualizar usuario
    private val _updateUserState = MutableStateFlow<UpdateUserState>(UpdateUserState.Idle)
    val updateUserState: StateFlow<UpdateUserState> = _updateUserState

    // StateFlow para activar/desactivar usuario
    private val _toggleUserStatusState = MutableStateFlow<ToggleUserStatusState>(ToggleUserStatusState.Idle)
    val toggleUserStatusState: StateFlow<ToggleUserStatusState> = _toggleUserStatusState

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

    // FUNCIÓN SIMPLIFICADA Y MÁS ROBUSTA: Solo actualizar nombreUsuario y email
    fun updatePersonalInfo(nombreUsuario: String, email: String?) {
        viewModelScope.launch {
            Log.d("UserViewModel", "Iniciando updatePersonalInfo")
            _updatePersonalInfoState.value = UpdatePersonalInfoState.Loading
            try {
                val result = userRepository.updatePersonalInfo(nombreUsuario, email)
                result.onSuccess { response ->
                    Log.d("UserViewModel", "UpdatePersonalInfo exitoso")
                    _updatePersonalInfoState.value = UpdatePersonalInfoState.Success(response)

                    // NO llamar a obtenerInfoUsuarioPersonal() inmediatamente para evitar conflictos
                    // Se puede llamar después si es necesario

                }.onFailure { error ->
                    Log.e("UserViewModel", "UpdatePersonalInfo falló: ${error.message}")
                    _updatePersonalInfoState.value = UpdatePersonalInfoState.Error(error.message ?: "Error desconocido")
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Exception en updatePersonalInfo ViewModel", e)
                _updatePersonalInfoState.value = UpdatePersonalInfoState.Error("Error inesperado: ${e.message}")
            }
        }
    }

    // Función para actualizar usuario
    fun updateUser(
        userId: String,
        nombre: String?,
        apellidos: String?,
        nombreUsuario: String?,
        email: String?,
        password: String?,
        rol: String?,
        estado: String?
    ) {
        viewModelScope.launch {
            Log.d("UserViewModel", "Iniciando updateUser para ID: $userId")
            _updateUserState.value = UpdateUserState.Loading
            try {
                val updateData = UpdateUserRequest(
                    nombre = nombre?.takeIf { it.isNotBlank() },
                    apellidos = apellidos?.takeIf { it.isNotBlank() },
                    nombreUsuario = nombreUsuario?.takeIf { it.isNotBlank() },
                    email = email?.takeIf { it.isNotBlank() },
                    password = password?.takeIf { it.isNotBlank() },
                    rol = rol?.takeIf { it.isNotBlank() },
                    estado = estado
                )

                val result = userRepository.updateUserById(userId, updateData)
                result.onSuccess { response ->
                    Log.d("UserViewModel", "UpdateUser exitoso")
                    _updateUserState.value = UpdateUserState.Success(response)

                    // Recargar la información del usuario después de actualizar
                    getUserDetail(userId)

                }.onFailure { error ->
                    Log.e("UserViewModel", "UpdateUser falló: ${error.message}")
                    _updateUserState.value = UpdateUserState.Error(error.message ?: "Error desconocido")
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Exception en updateUser ViewModel", e)
                _updateUserState.value = UpdateUserState.Error("Error inesperado: ${e.message}")
            }
        }
    }

    // Función para activar/desactivar usuario
    fun toggleUserStatus(userId: String) {
        viewModelScope.launch {
            Log.d("UserViewModel", "Iniciando toggleUserStatus para ID: $userId")
            _toggleUserStatusState.value = ToggleUserStatusState.Loading
            try {
                val result = userRepository.toggleUserStatus(userId)
                result.onSuccess { response ->
                    Log.d("UserViewModel", "ToggleUserStatus exitoso")
                    _toggleUserStatusState.value = ToggleUserStatusState.Success(response)

                    // Recargar la información del usuario después de cambiar el estado
                    getUserDetail(userId)

                }.onFailure { error ->
                    Log.e("UserViewModel", "ToggleUserStatus falló: ${error.message}")
                    _toggleUserStatusState.value = ToggleUserStatusState.Error(error.message ?: "Error desconocido")
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Exception en toggleUserStatus ViewModel", e)
                _toggleUserStatusState.value = ToggleUserStatusState.Error("Error inesperado: ${e.message}")
            }
        }
    }

    // NUEVO: Función para resetear el estado de actualización
    fun resetUpdatePersonalInfoState() {
        _updatePersonalInfoState.value = UpdatePersonalInfoState.Idle
    }

    // Función para resetear el estado de actualización de usuario
    fun resetUpdateUserState() {
        _updateUserState.value = UpdateUserState.Idle
    }

    // Función para resetear el estado de activar/desactivar usuario
    fun resetToggleUserStatusState() {
        _toggleUserStatusState.value = ToggleUserStatusState.Idle
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
        // NUEVO: Resetear también el estado de actualización personal
        _updatePersonalInfoState.value = UpdatePersonalInfoState.Idle
        _updateUserState.value = UpdateUserState.Idle
        _toggleUserStatusState.value = ToggleUserStatusState.Idle
    }

    sealed class BitacoraState {
        object Loading : BitacoraState()
        data class Success(
            val response: List<ListadoProduccion>,
            val totalPages: Int,
            val currentPage: Int
        ) : BitacoraState()
        data class Error(val message: String) : BitacoraState()
    }

    private val _bitacoraState = MutableStateFlow<BitacoraState>(BitacoraState.Loading)
    val bitacoraState: StateFlow<BitacoraState> = _bitacoraState

    private var totalPagesBitacora = 1

    private val _currentBitacoraPage = MutableStateFlow(1)
    val currentBitacoraPage: StateFlow<Int> = _currentBitacoraPage

    fun resetBitacoraState() {
        _bitacoraState.value = BitacoraState.Loading
    }

    fun getListadoBitacoraProduccion(
        page: Int = _currentBitacoraPage.value,
        folio: String? = null,
        usuario: String? = null,
        movimiento: String? = null,
        etapa: String? = null,
        fechaInicio: String? = null,
        fechaFin: String? = null,
        limit: Int? = null,
        id: Int? = null
    ) {
        _currentBitacoraPage.value = page
        viewModelScope.launch {
            Log.d("BitacoraViewModel", "Llamada a la API con página $page")
            _bitacoraState.value = BitacoraState.Loading
            try {
                val result = userRepository.getListadoBitacoraProduccion(
                    fechaInicio = fechaInicio,
                    fechaFin = fechaFin,
                    id = id,
                    folio = folio?.toIntOrNull(),
                    etapa = etapa,
                    movimiento = movimiento,
                    usuario = usuario,
                    page = page,
                    limit = limit
                )
                result.onSuccess { response ->
                    totalPagesBitacora = response.totalPages
                    _bitacoraState.value = BitacoraState.Success(
                        response = response.data,
                        totalPages = totalPagesBitacora,
                        currentPage = _currentBitacoraPage.value
                    )
                }.onFailure { error ->
                    _bitacoraState.value = BitacoraState.Error(error.message ?: "Error desconocido")
                }
            } catch (e: Exception) {
                _bitacoraState.value = BitacoraState.Error("Error inesperado")
            }
        }
    }

    fun nextBitacoraPage() {
        if (_currentBitacoraPage.value < totalPagesBitacora) {
            getListadoBitacoraProduccion(_currentBitacoraPage.value + 1)
            Log.d("BitacoraViewModel", "Siguiente página")
        }
    }

    fun previousBitacoraPage() {
        if (_currentBitacoraPage.value > 1) {
            getListadoBitacoraProduccion(_currentBitacoraPage.value - 1)
            Log.d("BitacoraViewModel", "Página anterior")
        }
    }

    //////////////////////////////

    sealed class BitacoraInventarioState {
        object Loading : BitacoraInventarioState()
        data class Success(
            val response: List<ListadoInventario>,
            val totalPages: Int,
            val currentPage: Int
        ) : BitacoraInventarioState()
        data class Error(val message: String) : BitacoraInventarioState()
    }

    private val _bitacoraInventarioState = MutableStateFlow<BitacoraInventarioState>(BitacoraInventarioState.Loading)
    val bitacoraInventarioState: StateFlow<BitacoraInventarioState> = _bitacoraInventarioState

    private var totalPagesInventario = 1

    private val _currentInventarioPage = MutableStateFlow(1)
    val currentInventarioPage: StateFlow<Int> = _currentInventarioPage

    fun resetBitacoraInventarioState() {
        _bitacoraInventarioState.value = BitacoraInventarioState.Loading
    }

    fun getListadoBitacoraInventario(
        page: Int = _currentInventarioPage.value,
        fechaInicio: String? = null,
        fechaFin: String? = null,
        id: Int? = null,
        codigo: String? = null,
        limit: Int? = null
    ) {
        _currentInventarioPage.value = page
        viewModelScope.launch {
            Log.d("BitacoraInventarioVM", "Llamada a API Inventario página $page")
            _bitacoraInventarioState.value = BitacoraInventarioState.Loading
            try {
                val result = userRepository.getListadoBitacoraInventario(
                    page = page,
                    limit = limit,
                    fechaInicio = fechaInicio,
                    fechaFin = fechaFin,
                    id = id,
                    codigo = codigo
                )

                result.onSuccess { response ->
                    totalPagesInventario = response.totalPages
                    _bitacoraInventarioState.value = BitacoraInventarioState.Success(
                        response = response.data,
                        totalPages = totalPagesInventario,
                        currentPage = _currentInventarioPage.value
                    )
                }.onFailure { error ->
                    _bitacoraInventarioState.value = BitacoraInventarioState.Error(error.message ?: "Error desconocido")
                }
            } catch (e: Exception) {
                _bitacoraInventarioState.value = BitacoraInventarioState.Error("Error inesperado")
            }
        }
    }

    fun nextInventarioPage() {
        if (_currentInventarioPage.value < totalPagesInventario) {
            getListadoBitacoraInventario(_currentInventarioPage.value + 1)
            Log.d("BitacoraInventarioVM", "Siguiente página")
        }
    }

    fun previousInventarioPage() {
        if (_currentInventarioPage.value > 1) {
            getListadoBitacoraInventario(_currentInventarioPage.value - 1)
            Log.d("BitacoraInventarioVM", "Página anterior")
        }
    }

}
