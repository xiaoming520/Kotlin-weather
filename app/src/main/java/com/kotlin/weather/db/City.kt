package com.kotlin.weather.db

import org.litepal.crud.DataSupport

/**
 * Created by Ming.Xiao on 2017/11/7.
 */
data class City(var id:Int,var cityName:String ,var cityCode:Int,var provinceId:Int) :DataSupport()