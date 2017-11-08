package com.kotlin.weather.gson

import com.google.gson.annotations.SerializedName

/**
 * Created by Ming.Xiao on 2017/11/8.
 */
class  Basic{

    @SerializedName("city")
    var cityNmae :String = ""

    @SerializedName("id")
    var weatherId: String = ""

    var update:Update ?=null

    public class Update{

        @SerializedName("loc")
        var updateTime: String = ""
    }
}