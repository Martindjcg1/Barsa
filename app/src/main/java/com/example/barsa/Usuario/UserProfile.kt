package com.example.barsa.Usuario




 data class UserFakeProfile(
  val first_name: String? = null,
  val last_name: String? = null,
  val username: String? = null,
  val email: String? = null,
  val date_joined: String? = null,
  val role: String? = null,
  val active: Boolean = true // Nuevo campo para indicar si el usuario está activo
)

