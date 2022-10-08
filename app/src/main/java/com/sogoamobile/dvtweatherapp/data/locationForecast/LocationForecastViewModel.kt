package com.sogoamobile.dvtweatherapp.data.locationForecast

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.sogoamobile.dvtweatherapp.data.location.LocationDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocationForecastViewModel(application: Application): AndroidViewModel(application) {

    private val _readCityForecast: LiveData<List<LocationForecastTable>>
    val readCityForecast: LiveData<List<LocationForecastTable>>

    private val _cityForecastDao = LocationDatabase.getDatabase(application).cityForecastDao()

    init {
        _readCityForecast = _cityForecastDao.readCityForecast()
         readCityForecast = _readCityForecast
    }

    fun addCityForecast(locationForecastTable: LocationForecastTable){
        viewModelScope.launch ( Dispatchers.IO) {
            _cityForecastDao.addCityForecast(locationForecastTable)
        }
    }

    fun updateCityForecast(locationForecastTable: LocationForecastTable){
        viewModelScope.launch ( Dispatchers.IO) {
            _cityForecastDao.updateCityForecast(locationForecastTable)
        }
    }

    fun getCityForecast(id: Int): LiveData<LocationForecastTable>{
        return _cityForecastDao.getCityForecast(id)
    }
}