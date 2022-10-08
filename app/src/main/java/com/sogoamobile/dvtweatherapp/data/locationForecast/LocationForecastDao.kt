package com.sogoamobile.dvtweatherapp.data.locationForecast

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface LocationForecastDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addCityForecast(locationForecastTable: LocationForecastTable)

    @Update
    fun updateCityForecast(locationForecastTable: LocationForecastTable)

    @Query("SELECT * FROM city_forecast_table ORDER BY id ASC")
    fun readCityForecast(): LiveData<List<LocationForecastTable>>

    @Query("SELECT * from city_forecast_table WHERE id = :id")
    fun getCityForecast(id: Int): LiveData<LocationForecastTable>

}