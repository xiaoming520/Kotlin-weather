package com.kotlin.weather.gson

import com.google.gson.annotations.SerializedName

class Forecast {

    var date: String = ""

    @SerializedName("tmp")
    var temperature: Temperature? = null

    @SerializedName("cond")
    var more: More? = null

    inner class Temperature {

        var max: String = ""

        var min: String = ""

    }

    inner class More {

        @SerializedName("txt_d")
        var info: String = ""

    }

}
