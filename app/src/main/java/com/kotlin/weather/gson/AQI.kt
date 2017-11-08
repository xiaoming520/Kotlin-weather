package com.kotlin.weather.gson

class AQI {

    var city: AQICity? = null

    inner class AQICity {

        var aqi: String = ""

        var pm25: String = ""

    }

}
