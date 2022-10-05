package com.sogoamobile.dvtweatherapp.data.cities

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.sogoamobile.dvtweatherapp.repository.CitiesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CitiesViewModel(application: Application): AndroidViewModel(application) {

    val readAllData: LiveData<List<CitiesTable>>
    private val repository: CitiesRepository
    val citiesDao = CitiesDatabase.getDatabase(application).citiesDao()

    init {
//        val citiesDao = CitiesDatabase.getDatabase(application).citiesDao()
        repository = CitiesRepository(citiesDao)
        readAllData = repository.readAllData
    }

    fun addCities(citiesTable: CitiesTable){
        viewModelScope.launch ( Dispatchers.IO) {
            repository.addCities(citiesTable)
        }
    }

    fun updateCities(citiesTable: CitiesTable){
        viewModelScope.launch ( Dispatchers.IO) {
            repository.updateCities(citiesTable)
        }
    }

    fun getCity(id: Int): LiveData<CitiesTable>{
        return citiesDao.getCity(id)
    }
}