package com.sogoamobile.dvtweatherapp.data.locationForecast

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "city_forecast_table")
data class LocationForecastTable (
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val day: Long,
    val main: String,
    val temperature: Int,
    )