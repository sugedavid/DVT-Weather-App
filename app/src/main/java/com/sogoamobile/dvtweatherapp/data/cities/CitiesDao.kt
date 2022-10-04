package com.sogoamobile.dvtweatherapp.data.cities

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface CitiesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addCities(notes: CitiesTable)

    @Update
    fun updateCity(citiesTable: CitiesTable)

    @Query("SELECT * FROM cities_table ORDER BY id ASC")
    fun readAllData(): LiveData<List<CitiesTable>>

}