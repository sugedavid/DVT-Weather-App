package com.sogoamobile.dvtweatherapp.presentation.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.sogoamobile.dvtweatherapp.adapter.WeatherForecastAdapter
import com.sogoamobile.dvtweatherapp.common.Common
import com.sogoamobile.dvtweatherapp.databinding.FragmentWeatherInfoBinding
import com.sogoamobile.dvtweatherapp.model.WeatherForecastResult
import com.sogoamobile.dvtweatherapp.network.IOpenWeatherMap
import com.sogoamobile.dvtweatherapp.network.RetrofitClient
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import java.lang.String
import kotlin.toString


class WeatherInfoFragment : Fragment() {

    companion object {
        const val long = "long"
        const val lat = "lat"
        const val city = "city"
    }

    private val instance: WeatherInfoFragment? = null
    private var txt_city_name: TextView? = null
    private var txt_humidity: TextView? = null
    private var txt_visibility: TextView? = null
    private var txt_sunset: TextView? = null
    private var txt_pressure: TextView? = null
    private var txt_description: TextView? = null
    private var txt_date_time: TextView? = null
    private var txt_wind: TextView? = null
    private var txt_temperature: TextView? = null
    private var txt_geo_cord: TextView? = null
    private var img_weather: ImageView? = null
    private var weather_panel: LinearLayout? = null
    private var loading: ProgressBar? = null

    private var compositeDisposable: CompositeDisposable? = null
    private var mService: IOpenWeatherMap? = null

    private var recycler_forecast: RecyclerView? = null

    private var dbLat = ""
    private var dbLng = ""
    private var mCity = ""

