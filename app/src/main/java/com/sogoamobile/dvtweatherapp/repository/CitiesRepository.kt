package com.sogoamobile.dvtweatherapp.repository

import android.content.ClipData
import androidx.lifecycle.LiveData
import com.sogoamobile.dvtweatherapp.data.cities.CitiesDao
import com.sogoamobile.dvtweatherapp.data.cities.CitiesTable

class CitiesRepository(private val citiesDao: CitiesDao) {

    val readAllData: LiveData<List<CitiesTable>> = citiesDao.readAllData()

    fun addCities(citiesTable: CitiesTable){
        citiesDao.addCities(citiesTable)
    }

    fun updateCities(citiesTable: CitiesTable){
        citiesDao.updateCity(citiesTable)
    }

    fun getCity(id: Int): LiveData<CitiesTable>{
        return citiesDao.getCity(id)
    }
}