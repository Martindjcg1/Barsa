package com.example.barsa.Usuario



// Clase de datos para el perfil de usuario
data class UserProfile(
    val first_name: String? = null,
    val last_name: String? = null,
    val username: String? = null,
    val email: String? = null,
    val date_joined: String? = null,
    val role: String? = null
)

