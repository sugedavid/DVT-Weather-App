package com.sogoamobile.dvtweatherapp.presentation.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.sogoamobile.dvtweatherapp.R
import com.sogoamobile.dvtweatherapp.common.Common
import com.sogoamobile.dvtweatherapp.data.cities.CitiesViewModel
import com.sogoamobile.dvtweatherapp.data.cityforecast.CityForecastViewModel
import com.sogoamobile.dvtweatherapp.databinding.FragmentFavouritesBinding
import com.sogoamobile.dvtweatherapp.databinding.FragmentHomeBinding
import com.sogoamobile.dvtweatherapp.presentation.adapter.FavouritesAdapter
import com.sogoamobile.dvtweatherapp.presentation.adapter.WeatherForecastAdapter

class FavouritesFragment : Fragment() {

    // view model
    private lateinit var citiesViewModel: CitiesViewModel
    // view binding
    private var _binding: FragmentFavouritesBinding? = null
    private val binding get() = _binding!!

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
        _binding = FragmentFavouritesBinding.inflate(inflater, container, false)
        val view = binding.root

        //CitiesViewModel
        citiesViewModel = ViewModelProvider(this)[CitiesViewModel::class.java]
        citiesViewModel.readAllData.observe(this.viewLifecycleOwner) { city ->

            // check if db has forecast weather info
            if((city.isEmpty() || city.none { it.isFavourite })){
                //  empty state
                binding.txtNoFavourites.visibility = View.VISIBLE
            }else{
                binding.txtNoFavourites.visibility = View.GONE
                // load forecast weather info from db to recyclerview
                val adapter = FavouritesAdapter(requireContext(), city)
                val recyclerView = binding.recyclerForecast
                recyclerView.layoutManager = LinearLayoutManager(requireContext())
                recyclerView.adapter = adapter
            }
        }

        //home button
        binding.imgHomeMenu.setOnClickListener {
            val action = FavouritesFragmentDirections.actionFavouritesFragmentToHomeFragment()
            findNavController().navigate(action)
        }

        // change background color
        binding.constraintLayout.setBackgroundResource(
            Common().changeBackgroundColor(Common().getCondition(requireContext()))
        )

        return view
    }
}