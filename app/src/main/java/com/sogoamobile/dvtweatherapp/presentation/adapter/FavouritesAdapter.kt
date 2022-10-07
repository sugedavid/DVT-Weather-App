package com.sogoamobile.dvtweatherapp.presentation.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.sogoamobile.dvtweatherapp.R
import com.sogoamobile.dvtweatherapp.data.cities.CitiesTable

class FavouritesAdapter(
    var context: Context,
    private var cities: List<CitiesTable>
) :
    RecyclerView.Adapter<FavouritesAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView: View =
            LayoutInflater.from(context).inflate(R.layout.item_favourite_location, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        if(cities[position].isFavourite){
            // location name
            holder.txtLocationName.text = cities[position].cityName
        }
    }

    override fun getItemCount(): Int {
        return cities.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtLocationName: TextView = itemView.findViewById(R.id.txt_location_name)

    }
}
