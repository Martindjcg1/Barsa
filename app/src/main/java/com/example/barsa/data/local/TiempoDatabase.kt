package com.example.barsa.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Database class with a singleton Instance object.
 */
@Database(entities = [Tiempo::class], version = 1, exportSchema = true)
abstract class TiempoDatabase : RoomDatabase() {

    abstract fun tiempoDao(): TiempoDao

    companion object {
        @Volatile
        private var Instance: TiempoDatabase? = null

        fun getDatabase(context: Context): TiempoDatabase {
            // if the Instance is not null, return it, otherwise create a new database instance.
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, TiempoDatabase::class.java, "tiempo_database")
                    .build()
                    .also { Instance = it }
            }
        }
    }
}