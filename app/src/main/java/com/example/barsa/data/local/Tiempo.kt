package com.example.barsa.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tiempos")
data class Tiempo (
    @PrimaryKey
    val folio: String,
    val cantidad: Int,
    val fecha: String,
    val tiempo: String
    )