package com.sogoamobile.dvtweatherapp.presentation.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.sogoamobile.dvtweatherapp.R
import com.sogoamobile.dvtweatherapp.common.Common
import com.sogoamobile.dvtweatherapp.data.location.LocationTable
import com.sogoamobile.dvtweatherapp.presentation.fragments.FavouritesFragmentDirections

class FavouritesAdapter(
    var context: Context,
    private var cities: List<LocationTable>
) :
    RecyclerView.Adapter<FavouritesAdapter.MyViewHolder>() {

    var filteredLocations = cities.filter { it.isFavourite }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView: View =
            LayoutInflater.from(context).inflate(R.layout.item_favourite_location, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        // location name
        holder.txtLocationName.text = filteredLocations[position].cityName
        holder.txtLocationName.setOnClickListener {
            Common().saveLocationID(context, filteredLocations[position].id)
            val action = FavouritesFragmentDirections.actionFavouritesFragmentToHomeFragment()
            holder.itemView.findNavController().navigate(action)
        }
    }

    override fun getItemCount(): Int {
        return filteredLocations.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtLocationName: TextView = itemView.findViewById(R.id.txt_location_name)

    }
}
