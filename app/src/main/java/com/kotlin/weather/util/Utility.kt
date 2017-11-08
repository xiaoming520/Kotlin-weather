package com.kotlin.weather.util

import android.text.TextUtils

import com.google.gson.Gson
import com.kotlin.weather.db.City
import com.kotlin.weather.db.County
import com.kotlin.weather.db.Province
import com.kotlin.weather.gson.Weather

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

object Utility {

    /**
     * 解析和处理服务器返回的省级数据
     */
    fun handleProvinceResponse(response: String): Boolean {
        if (!TextUtils.isEmpty(response)) {
            try {
                val allProvinces = JSONArray(response)
                for (i in 0..allProvinces.length() - 1) {
                    val provinceObject = allProvinces.getJSONObject(i)
                    val name = provinceObject.getString("name")
                    val id = provinceObject.getInt("id")
                    val code = provinceObject.getInt("id");//provinceObject.getInt("code")
                    val province = Province(id,name,code)
                    province.save()
                }
                return true
            } catch (e: JSONException) {
                e.printStackTrace()
            }

        }
        return false
    }

    /**
     * 解析和处理服务器返回的市级数据
     */
    fun handleCityResponse(response: String, provinceId: Int): Boolean {
        if (!TextUtils.isEmpty(response)) {
            try {
                val allCities = JSONArray(response)
                for (i in 0..allCities.length() - 1) {
                    val cityObject = allCities.getJSONObject(i)
                    val city = City(cityObject.getInt("id"),cityObject.getString("name"),cityObject.getInt("id"),provinceId)
                    city.save()
                }
                return true
            } catch (e: JSONException) {
                e.printStackTrace()
            }

        }
        return false
    }

    /**
     * 解析和处理服务器返回的县级数据
     */
    fun handleCountyResponse(response: String, cityId: Int): Boolean {
        if (!TextUtils.isEmpty(response)) {
            try {
                val allCounties = JSONArray(response)
                for (i in 0..allCounties.length() - 1) {
                    val countyObject = allCounties.getJSONObject(i)
                    val county = County(countyObject.getString("name"),countyObject.getString("weather_id"),cityId)
                    county.save()
                }
                return true
            } catch (e: JSONException) {
                e.printStackTrace()
            }

        }
        return false
    }

   /* *
     * 将返回的JSON数据解析成Weather实体类
     */
    fun handleWeatherResponse(response: String): Weather? {
        try {
            val jsonObject = JSONObject(response)
            val jsonArray = jsonObject.getJSONArray("HeWeather")
            val weatherContent = jsonArray.getJSONObject(0).toString()
            return Gson().fromJson<Weather>(weatherContent, Weather::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

}
