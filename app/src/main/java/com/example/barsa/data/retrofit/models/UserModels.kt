package com.example.barsa.data.retrofit.models

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