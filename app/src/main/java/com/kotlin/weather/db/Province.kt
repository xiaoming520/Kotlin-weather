package com.kotlin.weather.db

import org.litepal.crud.DataSupport

/**
 * Created by Ming.Xiao on 2017/11/7.
 */
data class Province(var id:Int,var provinceName:String,var provinceCode:Int):DataSupport()