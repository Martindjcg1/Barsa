package com.example.barsa.data.local

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
    val id: Int = 0,
    val procesoFolio: Int, // Clave foránea
    val etapa: String, // Etapas: "Madera", "Pintura", "Tapicería", "Empaque"
    val tiempo: Int, // Tiempo en segundos
    val fechaInicio: Long,
    val fechaFin: Long,
    val isRunning: Boolean = false, // Indica si esta etapa está en curso
    val isFinished: Boolean = false // Saber si la etapa finalizo
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
    val id: Int = 0,
    val tiempoId: Int,       // FK al tiempo específico
    val etapa: String,       // Etapa en la que ocurrió la detención
    val motivo: String,      // Motivo de la detención
    val fecha: Long,      // Fecha y hora en formato timestamp
    val activa: Boolean = false
)
