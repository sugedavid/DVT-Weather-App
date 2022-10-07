package com.sogoamobile.dvtweatherapp.presentation.fragments

import android.Manifest
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
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
import com.sogoamobile.dvtweatherapp.presentation.adapter.CitiesForecastAdapter
import com.sogoamobile.dvtweatherapp.presentation.adapter.WeatherForecastAdapter
import com.sogoamobile.dvtweatherapp.common.Common
import com.sogoamobile.dvtweatherapp.data.cities.CitiesTable
import com.sogoamobile.dvtweatherapp.data.cities.CitiesViewModel
import com.sogoamobile.dvtweatherapp.data.cityforecast.CityForecastTable
import com.sogoamobile.dvtweatherapp.data.cityforecast.CityForecastViewModel
import com.sogoamobile.dvtweatherapp.databinding.FragmentHomeBinding
import com.sogoamobile.dvtweatherapp.network.IOpenWeatherMap
import com.sogoamobile.dvtweatherapp.network.RetrofitClient
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import java.util.*
import kotlin.Boolean
import kotlin.toString


class HomeFragment : Fragment() {

    private var mTxtTempCurrent: TextView? = null
    private var mTxtTempMin: TextView? = null
    private var mTxtTempMax: TextView? = null
    private var mTxtWeatherDesc: TextView? = null
    private var mTxtCity: TextView? = null
    private var mTxtDateTime: TextView? = null
    private var mCity = ""
    private var loading: ProgressBar? = null
    private var mImgFav: ImageButton? = null
    private lateinit var adapter: CitiesForecastAdapter

    private var dbLat = "0.0"
    private var dbLng = "0.0"

    private var compositeDisposable: CompositeDisposable? = null
    private var mService: IOpenWeatherMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var citiesViewModel: CitiesViewModel
    private lateinit var cityForecastViewModel: CityForecastViewModel

    private var citiesList:List<CitiesTable> = emptyList()
    private var cityForecastList:List<CityForecastTable> = emptyList()

    // current weather info
    var cityId =  0
    var cityName =  ""
    var description = ""
    var refreshTime: Long = 0
    var temperature: Int =  0
    var temperatureMax: Int =  0
    var temperatureMin: Int =  0
    var isFavourite: Boolean = false

    // forecast info
    var forecastId = 0
    var forecastImage = ""
    var forecastDay: Long = 0
    var forecastTemperature: Int =  0

    lateinit var drawerLayout: DrawerLayout
    lateinit var actionBarDrawerToggle: ActionBarDrawerToggle

    private val AUTOCOMPLETE_REQUEST_CODE = 1

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

        // search
//        searchView = binding.layoutHomeAppbar.searchView
//        searchView?.setOnQueryTextListener(this)

        //CitiesViewModel
        citiesViewModel = ViewModelProvider(this)[CitiesViewModel::class.java]
        //CityForecastViewModel
        cityForecastViewModel = ViewModelProvider(this)[CityForecastViewModel::class.java]

        // current weather observable
        citiesViewModel.readAllData.observe(this.viewLifecycleOwner) { cities ->
            Log.d("TAG_cities_list_db", cities.toString())
            citiesList = cities
//            adapter.setData(cities)

            // check if db has current weather info
            if(citiesList.isEmpty()){
                // fetch current weather
                getCurrentWeatherInformation()
            }else{
                // display current weather info from db
                citiesViewModel.getCity(Common().getCityID(requireContext())).observe(this.viewLifecycleOwner) { city ->
                    updateViews(city.id, city.cityName, city.description, city.temperature, city.temperatureMin,
                        city.temperatureMax, city.refreshTime, city.isFavourite)
                }
            }
        }

        // weather forecast observable
        cityForecastViewModel.readCityForecast.observe(this.viewLifecycleOwner) { cityForecast ->
            Log.d("TAG_city_forecast_list", cityForecast.toString())
            cityForecastList = cityForecast

            // check if db has forecast weather info
            if(cityForecastList.isEmpty()){
                //  fetch 5 day forecast
                getForecastWeatherInformation()
            }else{
                // load forecast weather info from db to recyclerview
                val adapter = WeatherForecastAdapter(requireContext(), cityForecast)
                val recyclerView = binding.recyclerForecast
                recyclerView.layoutManager = LinearLayoutManager(requireContext())
                recyclerView.adapter = adapter
            }
        }

