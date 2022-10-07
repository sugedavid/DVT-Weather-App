package com.sogoamobile.dvtweatherapp.data.location

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface LocationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addCities(locationTable: LocationTable)

    @Update
    fun updateCity(locationTable: LocationTable)

    @Query("SELECT * FROM location_table ORDER BY id ASC")
    fun readAllData(): LiveData<List<LocationTable>>

    @Query("SELECT * from location_table WHERE id = :id")
    fun getCity(id: Int): LiveData<LocationTable>

}