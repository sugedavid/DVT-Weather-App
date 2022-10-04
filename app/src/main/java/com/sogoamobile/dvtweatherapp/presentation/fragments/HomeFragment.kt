package com.sogoamobile.dvtweatherapp.presentation.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.sogoamobile.dvtweatherapp.R
import com.sogoamobile.dvtweatherapp.adapter.CitiesForecastAdapter
import com.sogoamobile.dvtweatherapp.adapter.WeatherForecastAdapter
import com.sogoamobile.dvtweatherapp.common.Common
import com.sogoamobile.dvtweatherapp.data.cities.CitiesTable
import com.sogoamobile.dvtweatherapp.data.cities.CitiesViewModel
import com.sogoamobile.dvtweatherapp.databinding.FragmentHomeBinding
import com.sogoamobile.dvtweatherapp.model.WeatherForecastResult
import com.sogoamobile.dvtweatherapp.network.IOpenWeatherMap
import com.sogoamobile.dvtweatherapp.network.RetrofitClient
import com.sogoamobile.dvtweatherapp.presentation.fragments.WeatherInfoFragment.Companion.city
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import java.util.*
import kotlin.Boolean
import kotlin.toString


class HomeFragment : Fragment() , SearchView.OnQueryTextListener{

    private var mTxtTemp: TextView? = null
    private var mTxtWeatherDesc: TextView? = null
    private var mTxtCity: TextView? = null
    private var mTxtDateTime: TextView? = null
    private var mCity = ""
    private var loading: ProgressBar? = null
    private var searchView: SearchView? = null
    private lateinit var adapter: CitiesForecastAdapter

    private var dbLat = "0.0"
    private var dbLng = "0.0"

    private var compositeDisposable: CompositeDisposable? = null
    private var mService: IOpenWeatherMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var citiesViewModel: CitiesViewModel

    private var citiesList:List<CitiesTable> = emptyList()

    // current weather info
    var cityId =  0
    var cityName =  ""
    var description = ""
    var refreshTime: Long = 0
    var temperature: Int =  0

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
        citiesViewModel = ViewModelProvider(this).get(CitiesViewModel::class.java)
//        citiesList = citiesViewModel.readAllData.value ?: emptyList()

        citiesViewModel.readAllData.observe(this.viewLifecycleOwner) { cities ->
            Log.d("TAG_cities_list_db", cities.toString())
            citiesList = cities
            adapter.setData(cities)

            // check if db has current weather info
            if(citiesList.isEmpty()){
                // fetch current weather
                getCurrentWeatherInformation()
            }else{
                // display current weather info from db
                citiesViewModel.getCity(Common().getCityID(requireContext())).observe(this.viewLifecycleOwner) { city ->
                    Log.d("TAG_city_db", city.toString())
                    updateViews(city.cityName, city.description, city.temperature, city.refreshTime)
                }
            }
        }

        // recycler view
         adapter =
            CitiesForecastAdapter(requireContext(), compositeDisposable!!, mService!!, Common().getCitiesList(), citiesViewModel)
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
                        "Permission Denied",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }).check()

        mTxtTemp = binding.txtTemp
        mTxtWeatherDesc = binding.txtWeatherDesc
        mTxtCity = binding.txtCurrentLocation
        mTxtDateTime = binding.txtDateTime
        loading = binding.loadingF

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

       //  get 5 day forecast
//        getForecastWeatherInformation()
        return view
    }

    private fun updateViews(cityName: String, description: String, temperature: Int, refreshTime: Long ){

        mTxtCity?.text = cityName
        mCity = mTxtCity?.text.toString()
        mTxtWeatherDesc?.text = description
        mTxtTemp?.text = "$temperature °C"
        //date
        mTxtDateTime!!.text =
            getString(R.string.last_refresh, Common().convertUnixToHour(refreshTime));
        // change background image
        binding.imgBg1.setBackgroundResource(
            Common().changeBackgroundImage(
                description
            )
        )
        // change background color
        binding.constraintLayout.setBackgroundColor(
            Common().changeBackgroundColor(
                description
            )
        )
    }


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

                    updateViews(cityName, description, temperature, refreshTime)

                    // save weather info to db
                    citiesViewModel.addCities(CitiesTable(id = cityId, cityName = cityName, description = description, refreshTime = refreshTime, temperature = temperature, isFavourite = false))
                    // save locationID to preference
                    Common().saveLocationID(requireContext(), cityId)

//                    mTxtCity?.text = weatherResult.name
//                    mCity = mTxtCity?.text.toString()
//                    mTxtWeatherDesc?.text = weatherResult.weather?.get(0)?.description
//                    mTxtTemp?.text =
//                        StringBuilder((weatherResult.main?.temp?.toInt()!!))
//                            .append("°C").toString()
//                    //date
//                    mTxtDateTime!!.text =
//                        getString(R.string.last_refresh, Common().convertUnixToHour(weatherResult?.dt!!.toLong()));
//                    // change background image
//                    binding.imgBg1.setBackgroundResource(
//                        Common().changeBackgroundImage(
//                            weatherResult.weather?.get(0)?.description!!
//                        )
//                    )
//                    // change background color
//                    binding.constraintLayout.setBackgroundColor(
//                        Common().changeBackgroundColor(
//                            weatherResult.weather?.get(0)?.description!!
//                        )
//                    )
                    loading?.visibility = View.INVISIBLE
                }, { throwable ->
                    loading?.visibility = View.INVISIBLE
                    Snackbar.make(
                        requireActivity().findViewById(android.R.id.content),
                        throwable.message!!, Snackbar.LENGTH_LONG
                    ).show()
                })
        )
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
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    override fun onQueryTextSubmit(query: kotlin.String?): Boolean {
        adapter.filter.filter(query)
        return false
    }

    override fun onQueryTextChange(newText: kotlin.String?): Boolean {
        adapter.filter.filter(newText)
        return false
    }

}