    private var _binding: FragmentWeatherInfoBinding? = null
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            dbLat = it.getString(lat).toString()
            dbLng = it.getString(long).toString()
            mCity = it.getString(city).toString()
        }

        compositeDisposable = CompositeDisposable()
        val retrofit: Retrofit? = RetrofitClient.instance
        mService = retrofit?.create(IOpenWeatherMap::class.java)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentWeatherInfoBinding.inflate(inflater, container, false)
        val view = binding.root



        img_weather = binding.imgWeather
        txt_city_name = binding.txtCityName
        txt_date_time = binding.txtDateTime
        txt_description = binding.txtDescription
        txt_humidity = binding.txtHumidity
        txt_pressure = binding.txtPressure
        txt_visibility = binding.txtVisibility
        txt_sunset = binding.txtSunset
        txt_wind = binding.txtWind
        txt_temperature = binding.txtTemperature
        weather_panel = binding.weatherPanel
        loading = binding.loading

        // home tab
        val homeTab = binding.weatherInfoAppbar.cdvHome
        homeTab.setOnClickListener {
            val action = WeatherInfoFragmentDirections.actionWeatherFragmentToHomeFragment()
            findNavController().navigate(action)
        }

        // notes tab
        val notesTab = binding.weatherInfoAppbar.cdvNotes
        notesTab.setOnClickListener {
            val action = WeatherInfoFragmentDirections.actionWeatherFragmentToNotesFragment()
            findNavController().navigate(action)
        }

        // check location permissions
        Dexter.withActivity(activity)
            .withPermissions(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        if (ActivityCompat.checkSelfPermission(
                                context!!,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            )
                            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                                context!!,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            return
                        }
                        //current weather
                        getWeatherInformation()
                        // weather forecast
                        getForecastWeatherInformation()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest>,
                    token: PermissionToken
                ) {
                    Snackbar.make(
                        activity!!.findViewById(android.R.id.content),
                        "Permission Denied",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }).check()

        return view
    }


    private fun getWeatherInformation() {

        if (dbLat.isNotEmpty() && dbLng.isNotEmpty()) {

            compositeDisposable?.add(
                mService!!.getWeatherByLatLng(
                    dbLat, dbLng,
                    getString(Common().apiKey),
                    "metric"
                )
                !!.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ weatherResult -> //Load Image
                        Picasso.get().load(
                            StringBuilder(Common().imageUrl)
                                .append(weatherResult?.weather?.get(0)?.icon)
                                .append(".png").toString()
                        ).into(img_weather)

                        //city
                        txt_city_name?.text = weatherResult?.name

                        //temperature
                        txt_temperature?.text = java.lang.StringBuilder(
                            String.valueOf(weatherResult?.main?.temp?.toInt())
                        )
                            .append("°C").toString()

                        //date
                        txt_date_time!!.text =
                             Common().convertUnixToDate(weatherResult?.dt!!.toLong()) + "\n\n" + Common().convertUnixToHour(weatherResult?.dt!!.toLong())

                        //pressure
                        txt_pressure?.text = java.lang.StringBuilder(
                            String.valueOf(
                                weatherResult.main?.pressure
                            )
                        )
                            .append(" hPa").toString()

                        //humidity
                        txt_humidity?.text = java.lang.StringBuilder(
                            String.valueOf(
                                weatherResult.main?.humidity
                            )
                        ).append(" %").toString()

                        //wind
                        txt_wind?.text = java.lang.StringBuilder(
                            String.valueOf(
                                (weatherResult.wind?.speed?.times(3.6))?.toInt()
                            )
                        ).append(" km/h").toString()

                        //visibility
                        txt_visibility?.text = java.lang.StringBuilder(
                            String.valueOf(
                                weatherResult.visibility / 1000
                            )
                        ).append(" km/h").toString()

                        txt_sunset?.text = Common().convertUnixToHour(
                            weatherResult.sys?.sunset!!.toLong()
                        )
                        txt_geo_cord?.text = java.lang.StringBuilder("[")
                            .append(weatherResult.coord.toString())
                            .append("]").toString()
                        weather_panel!!.visibility = View.VISIBLE
                        loading!!.visibility = View.GONE
                    }, { throwable ->
                        loading?.visibility = View.GONE
                        Snackbar.make(
                            requireActivity().findViewById(android.R.id.content),
                            throwable.message!!, Snackbar.LENGTH_LONG
                        ).show()
                    })
            )
        }
        else if (mCity.isNotEmpty()){
            compositeDisposable?.add(
                mService?.getWeatherByCity(
                    mCity,
                    getString(Common().apiKey),
                    "metric"
                )
                !!.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ weatherResult -> //Load Image
                        Picasso.get().load(
                            StringBuilder(Common().imageUrl)
                                .append(weatherResult?.weather?.get(0)?.icon)
                                .append(".png").toString()
                        ).into(img_weather)

                        //city
                        txt_city_name?.text = weatherResult?.name

                        //temperature
                        txt_temperature?.text = java.lang.StringBuilder(
                            String.valueOf(weatherResult?.main?.temp?.toInt())
                        )
                            .append("°C").toString()

                        //date
                        txt_date_time!!.text =
                            Common().convertUnixToDate(weatherResult?.dt!!.toLong()) + "\n\n" + Common().convertUnixToHour(weatherResult?.dt!!.toLong())

                        //pressure
                        txt_pressure?.text = java.lang.StringBuilder(
                            String.valueOf(
                                weatherResult.main?.pressure
                            )
                        )
                            .append(" hPa").toString()

                        //humidity
                        txt_humidity?.text = java.lang.StringBuilder(
                            String.valueOf(
                                weatherResult.main?.humidity
                            )
                        ).append(" %").toString()

                        //wind
                        txt_wind?.text = java.lang.StringBuilder(
                            String.valueOf(
                                (weatherResult.wind?.speed?.times(3.6))?.toInt()
                            )
                        ).append(" km/h").toString()

                        //visibility
                        txt_visibility?.text = java.lang.StringBuilder(
                            String.valueOf(
                                weatherResult.visibility / 1000
                            )
                        ).append(" km/h").toString()

                        txt_sunset?.text = Common().convertUnixToHour(
                            weatherResult.sys?.sunset!!.toLong()
                        )
                        txt_geo_cord?.text = java.lang.StringBuilder("[")
                            .append(weatherResult.coord.toString())
                            .append("]").toString()
                        weather_panel!!.visibility = View.VISIBLE
                        loading!!.visibility = View.GONE
                    }, { throwable ->
                        loading?.visibility = View.GONE
                        Snackbar.make(
                            requireActivity().findViewById(android.R.id.content),
                            throwable.message!!, Snackbar.LENGTH_LONG
                        ).show()
                    })
            )
        }
    }

    private fun getForecastWeatherInformation() {

        if (dbLat.isNotEmpty() && dbLng.isNotEmpty()) {
            compositeDisposable!!.add(
                mService!!.getForecastWeatherByLatLng(
                    dbLat, dbLng,
                    getString(Common().apiKey),
                    "metric"
                )
                !!.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ weatherForecastResult ->
                        displayForecastWeather(
                            weatherForecastResult!!,
                        )
                    }
                    ) { throwable ->
                        Snackbar.make(
                            requireActivity().findViewById(android.R.id.content),
                            throwable.message!!, Snackbar.LENGTH_LONG
                        ).show()
                    }
            )
        } else if (mCity.isNotEmpty()){
            compositeDisposable!!.add(
                mService!!.getForecastWeatherCity(
                    mCity,
                    getString(Common().apiKey),
                    "metric"
                )
                !!.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ weatherForecastResult ->
                        displayForecastWeather(
                            weatherForecastResult!!,
                        )
                    }
                    ) { throwable ->
                        Snackbar.make(
                            requireActivity().findViewById(android.R.id.content),
                            throwable.message!!, Snackbar.LENGTH_LONG
                        ).show()
                    }
            )
        }

    }

    private fun displayForecastWeather(
        weatherForecastResult: WeatherForecastResult

    ) {
        val adapter = WeatherForecastAdapter(requireContext(), weatherForecastResult)
        val recyclerView = binding.recyclerForecast
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = adapter
    }

}