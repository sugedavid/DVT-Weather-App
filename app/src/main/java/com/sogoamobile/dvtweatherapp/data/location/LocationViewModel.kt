package com.sogoamobile.dvtweatherapp.data.location

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.sogoamobile.dvtweatherapp.repository.CitiesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocationViewModel(application: Application): AndroidViewModel(application) {

    val readAllData: LiveData<List<LocationTable>>
    private val repository: CitiesRepository
    val citiesDao = LocationDatabase.getDatabase(application).citiesDao()

    init {
//        val citiesDao = CitiesDatabase.getDatabase(application).citiesDao()
        repository = CitiesRepository(citiesDao)
        readAllData = repository.readAllData
    }

    fun addCities(locationTable: LocationTable){
        viewModelScope.launch ( Dispatchers.IO) {
            repository.addCities(locationTable)
        }
    }

    fun updateCity(locationTable: LocationTable){
        viewModelScope.launch ( Dispatchers.IO) {
            repository.updateCities(locationTable)
        }
    }

    fun getCity(id: Int): LiveData<LocationTable>{
        return citiesDao.getCity(id)
    }
}