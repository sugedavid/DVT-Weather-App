package com.sogoamobile.dvtweatherapp.presentation.fragments

import android.Manifest
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.snackbar.Snackbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.sogoamobile.dvtweatherapp.R
import com.sogoamobile.dvtweatherapp.common.Common
import com.sogoamobile.dvtweatherapp.data.location.LocationTable
import com.sogoamobile.dvtweatherapp.data.location.LocationViewModel
import com.sogoamobile.dvtweatherapp.data.locationForecast.LocationForecastTable
import com.sogoamobile.dvtweatherapp.data.locationForecast.LocationForecastViewModel
import com.sogoamobile.dvtweatherapp.databinding.FragmentHomeBinding
import com.sogoamobile.dvtweatherapp.network.IOpenWeatherMap
import com.sogoamobile.dvtweatherapp.network.RetrofitClient
import com.sogoamobile.dvtweatherapp.presentation.adapter.WeatherForecastAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.nav_header.view.*
import retrofit2.Retrofit
import java.util.*


class HomeFragment : Fragment() {

    // current location lat & lng
    private var dbLat = "0.0"
    private var dbLng = "0.0"

    // rxjava disposable
    private var compositeDisposable: CompositeDisposable? = null

    // retrofit
    private var mService: IOpenWeatherMap? = null

    // location client
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // binding
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // view models
    private lateinit var locationViewModel: LocationViewModel
    private lateinit var locationForecastViewModel: LocationForecastViewModel

    // db entries list
    private var citiesList: List<LocationTable> = emptyList()
    private var cityForecastList: List<LocationForecastTable> = emptyList()

    // current weather info
    var cityId = 0
    var cityName = ""
    var description = ""
    var refreshTime: Long = 0
    var temperature: Int = 0
    var temperatureMax: Int = 0
    var temperatureMin: Int = 0

    // forecast info
    var forecastImage = ""
    var forecastDay: Long = 0
    var forecastTemperature: Int = 0

    // actionbar drawer
    lateinit var actionBarDrawerToggle: ActionBarDrawerToggle

