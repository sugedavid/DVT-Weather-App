package com.sogoamobile.dvtweatherapp.data.cities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cities_table")
data class CitiesTable (
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val cityName: String,
    val description: String,
    val refreshTime: Long,
    val temperature: Int,
    var isFavourite: Boolean
    )