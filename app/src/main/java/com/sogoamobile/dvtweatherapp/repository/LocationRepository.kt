package com.sogoamobile.dvtweatherapp.repository

import androidx.lifecycle.LiveData
import com.sogoamobile.dvtweatherapp.data.location.LocationDao
import com.sogoamobile.dvtweatherapp.data.location.LocationTable

class LocationRepository(private val locationDao: LocationDao) {

    val readAllData: LiveData<List<LocationTable>> = locationDao.readAllData()

    fun addLocation(locationTable: LocationTable){
        locationDao.addLocation(locationTable)
    }

    fun updateLocation(locationTable: LocationTable){
        locationDao.updateLocation(locationTable)
    }

    fun getCity(id: Int): LiveData<LocationTable>{
        return locationDao.getLocation(id)
    }
}