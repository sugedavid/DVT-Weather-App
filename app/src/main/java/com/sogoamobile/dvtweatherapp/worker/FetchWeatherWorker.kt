package com.sogoamobile.dvtweatherapp.worker

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.viewModelScope
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.sogoamobile.dvtweatherapp.DvtWeatherApplication
import com.sogoamobile.dvtweatherapp.common.Common
import com.sogoamobile.dvtweatherapp.data.location.LocationTable
import com.sogoamobile.dvtweatherapp.data.location.LocationViewModel
import com.sogoamobile.dvtweatherapp.network.IOpenWeatherMap
import com.sogoamobile.dvtweatherapp.network.RetrofitClient
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import retrofit2.Retrofit


class FetchWeatherWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    private val locationDao = DvtWeatherApplication().database.locationDao()

    // rxjava disposable
    private var compositeDisposable: CompositeDisposable? = null

    // retrofit
    private var mService: IOpenWeatherMap? = null

    override suspend fun doWork(): Result {
        val appContext = applicationContext

        compositeDisposable = CompositeDisposable()
        val retrofit: Retrofit? = RetrofitClient.instance
        mService = retrofit?.create(IOpenWeatherMap::class.java)

        val latitude = inputData.getString("latitude")
        val longitude = inputData.getString("longitude")

        return try {

            coroutineScope {

                Log.d("worker", "worker started")
                // fetch weather information
                compositeDisposable?.add(
                    mService!!.getWeatherByLatLng(
                        latitude, longitude,
                        applicationContext.getString(Common().apiKey),
                        "metric"
                    )
                    !!.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ weatherResult ->
                            val cityId = weatherResult?.id ?: 0
                            val cityName = weatherResult?.name ?: ""
                            val description = weatherResult?.weather?.get(0)?.description ?: ""
                            val main = weatherResult?.weather?.get(0)?.main ?: ""
                            val refreshTime = weatherResult?.dt!!.toLong()
                            val temperature = weatherResult.main?.temp?.toInt() ?: 0
                            val temperatureMin = weatherResult.main?.temp_min?.toInt() ?: 0
                            val temperatureMax = weatherResult.main?.temp_max?.toInt() ?: 0

                            // save weather info to db
                            locationDao.addLocation(
                                LocationTable(
                                    id = cityId,
                                    cityName = cityName,
                                    description = description,
                                    main = main,
                                    refreshTime = refreshTime,
                                    temperature = temperature,
                                    temperatureMin = temperatureMin,
                                    temperatureMax = temperatureMax,
                                    isFavourite = false,
                                    latitude = latitude ?: "0.0",
                                    longitude = longitude ?: "0.0",
                                )
                            )
                            // save locationID & condition to preference
                            Common().saveLocationID(applicationContext, cityId)
                            Common().saveCondition(applicationContext, description)

                        }, { throwable ->
                            Log.e("worker", "error:  ${throwable.message}")

                        })
                )
            }

            return Result.success()

        } catch (throwable: Throwable) {
            Log.e("worker", "error:  ${throwable.message}")
            throwable.printStackTrace()
            Result.failure()
        }

    }
}