package com.sogoamobile.dvtweatherapp.data.cities

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.sogoamobile.dvtweatherapp.data.cityforecast.CityForecastDao
import com.sogoamobile.dvtweatherapp.data.cityforecast.CityForecastTable

@Database(entities = [CitiesTable::class, CityForecastTable::class], version = 3, exportSchema = false)
abstract class CitiesDatabase: RoomDatabase() {

    abstract fun citiesDao(): CitiesDao
    abstract fun cityForecastDao(): CityForecastDao

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
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}