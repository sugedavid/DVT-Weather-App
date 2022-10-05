package com.sogoamobile.dvtweatherapp.data.cityforecast

import android.content.ClipData
import androidx.lifecycle.LiveData
import androidx.room.*
import com.sogoamobile.dvtweatherapp.data.cities.CitiesTable
import kotlinx.coroutines.flow.Flow

@Dao
interface CityForecastDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addCityForecast(cityForecastTable: CityForecastTable)

    @Update
    fun updateCityForecast(cityForecastTable: CityForecastTable)

    @Query("SELECT * FROM city_forecast_table ORDER BY id ASC")
    fun readCityForecast(): LiveData<List<CityForecastTable>>

    @Query("SELECT * from city_forecast_table WHERE id = :id")
    fun getCityForecast(id: Int): LiveData<CityForecastTable>

}