package com.example.barsa.data.room.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Database class with a singleton Instance object.
 */
@Database(
    entities = [
        //Proceso::class,
        Tiempo::class, Detencion::class],
    version = 8,
    exportSchema = false
)
abstract class TiempoDatabase : RoomDatabase() {

    //abstract fun procesoDao(): ProcesoDao
    abstract fun tiempoDao(): TiempoDao
    abstract fun detencionDao(): DetencionDao

    companion object {
        @Volatile
        private var Instance: TiempoDatabase? = null

        fun getDatabase(context: Context): TiempoDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    TiempoDatabase::class.java,
                    "tiempo_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