    // places autocomplete code
    private val autocompleteRequestCode = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        compositeDisposable = CompositeDisposable()
        val retrofit: Retrofit? = RetrofitClient.instance
        mService = retrofit?.create(IOpenWeatherMap::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        // drawer setup
        actionBarDrawerToggle = ActionBarDrawerToggle(
            requireActivity(),
            binding.drawerLayout,
            R.string.nav_open,
            R.string.nav_close
        )
        binding.drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        binding.layoutHomeAppbar.imgDrawerMenu.setOnClickListener {
            if (binding.drawerLayout.isDrawerVisible(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                binding.drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        binding.navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_favourites -> {
                    val action = HomeFragmentDirections.actionHomeFragmentToFavouritesFragment()
                    findNavController().navigate(action)
                }
            }
            true
        }

        //CitiesViewModel
        locationViewModel = ViewModelProvider(this)[LocationViewModel::class.java]
        //CityForecastViewModel
        locationForecastViewModel = ViewModelProvider(this)[LocationForecastViewModel::class.java]

        // current weather observable
        locationViewModel.readAllData.observe(this.viewLifecycleOwner) { cities ->
            citiesList = cities

            // check if db has current weather info
            if (citiesList.isEmpty()) {
                // fetch current weather
                getCurrentWeatherInformation()
            } else {
                // display current weather info from db
                locationViewModel.getCity(Common().getLocationID(requireContext()))
                    .observe(this.viewLifecycleOwner) { city ->
                        updateViews(
                            city.id,
                            city.cityName,
                            city.description,
                            city.temperature,
                            city.temperatureMin,
                            city.temperatureMax,
                            city.refreshTime,
                            city.isFavourite
                        )
                    }
            }
        }

        // weather forecast observable
        locationForecastViewModel.readCityForecast.observe(this.viewLifecycleOwner) { cityForecast ->
            Log.d("TAG_city_forecast_list", cityForecast.toString())
            cityForecastList = cityForecast

            // check if db has forecast weather info
            if (cityForecastList.isEmpty()) {
                //  fetch 5 day forecast
                getForecastWeatherInformation()
            } else {
                // load forecast weather info from db to recyclerview
                val adapter = WeatherForecastAdapter(requireContext(), cityForecast)
                val recyclerView = binding.recyclerForecast
                recyclerView.layoutManager = LinearLayoutManager(requireContext())
                recyclerView.adapter = adapter
            }
        }

        // check location permissions
        checkLocationPermission()


        // Google Places
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), getString(R.string.places_apiKey), Locale.US);
        }
        // Set the fields to specify which types of place data to
        // return after the user has made a selection.
        val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)

        binding.layoutHomeAppbar.imgSearchTab.setOnClickListener {
            // Start the autocomplete intent.
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .build(requireContext())
            startActivityForResult(intent, autocompleteRequestCode)
        }

        return view
    }

    // checks for location permissions
    fun checkLocationPermission() {
        Dexter.withActivity(activity)
            .withPermissions(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        if (ActivityCompat.checkSelfPermission(
                                requireContext(),
                                Manifest.permission.ACCESS_FINE_LOCATION
                            )
                            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                                requireContext(),
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            return
                        }

                        fusedLocationClient =
                            LocationServices.getFusedLocationProviderClient(requireContext())
                        fusedLocationClient.lastLocation
                            .addOnSuccessListener { location: Location? ->
                                // Got last known location. In some rare situations this can be null.
                                dbLat = location?.latitude.toString()
                                dbLng = location?.longitude.toString()
                            }
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest>,
                    token: PermissionToken
                ) {
                    Snackbar.make(
                        activity!!.findViewById(android.R.id.content),
                        context!!.getString(R.string.permission_denied),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }).check()

    }

    // updates the ui with data from db
    private fun updateViews(
        cityID: Int, cityName: String, description: String, temperature: Int, temperatureMin: Int,
        temperatureMax: Int, refreshTime: Long, isFavourite: Boolean
    ) {

        Common().saveCondition(requireContext(), description)
        // city name
        binding.txtCurrentLocation.text = cityName
        // weather description
        binding.txtWeatherDesc.text = description
        // current temperature
        binding.txtTemp.text = "$temperature °"
        binding.txtTempCurrent.text = "$temperature °\nCurrent"
        // min temperature
        binding.txtTempMin.text = "$temperatureMin °\nmin"
        // max temperature
        binding.txtTempMax.text = "$temperatureMax °\nmax"
        //date
        binding.txtDateTime.text =
            getString(R.string.last_refresh, Common().convertUnixToHour(refreshTime))
        // change background image
        binding.imgBg1.setBackgroundResource(
            Common().changeBackgroundImage(description)
        )
        // change background color
        binding.constraintLayout.setBackgroundResource(
            Common().changeBackgroundColor(description)
        )
        binding.navView.constraint_nav_header?.setBackgroundResource(
            Common().changeBackgroundColor(description)
        )
        // favourite city
        binding.layoutHomeAppbar.imgWeatherFav.setImageResource(
            when {
                isFavourite -> {
                    R.drawable.ic_heart_white
                }
                else -> {
                    R.drawable.ic_heart_outline
                }
            }
        )
        binding.layoutHomeAppbar.imgWeatherFav.setOnClickListener {
            // save weatherForecastResult to db
            locationViewModel.updateCity(
                LocationTable(
                    id = cityID,
                    cityName = cityName,
                    description = description,
                    refreshTime = refreshTime,
                    temperature = temperature,
                    temperatureMin = temperatureMin,
                    temperatureMax = temperatureMax,
                    isFavourite = !isFavourite,
                    latitude = dbLat,
                    longitude = dbLng,

                )
            )

            if (!isFavourite) Toast.makeText(
                requireContext(),
                "$cityName added to your favourites",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // fetch current weather info
    private fun getCurrentWeatherInformation() {
        binding.loadingF.visibility = View.VISIBLE
        compositeDisposable?.add(
            mService!!.getWeatherByLatLng(
                dbLat, dbLng,
                getString(Common().apiKey),
                "metric"
            )
            !!.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ weatherResult ->
                    cityId = weatherResult?.id ?: 0
                    cityName = weatherResult?.name ?: ""
                    description = weatherResult?.weather?.get(0)?.description ?: ""
                    refreshTime = weatherResult?.dt!!.toLong()
                    temperature = weatherResult.main?.temp?.toInt() ?: 0
                    temperatureMin = weatherResult.main?.temp_min?.toInt() ?: 0
                    temperatureMax = weatherResult.main?.temp_max?.toInt() ?: 0

                    // save weather info to db
                    locationViewModel.addCities(
                        LocationTable(
                            id = cityId,
                            cityName = cityName,
                            description = description,
                            refreshTime = refreshTime,
                            temperature = temperature,
                            temperatureMin = temperatureMin,
                            temperatureMax = temperatureMax,
                            isFavourite = false,
                            latitude = dbLat,
                            longitude = dbLng,
                        )
                    )
                    // save locationID & condition to preference
                    Common().saveLocationID(requireContext(), cityId)

                    binding.loadingF.visibility = View.GONE
                }, { throwable ->
                    binding.loadingF.visibility = View.GONE
                    Snackbar.make(
                        requireActivity().findViewById(android.R.id.content),
                        throwable.message ?: "", Snackbar.LENGTH_LONG
                    ).show()
                })
        )
    }

    // fetch forecast info
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

                        for (result in weatherForecastResult?.list!!) {
                            forecastImage = result.weather?.get(0)?.icon ?: ""
                            forecastDay = result.dt
                            forecastTemperature = result.main?.temp?.toInt() ?: 0

                            // save weatherForecastResult to db
                            locationForecastViewModel.addCityForecast(
                                LocationForecastTable(
                                    id = 0, day = forecastDay, imageIcon = forecastImage,
                                    temperature = forecastTemperature
                                )
                            )
                        }
                    }
                    ) { throwable ->
                        Snackbar.make(
                            requireActivity().findViewById(android.R.id.content),
                            throwable.message ?: "", Snackbar.LENGTH_LONG
                        ).show()
                    }
            )
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == autocompleteRequestCode) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    data?.let {
                        val place = Autocomplete.getPlaceFromIntent(data)
                        Log.i(TAG, "Place: ${place.name}, ${place.id}, ${place.latLng}")

                        dbLat = place.latLng?.latitude.toString()
                        dbLng = place.latLng?.longitude.toString()
                        // fetch weather info for new location
                        getCurrentWeatherInformation()
                        getForecastWeatherInformation()
                    }
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    data?.let {
                        val status = Autocomplete.getStatusFromIntent(data)
                        Toast.makeText(
                            requireContext(),
                            "An error occurred: ${status.statusMessage}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                Activity.RESULT_CANCELED -> {
                    // The user canceled the operation.
                }
            }
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

}