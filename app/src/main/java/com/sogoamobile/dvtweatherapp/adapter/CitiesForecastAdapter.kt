package com.sogoamobile.dvtweatherapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.sogoamobile.dvtweatherapp.R
import com.sogoamobile.dvtweatherapp.common.Common
import com.sogoamobile.dvtweatherapp.data.cities.CitiesTable
import com.sogoamobile.dvtweatherapp.data.cities.CitiesViewModel
import com.sogoamobile.dvtweatherapp.presentation.fragments.HomeFragmentDirections
import com.sogoamobile.dvtweatherapp.network.IOpenWeatherMap
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.item_cities_forecast.view.*
import java.util.*
import kotlin.CharSequence
import kotlin.Int
import kotlin.Suppress
import kotlin.let
import kotlin.toString


open class CitiesForecastAdapter(
    var context: Context,
    private var compositeDisposable: CompositeDisposable,
    private var mService: IOpenWeatherMap,
    private var cities: ArrayList<CitiesTable>,
    private var citiesViewModel: CitiesViewModel
) :
    RecyclerView.Adapter<CitiesForecastAdapter.MyViewHolder>(), Filterable {

    private var citiesDB = emptyList<CitiesTable>()
    var citiesListFiltered = ArrayList<CitiesTable>()

    init {
        citiesListFiltered = cities
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView: View =
            LayoutInflater.from(context).inflate(R.layout.item_cities_forecast, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val city = citiesListFiltered[position]

        compositeDisposable.add(
            mService.getForecastWeatherCity(
                city.cityName,
                holder.itemView.context.getString(Common().apiKey),
                "metric"
            )
            !!.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ weatherForecastResult ->

                    //Load weather icon
                    Picasso.get().load(
                        StringBuilder(Common().imageUrl)
                            .append(weatherForecastResult?.list?.get(position)?.weather?.get(0)?.icon)
                            .append(".png").toString()
                    ).into(holder.itemView.imgWeatherIcon)

                    //city name
                    holder.itemView.txtCityName.text = city.cityName

                    //temperature
                    holder.itemView.txtTemperature.text = StringBuilder(
                        java.lang.String.valueOf(
                            weatherForecastResult?.list?.get(
                                position
                            )?.main?.temp?.toInt()
                        )
                    ).append("°C")

                    val time = weatherForecastResult?.list?.get(position)?.dt?.let {
                        (Common().convertUnixToHour(
                            it
                        ))
                    }

                    val date = weatherForecastResult?.list?.get(position)?.dt?.let {
                        (Common().convertUnixToDate(
                            it
                        ))
                    }

                    // date and time
                    holder.itemView.txtDateTime.text = "$date \n\n$time"


                    var isFavouriteDB = false
                    for (mCityDB in citiesDB) {
                        if (mCityDB.cityName == city.cityName && mCityDB.isFavourite) {
                            isFavouriteDB = true
                        }else if (mCityDB.cityName == city.cityName && !mCityDB.isFavourite) {
                            isFavouriteDB = false
                        }
                    }

                    // favourite image
                    holder.itemView.imgFavourite.setBackgroundResource(
                        Common().changeFavouriteImage(isFavouriteDB)
                    )

                    // favourite click
                    holder.itemView.imgFavourite.setOnClickListener {

                        citiesViewModel.addCities(city)

                        // update db
//                        val updatedCity = CitTable(city.id, city.cityName, city.description, city.refreshTime,city.temperature, !city.isFavourite)
                        // add to db
//                        citiesViewModel.updateCities(updatedCity)

                    }

                    holder.itemView.setOnClickListener {
                        // navigate to weather info
                        val action = HomeFragmentDirections.actionHomeFragmentToWeatherFragment(
                            city = city.cityName,
                        )
                        holder.itemView.findNavController().navigate(action)
                    }

                }
                ) { throwable ->
                    Toast.makeText(
                        context, throwable.message!!, Toast.LENGTH_LONG
                    ).show()
                }
        )

    }

    override fun getItemCount(): Int {
        return citiesListFiltered.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    fun setData(citiesListDB: List<CitiesTable>) {
        this.citiesDB = citiesListDB
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                if (charSearch.isEmpty()) {
                    citiesListFiltered = cities
                } else {
                    val resultList = ArrayList<CitiesTable>()
                    for (row in cities) {
                        if (row.cityName.lowercase(Locale.ROOT)
                                .contains(charSearch.lowercase(Locale.ROOT))
                        ) {
                            resultList.add(row)
                        }
                    }
                    citiesListFiltered = resultList
                }
                val filterResults = FilterResults()
                filterResults.values = citiesListFiltered
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                citiesListFiltered = results?.values as ArrayList<CitiesTable>
                notifyDataSetChanged()
            }

        }
    }

}