package com.sogoamobile.dvtweatherapp.data.cities

import android.content.ClipData
import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CitiesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addCities(citiesTable: CitiesTable)

    @Update
    fun updateCity(citiesTable: CitiesTable)

    @Query("SELECT * FROM cities_table ORDER BY id ASC")
    fun readAllData(): LiveData<List<CitiesTable>>

    @Query("SELECT * from cities_table WHERE id = :id")
    fun getCity(id: Int): LiveData<CitiesTable>

}