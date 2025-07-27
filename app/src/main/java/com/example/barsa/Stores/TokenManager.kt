package com.example.barsa.Stores

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore by preferencesDataStore(name = "user_prefs")

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val NOMBRE = stringPreferencesKey("nombre")
        val NOMBRE_USUARIO = stringPreferencesKey("nombreUsuario")
        val ROL = stringPreferencesKey("rol")
        val EXCLUSION_BATERIA_SOLICITADA = booleanPreferencesKey("exclusion_bateria_solicitada")

    }

    // TOKENS DE ACCESO

    val accessTokenFlow: Flow<String?> = dataStore.data
        .map { it[ACCESS_TOKEN] }

    val refreshTokenFlow: Flow<String?> = dataStore.data
        .map { it[REFRESH_TOKEN] }

    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN] = accessToken
            prefs[REFRESH_TOKEN] = refreshToken
        }
    }

    suspend fun clearTokens() {
        dataStore.edit { prefs ->
            prefs.remove(ACCESS_TOKEN)
            prefs.remove(REFRESH_TOKEN)
        }
    }

    // STORE´s DE INFORMACION DE USUARIO

    val accessNombre: Flow<String?> = dataStore.data
        .map { it[NOMBRE] }

    val accessNombreUsuario: Flow<String?> = dataStore.data
        .map { it[NOMBRE_USUARIO] }

    val accessRol: Flow<String?> = dataStore.data
        .map { it[ROL] }

    suspend fun saveUsuarioInfo(nombre: String, nombreUsuario: String, rol: String) {
        dataStore.edit { prefs ->
            prefs[NOMBRE] = nombre
            prefs[NOMBRE_USUARIO] = nombreUsuario
            prefs[ROL] = rol
        }
    }

    suspend fun clearUsuarioInfo() {
        dataStore.edit { prefs ->
            prefs.remove(NOMBRE)
            prefs.remove(NOMBRE_USUARIO)
            prefs.remove(ROL)
        }
    }

    val exclusionBateriaSolicitadaFlow: Flow<Boolean> = dataStore.data
        .map { prefs -> prefs[EXCLUSION_BATERIA_SOLICITADA] ?: false }

    suspend fun marcarExclusionBateriaSolicitada() {
        dataStore.edit { prefs ->
            prefs[EXCLUSION_BATERIA_SOLICITADA] = true
        }
    }


}

