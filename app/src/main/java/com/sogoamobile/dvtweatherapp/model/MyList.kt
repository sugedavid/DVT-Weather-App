package com.sogoamobile.dvtweatherapp.model

class MyList {
    var dt:Long = 0
    var main: Main? = null
    var weather: List<Weather>? = null
    var clouds: Clouds? = null
    var wind: Wind? = null
    var rain: Rain? = null
    var sys: Sys? = null
    var dt_txt: String? = null
    var temp: Double = 0.0
}