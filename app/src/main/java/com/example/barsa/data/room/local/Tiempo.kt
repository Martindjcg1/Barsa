package com.example.barsa.data.room.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "procesos")
data class Proceso(
    @PrimaryKey
    val folio: Int,
    val tipoId: String,
    val fecha: Long,
    val status: String
)

@Entity(
    tableName = "tiempos",
    foreignKeys = [ForeignKey(
        entity = Proceso::class,
        parentColumns = ["folio"],
        childColumns = ["procesoFolio"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("procesoFolio"), Index(value = ["procesoFolio", "etapa"], unique = true)]
)
data class Tiempo(
    @PrimaryKey(autoGenerate = true)
// Tiempo
    val id: Int = 0, // Clave primaria
    val procesoFolio: Int, // Clave foránea a Papeleta
    val etapa: String,
    val tiempo: Int,
    val fechaInicio: Long,
    val fechaFin: Long,
    val isRunning: Boolean = false,
    val isFinished: Boolean = false
)
@Entity(
    tableName = "detenciones",
    foreignKeys = [
        ForeignKey(
            entity = Tiempo::class,
            parentColumns = ["id"],
            childColumns = ["tiempoId"],
            onDelete = ForeignKey.CASCADE // Si un tiempo es eliminado, también se eliminan sus detenciones
        )
    ],
    indices = [Index("tiempoId")] // Índice para mejorar búsquedas
)
data class Detencion(
    @PrimaryKey(autoGenerate = true)
// Detencion
    val id: Int = 0, // Clave primaria
    val tiempoId: Int,       // Clave foranea a Tiempo (id)
    val folioPapeleta: Int,
    val etapa: String,
    val motivo: String,
    val fecha: Long,
    val activa: Boolean = false
)
