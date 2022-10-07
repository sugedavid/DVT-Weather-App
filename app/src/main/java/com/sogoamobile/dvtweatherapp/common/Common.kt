package com.sogoamobile.dvtweatherapp.common

import android.content.Context
import com.sogoamobile.dvtweatherapp.R
import com.sogoamobile.dvtweatherapp.data.cities.CitiesTable
import java.text.SimpleDateFormat
import java.util.*

class Common {
    val apiKey = R.string.apiKey
    val baseUrl = "https://api.openweathermap.org/data/2.5/"
    val imageUrl = "https://openweathermap.org/img/w/"

    fun convertUnixToDate(dt: Long): String? {
        val date = Date(dt * 1000L)
        val simpleDateFormat = SimpleDateFormat("EEE, d MMM, yyy")
        return simpleDateFormat.format(date)
    }

    fun convertUnixToHour(dt: Long): String? {
        val date = Date(dt * 1000L)
        val simpleDateFormat = SimpleDateFormat("EE, h:mm a")
        return simpleDateFormat.format(date)
    }

    fun convertUnixToDay(dt: Long): String? {
        val date = Date(dt * 1000L)
        val simpleDateFormat = SimpleDateFormat("EEEE")
        return simpleDateFormat.format(date)
    }

    fun changeBackgroundImage(condition: String): Int {
        return when {
            condition.contains("rain") -> {
                R.drawable.forest_rainy
            }
            condition.contains("cloud") -> {
                R.drawable.forest_cloudy
            }
            condition.contains("sun") -> {
                R.drawable.forest_sunny
            }
            else -> {
                R.drawable.forest_sunny
            }
        }
    }

    fun changeBackgroundColor(condition: String): Int {
        return when {
            condition.contains("rain") -> {
                R.color.color_rainy
            }
            condition.contains("cloud") -> {
                R.color.color_cloudy
            }
            condition.contains("sun") -> {
                R.color.color_sunny
            }
            else -> {
                R.color.color_sunny
            }
        }
    }

    fun changeFavouriteImage(isFavourite: Boolean): Int {
        return if (isFavourite) {
            R.drawable.ic_heart_white
        } else {
            R.drawable.ic_heart_outline
        }
    }

    fun saveLocationID(context: Context, locationID: Int){
        val sharedPreference =  context.getSharedPreferences("CITY_ID", Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        editor.putInt("cityID",locationID)
        editor.apply()
    }

    fun getCityID(context: Context): Int{
        val sharedPreference =  context.getSharedPreferences("CITY_ID", Context.MODE_PRIVATE)
        return sharedPreference.getInt("cityID",3163858)
    }

    fun saveCondition(context: Context, condition: String){
        val sharedPreference =  context.getSharedPreferences("CONDITION", Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        editor.putString("condition",condition)
        editor.apply()
    }

    fun getCondition(context: Context): String {
        val sharedPreference =  context.getSharedPreferences("CONDITION", Context.MODE_PRIVATE)
        return sharedPreference.getString("condition","sun").toString()
    }
}