package com.sogoamobile.dvtweatherapp.data.cityforecast

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "city_forecast_table")
data class CityForecastTable (
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val day: Long,
    val imageIcon: String,
    val temperature: Int,
    )