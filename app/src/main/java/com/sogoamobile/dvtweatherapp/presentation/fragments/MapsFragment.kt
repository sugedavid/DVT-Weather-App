package com.sogoamobile.dvtweatherapp.presentation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.sogoamobile.dvtweatherapp.R
import com.sogoamobile.dvtweatherapp.data.location.LocationTable
import com.sogoamobile.dvtweatherapp.data.location.LocationViewModel
import com.sogoamobile.dvtweatherapp.databinding.FragmentMapsBinding
import com.sogoamobile.dvtweatherapp.presentation.fragments.WeatherInfoFragment.Companion.lat


class MapsFragment : Fragment(), OnMapReadyCallback {

    // view model
    private lateinit var locationViewModel: LocationViewModel
    // view binding
    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!

    private var cityList: List<LocationTable> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMapsBinding.inflate(inflater, container, false)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //back button
        binding.imgBackBtn.setOnClickListener {
            val action = MapsFragmentDirections.actionMapFragmentToFavouritesFragment()
            findNavController().navigate(action)
        }

        return binding.root
    }

    override fun onMapReady(googleMap: GoogleMap) {

        // cities view model
        locationViewModel = ViewModelProvider(this)[LocationViewModel::class.java]
        locationViewModel.readAllData.observe(this.viewLifecycleOwner) { city ->

            // check if db has forecast weather info
            if ((city.isNotEmpty() && !city.none { it.isFavourite })) {

                // loop through data & add markers
                for (item in city) {
                    googleMap.addMarker(
                        MarkerOptions()
                            .position(LatLng(item.latitude.toDouble(), item.longitude.toDouble()))
                            .title("${item.cityName}")
                    )

                }
            }
        }
    }

}