        // recycler view
//         adapter =
//            CitiesForecastAdapter(requireContext(), compositeDisposable!!, mService!!, Common().getCitiesList(), citiesViewModel)
//        val recyclerView = binding.recyclerCitiesForecast
//        recyclerView.layoutManager = LinearLayoutManager(requireContext())
//        recyclerView.adapter = adapter

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
                        getString(R.string.permission_denied),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }).check()

        mTxtTempCurrent = binding.txtTempCurrent
        mTxtTempMin = binding.txtTempMin
        mTxtTempMax = binding.txtTempMax
        mTxtWeatherDesc = binding.txtWeatherDesc
        mTxtCity = binding.txtCurrentLocation
        mTxtDateTime = binding.txtDateTime
        loading = binding.loadingF
        mImgFav = binding.layoutHomeAppbar.imgWeatherFav
        drawerLayout = binding.drawerLayout

        // drawer setup
        actionBarDrawerToggle = ActionBarDrawerToggle(requireActivity(), drawerLayout, R.string.nav_open, R.string.nav_close)
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        binding.layoutHomeAppbar.imgDrawerMenu.setOnClickListener {
            if (drawerLayout.isDrawerVisible(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
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
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
        }

        // to make the Navigation drawer icon always appear on the action bar
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)

//        cdvFav?.setOnClickListener {
//            val action = HomeFragmentDirections.actionHomeFragmentToWeatherFragment(
//                lat = dbLat,
//                long = dbLng
//            )
//            findNavController().navigate(action)
//        }

//        val weatherInfoTab = binding.layoutHomeAppbar.imgWeatherInfoTab
//        weatherInfoTab.setOnClickListener {
//            val action = HomeFragmentDirections.actionHomeFragmentToWeatherFragment(
//                lat = dbLat,
//                long = dbLng
//            )
//            findNavController().navigate(action)
//        }

        //search tab
//        val searchTab = binding.layoutHomeAppbar.imgSearchTab
//        val searchView = binding.layoutHomeAppbar.searchView
//        val cdvSearchView = binding.layoutHomeAppbar.cdvSearchView
//        searchTab.setOnClickListener {
//            searchTab.visibility = View.GONE
//            cdvSearchView.visibility = View.VISIBLE
//        }

        return view
    }

    // updates the ui with data from db
    private fun updateViews(cityID: Int, cityName: String, description: String, temperature: Int,temperatureMin: Int,
                            temperatureMax: Int, refreshTime: Long, isFavourite: Boolean){

        // city name
        mTxtCity?.text = cityName
        mCity = mTxtCity?.text.toString()
        // weather description
        mTxtWeatherDesc?.text = description
        // current temperature
        binding.txtTemp.text = "$temperature °"
        mTxtTempCurrent?.text = "$temperature °\nCurrent"
        // min temperature
        mTxtTempMin?.text = "$temperatureMin °\nmin"
        // max temperature
        mTxtTempMax?.text = "$temperatureMax °\nmax"
        //date
        mTxtDateTime!!.text = getString(R.string.last_refresh, Common().convertUnixToHour(refreshTime))
        // change background image
        binding.imgBg1.setBackgroundResource(
            Common().changeBackgroundImage(description)
        )
        // change background color
        binding.constraintLayout.setBackgroundResource(
            Common().changeBackgroundColor(description)
        )
        // favourite city
        mImgFav?.setImageResource(
            when {
                isFavourite -> {
                    R.drawable.ic_heart_white
                }
                else -> {
                    R.drawable.ic_heart_outline
                }
            }
        )
        mImgFav?.setOnClickListener{
            // save weatherForecastResult to db
            citiesViewModel.updateCity(CitiesTable(id = cityID, cityName = cityName,
                description = description, refreshTime = refreshTime, temperature = temperature,
                temperatureMin = temperatureMin,temperatureMax = temperatureMax, isFavourite = !isFavourite))

            if(!isFavourite) Toast.makeText(requireContext(), "$cityName added to your favourites", Toast.LENGTH_SHORT).show()
        }
    }

    // fetch current weather info
    private fun getCurrentWeatherInformation() {
        loading?.visibility = View.VISIBLE
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
                    citiesViewModel.addCities(CitiesTable(id = cityId, cityName = cityName,
                        description = description, refreshTime = refreshTime, temperature = temperature,
                        temperatureMin = temperatureMin,temperatureMax = temperatureMax, isFavourite = false))
                    // save locationID & condition to preference
                    Common().saveLocationID(requireContext(), cityId)
                    Common().saveCondition(requireContext(), description)

                    loading?.visibility = View.GONE
                }, { throwable ->
                    loading?.visibility = View.GONE
                    Snackbar.make(
                        requireActivity().findViewById(android.R.id.content),
                        throwable.message!!, Snackbar.LENGTH_LONG
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

                        for (result in weatherForecastResult?.list!!){
                            forecastImage = result.weather?.get(0)?.icon ?: ""
                            forecastDay = result.dt
                            forecastTemperature = result.main?.temp?.toInt() ?: 0

                            // save weatherForecastResult to db
                            cityForecastViewModel.addCityForecast(CityForecastTable(id = 0, day = forecastDay,imageIcon = forecastImage,
                                temperature = forecastTemperature  ))
                        }
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

                        for (result in weatherForecastResult?.list!!){
                            forecastId = weatherForecastResult.id
                            forecastImage = result.weather?.get(0)?.icon ?: ""
                            forecastDay = result.dt
                            forecastTemperature = result.main?.temp?.toInt() ?: 0

                            // save weatherForecastResult to db
                            cityForecastViewModel.addCityForecast(CityForecastTable(id = forecastId, day = forecastDay,imageIcon = forecastImage,
                                temperature = forecastTemperature  ))
                        }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
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
                        Toast.makeText(requireContext(), "An error occured: ${status.statusMessage}", Toast.LENGTH_SHORT).show()
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