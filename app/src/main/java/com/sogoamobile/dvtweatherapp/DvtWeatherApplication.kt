package com.sogoamobile.dvtweatherapp

import android.app.Application
import com.sogoamobile.dvtweatherapp.data.location.LocationDatabase

/**
 * Override application to setup background work via WorkManager
 */
class DvtWeatherApplication : Application() {
    val database: LocationDatabase by lazy { LocationDatabase.getDatabase(this) }

}

