package com.sogoamobile.dvtweatherapp.data.cityforecast

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.sogoamobile.dvtweatherapp.data.cities.CitiesDatabase
import com.sogoamobile.dvtweatherapp.data.cities.CitiesTable
import com.sogoamobile.dvtweatherapp.repository.CitiesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CityForecastViewModel(application: Application): AndroidViewModel(application) {

    private val _readCityForecast: LiveData<List<CityForecastTable>>
    val readCityForecast: LiveData<List<CityForecastTable>>

    private val _cityForecastDao = CitiesDatabase.getDatabase(application).cityForecastDao()

    init {
        _readCityForecast = _cityForecastDao.readCityForecast()
         readCityForecast = _readCityForecast
    }

    fun addCityForecast(cityForecastTable: CityForecastTable){
        viewModelScope.launch ( Dispatchers.IO) {
            _cityForecastDao.addCityForecast(cityForecastTable)
        }
    }

    fun updateCityForecast(cityForecastTable: CityForecastTable){
        viewModelScope.launch ( Dispatchers.IO) {
            _cityForecastDao.updateCityForecast(cityForecastTable)
        }
    }

    fun getCityForecast(id: Int): LiveData<CityForecastTable>{
        return _cityForecastDao.getCityForecast(id)
    }
}