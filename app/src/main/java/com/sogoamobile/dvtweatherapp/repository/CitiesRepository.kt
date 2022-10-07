package com.sogoamobile.dvtweatherapp.repository

import androidx.lifecycle.LiveData
import com.sogoamobile.dvtweatherapp.data.location.LocationDao
import com.sogoamobile.dvtweatherapp.data.location.LocationTable

class CitiesRepository(private val citiesDao: LocationDao) {

    val readAllData: LiveData<List<LocationTable>> = citiesDao.readAllData()

    fun addCities(locationTable: LocationTable){
        citiesDao.addCities(locationTable)
    }

    fun updateCities(locationTable: LocationTable){
        citiesDao.updateCity(locationTable)
    }

    fun getCity(id: Int): LiveData<LocationTable>{
        return citiesDao.getCity(id)
    }
}