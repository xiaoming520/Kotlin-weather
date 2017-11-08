package com.kotlin.weather.ui

import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import com.kotlin.weather.R
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.annotation.RequiresApi
import android.support.v4.view.GravityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.kotlin.weather.gson.Forecast
import com.kotlin.weather.gson.Weather
import com.kotlin.weather.util.HttpUtil
import com.kotlin.weather.util.Utility
import kotlinx.android.synthetic.main.title.*
import kotlinx.android.synthetic.main.now.*
import kotlinx.android.synthetic.main.suggestion.*
import kotlinx.android.synthetic.main.aqi.*
import kotlinx.android.synthetic.main.forecast_item.*
import kotlinx.android.synthetic.main.weather_main.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

/**
 * Created by Ming.Xiao on 2017/11/8.
 */

class WeatherActivity : AppCompatActivity(){

    private var mWeatherId: String? = null

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= 21) {
            val decorView = window.decorView
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            window.statusBarColor = Color.TRANSPARENT
        }
        setContentView(R.layout.weather_main)

        //查询是否有缓存天气
        val prefs = PreferenceManager.getDefaultSharedPreferences(this) as SharedPreferences
        val weatherString = prefs.getString("weather",null)
        if(weatherString!=null){
            val weather = Utility.handleWeatherResponse(weatherString) as Weather
            mWeatherId = weather.basic!!.weatherId
            showWeatherInfo(weather)
        }else{
            // 无缓存时去服务器查询天气
            mWeatherId = intent.getStringExtra("weather_id")
            weather_layout.visibility = View.INVISIBLE
            requestWeather(mWeatherId!!)
        }

        swipe_refresh.setOnRefreshListener {
            requestWeather( mWeatherId!! )
        }

        nav_button.setOnClickListener {
            drawer_layout.openDrawer(GravityCompat.START)
        }


      val bingPic = prefs.getString("bing_pic", null)
        if (bingPic != null) {
            Glide.with(this).load(bingPic).into(bing_pic_img)
        } else {
            loadBingPic()
        }
    }


    override fun onResume() {
        super.onResume()

    }

    fun showWeatherInfo(weather: Weather){

        title_city.text = weather.basic!!.cityNmae
        title_update_time.text = weather.basic!!.update!!.updateTime

        degree_text.text = weather.now!!.temperature + "℃";
        weather_info_text.text = weather.now!!.more!!.info

        val forecastLayout = findViewById(R.id.forecast_layout) as LinearLayout
        forecastLayout.removeAllViews()

        val datalist = weather.forecastList as List<Forecast>

        if(datalist!=null){
            for (forecast in datalist){
                val view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false) as LinearLayout

                val dateText:TextView = view.findViewById(R.id.date_text)
                val infoText:TextView = view.findViewById(R.id.info_text)
                val maxText:TextView= view.findViewById(R.id.max_text)
                val minText:TextView =view. findViewById(R.id.min_text)

                dateText.text = forecast.date
                infoText.text = forecast.more!!.info
                maxText.text = forecast.temperature!!.max
                minText.text = forecast.temperature!!.min
                forecastLayout.addView(view)
            }
        }

        if (weather.aqi != null) {
            aqi_text.text = weather.aqi!!.city!!.aqi
            pm_text.text = weather.aqi!!.city!!.pm25
        }
        val comfort = "舒适度：" + weather.suggestion!!.comfort!!.info
        val carWash = "洗车指数：" + weather.suggestion!!.carWash!!.info
        val sport = "运行建议：" + weather.suggestion!!.sport!!.info
        comfort_text.text = comfort
        car_wash_text.text = carWash
        sport_text.text = sport
        weather_layout.visibility = View.VISIBLE
    }

   public  fun  requestWeather(weatherId:String){
        val weatherUrl = "http://guolin.tech/api/weather?cityid=$weatherId&key=bc0418b57b2d4918819d3974ac1285d9"
        HttpUtil.sendOkHttpRequest(weatherUrl,object :Callback{
            override fun onFailure(call: Call?, e: IOException?) {
                runOnUiThread {
                    Toast.makeText(this@WeatherActivity, "获取天气信息失败", Toast.LENGTH_SHORT).show()
                    swipe_refresh.setRefreshing(false)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseText = response.body().string()
                val weather = Utility.handleWeatherResponse(responseText) as Weather
                Log.w("lavaTest","weatherId " +weatherId +" weather status " +weather!!.status);
                Log.w("lavaTest","Weather list *** " +weather.forecastList)
                runOnUiThread {
                    if(weather!=null&&"ok".equals(weather.status)){
                           val editor = PreferenceManager.getDefaultSharedPreferences(this@WeatherActivity).edit()
                           editor.putString("weather",responseText)
                           editor.apply()
                           mWeatherId = weather.basic!!.weatherId
                           showWeatherInfo(weather)

                    }else{
                          Toast.makeText(this@WeatherActivity, "获取天气信息失败", Toast.LENGTH_SHORT).show()
                    }
                    swipe_refresh.setRefreshing(false)
                }
            }
        })
       loadBingPic()
    }

    fun loadBingPic(){
        val requestBingPic = "http://guolin.tech/api/bing_pic"
        HttpUtil.sendOkHttpRequest(requestBingPic,object :Callback{
            override fun onFailure(call: Call?, e: IOException?) {
                runOnUiThread {
                    bing_pic_img.setImageResource(R.drawable.bg)
                }
            }
            override fun onResponse(call: Call, response: Response) {
                val bingPic = response.body().string()
                val editor = PreferenceManager.getDefaultSharedPreferences(this@WeatherActivity).edit()
                editor.putString("bing_pic", bingPic)
                editor.apply()
                runOnUiThread {
                    Glide.with(this@WeatherActivity).load(bingPic).into(bing_pic_img)
                }
            }

        })
    }


}
