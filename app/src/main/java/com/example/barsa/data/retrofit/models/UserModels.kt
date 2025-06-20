package com.example.barsa.data.retrofit.models

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val nombreUsuario: String,
    val password: String
)

data class LoginResponse(
    val access_token: String,
    val refresh_token: String
)

data class ErrorResponse(
    val message: String,
    val error: String,
    val status: Int
)

data class RegisterRequest(
    val nombre: String,
    val apellidos: String?,
    val nombreUsuario: String,
    val email: String?,
    val password: String,
    val rol: String
)

data class RegisterResponse(
    val message: String,
    val user: UserData
)

data class UserData(
    val id: Int,
    val nombre: String,
    val apellidos: String?,
    val nombreUsuario: String,
    val email: String?,
    val rol: String
)


data class LogoutResponse(
    val message: String
)

data class RefreshResponse(
    val access_token: String,
    val refresh_token: String
)


data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)

data class ChangePasswordResponse(
    val message: String
)


// MODELO PARA USUARIOS EN LA LISTA - ACTUALIZADO PARA CAPTURAR EL ID CORRECTAMENTE
data class UserProfile(
    @SerializedName("_id") val id: String? = null, // CAMBIADO: usar _id que es como viene del servidor
    @SerializedName("nombre") val first_name: String? = null,
    @SerializedName("apellidos") val last_name: String? = null,
    @SerializedName("nombreUsuario") val username: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("rol") val role: String? = null,
    @SerializedName("estado") val active: Boolean = true,
    val created_at: String? = null
)

// MODELO PARA INFORMACIÓN DETALLADA DE USUARIO
data class UserDetailResponse(
    @SerializedName("_id") val id: String,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("apellidos") val apellidos: String?,
    @SerializedName("nombreUsuario") val nombreUsuario: String,
    @SerializedName("email") val email: String?,
    @SerializedName("rol") val rol: String,
    @SerializedName("estado") val estado: Boolean
)


data class ErrorResponseInfo(
    val message: String,
    val statusCode: Int
)

data class UsuarioInfoResponse(
    @SerializedName("_id") val id: String,
    val nombre: String,
    val apellidos: String,
    val nombreUsuario: String,
    val email: String,
    val rol: String,
    val estado: Boolean
)


data class UpdatePersonalInfoRequest(
    val nombreUsuario: String,
    val email: String?
)

// Hacemos la respuesta más flexible para manejar diferentes formatos del servidor
data class UpdatePersonalInfoResponse(
    val message: String? = null,
    val success: Boolean? = null,
    val status: String? = null,
    val data: Any? = null
)

// Modelo para la request de actualización de usuario
data class UpdateUserRequest(
    val nombre: String?,
    val apellidos: String?,
    val nombreUsuario: String?,
    val email: String?,
    val password: String?,
    val rol: String?,
    val estado: String? // "true" o "false"
)

// Modelo para la respuesta de actualización de usuario
data class UpdateUserResponse(
    val message: String? = null,
    val success: Boolean? = null,
    val status: String? = null,
    val data: Any? = null
)

// Modelo para la respuesta de activar/desactivar usuario
data class ToggleUserStatusResponse(
    val message: String? = null,
    val success: Boolean? = null,
    val status: String? = null,
    val data: Any? = null
)

