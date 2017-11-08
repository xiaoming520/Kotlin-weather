package com.kotlin.weather.gson

import com.google.gson.annotations.SerializedName

class Now {

    @SerializedName("tmp")
    var temperature: String = ""

    @SerializedName("cond")
    var more: More? = null

    inner class More {

        @SerializedName("txt")
        var info: String = ""

    }

}
