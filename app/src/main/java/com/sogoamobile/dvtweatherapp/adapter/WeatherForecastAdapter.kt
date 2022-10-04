package com.sogoamobile.dvtweatherapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.sogoamobile.dvtweatherapp.R
import com.sogoamobile.dvtweatherapp.common.Common
import com.sogoamobile.dvtweatherapp.model.WeatherForecastResult
import com.squareup.picasso.Picasso


class WeatherForecastAdapter(
    var context: Context,
    private var weatherForecastResult: WeatherForecastResult
) :
    RecyclerView.Adapter<WeatherForecastAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView: View =
            LayoutInflater.from(context).inflate(R.layout.item_weather_forecast, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val desc: String? = weatherForecastResult.list?.get(position)?.weather?.get(0)?.description

        //Load weather icon
        Picasso.get().load(
            StringBuilder(Common().imageUrl)
                .append(weatherForecastResult.list?.get(position)?.weather?.get(0)?.icon)
                .append(".png").toString()
        ).into(holder.imgWeather)
        val time: String? = weatherForecastResult.list?.get(position)?.dt?.let {
            Common().convertUnixToDay(
                it
            )
        }

        //time
        holder.txtDateTime.text = time
        //temperature
        holder.txtTemperature.text = StringBuilder(
            java.lang.String.valueOf(
                weatherForecastResult.list?.get(
                    position
                )?.main?.temp?.toInt()
            )
        ).append(" °")
    }

    override fun getItemCount(): Int {
//        weatherForecastResult.list!!.size
        return 8
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtDateTime: TextView = itemView.findViewById(R.id.txt_date)
        var txtTemperature: TextView = itemView.findViewById(R.id.txt_temperature)
        var imgWeather: ImageView = itemView.findViewById(R.id.img_weather)
    }
}
