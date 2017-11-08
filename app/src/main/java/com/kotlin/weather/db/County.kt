package com.kotlin.weather.db

import org.litepal.crud.DataSupport

data class County(var countyName:String,var weatherId:String,var cityId:Int) : DataSupport()