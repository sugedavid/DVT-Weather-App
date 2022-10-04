package com.sogoamobile.dvtweatherapp.data.cities

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CitiesTable::class], version = 1, exportSchema = false)
abstract class CitiesDatabase: RoomDatabase() {

    abstract fun citiesDao(): CitiesDao

    companion object{
        @Volatile
        private var INSTANCE: CitiesDatabase? = null

        fun getDatabase(context: Context): CitiesDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null){
                return tempInstance
            }
            synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CitiesDatabase::class.java,
                    "cities_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}