package com.example.barsa.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tiempos")
data class Tiempo (
    val tipoId: String,
    @PrimaryKey
    val folio: Int,
    val fecha: String,
    val status: String,
    val tiempo: Int,
    val isRunning: Boolean
    )