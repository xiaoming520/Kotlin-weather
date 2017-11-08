package com.kotlin.weather

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.kotlin.weather.db.City
import com.kotlin.weather.db.County
import com.kotlin.weather.db.Province
import com.kotlin.weather.ui.WeatherActivity
import com.kotlin.weather.util.HttpUtil
import com.kotlin.weather.util.Utility
import kotlinx.android.synthetic.main.choose_city.*
import kotlinx.android.synthetic.main.weather_main.*
import okhttp3.Call
import okhttp3.Response
import org.litepal.crud.DataSupport
import java.io.IOException

/**
 * Created by Ming.Xiao on 2017/11/7.
 */

class ChooseAreaFragment: Fragment(){

    val LEVEL_PROVINCE = 0

    val LEVEL_CITY = 1

    val LEVEL_COUNTY = 2

    var listView:ListView ?=null

    var dataList:ArrayList<String> = ArrayList()

    //省列表
    var provinceList:ArrayList<Province> = ArrayList()

    //市列表
    var cityList:ArrayList<City> = ArrayList()

    //县列表
    var countyList:ArrayList<County> = ArrayList()

    var  progressDialog: ProgressDialog ?= null


    /**
     * 选中的省份
     */
    private var selectedProvince: Province? = null

    /**
     * 选中的城市
     */
    private var selectedCity: City? = null

    /**
     * 当前选中的级别
     */
    private var currentLevel: Int = 0
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.choose_city,container,false)

        listView = view.findViewById(R.id.list_view)

        val  adapter = ArrayAdapter(context,android.R.layout.simple_list_item_1,dataList)
        listView!!.adapter = adapter

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        list_view.setOnItemClickListener(object :AdapterView.OnItemClickListener{

            override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when(currentLevel){
                    LEVEL_PROVINCE->{
                        selectedProvince = provinceList[position]
                        queryCities()
                    }
                    LEVEL_CITY->{
                        selectedCity = cityList[position]
                        queryCounties()
                    }
                    LEVEL_COUNTY->{
                        val weatherId = countyList[position].weatherId
                        if(activity is MainActivity){
                            Intent(activity,WeatherActivity::class.java).apply {
                                putExtra("weather_id", weatherId)
                                startActivity(this)
                                activity.finish()
                            }
                        }else if (activity is WeatherActivity) {
                            val activity = activity as WeatherActivity
                            activity.drawer_layout.closeDrawers()
                            activity.swipe_refresh.setRefreshing(true)
                            activity.requestWeather(weatherId)
                        }
                    }

                }

            }

        })

        back_button.setOnClickListener {
            if (currentLevel == LEVEL_COUNTY) {
                queryCities()
            } else if (currentLevel == LEVEL_CITY) {
                queryProvinces()
            }
        }
        queryProvinces()
    }

    /**
     * 查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器上查询。
     */

    fun queryProvinces(){
       title_text.text = "中国"
       back_button.visibility = View.GONE
        provinceList = (DataSupport.findAll(Province::class.java)) as ArrayList<Province>
        if(provinceList.size>0){
            dataList.clear()
            for (province in provinceList){
                dataList.add(province.provinceName)
            }
            (list_view.adapter as BaseAdapter).notifyDataSetChanged()
            list_view.setSelection(0)
            currentLevel = LEVEL_PROVINCE
        }else{
            val address = "http://guolin.tech/api/china"
            queryFromServer(address, "province")
        }

    }

    /**
     * 根据传入的地址和类型从服务器上查询省市县数据。
     */
    fun queryFromServer(address:String,type:String){
        showProgressDialog()

        HttpUtil.sendOkHttpRequest(address,object :okhttp3.Callback{

            override fun onFailure(call: Call?, e: IOException?) {
                // 通过runOnUiThread()方法回到主线程处理逻辑
                activity.runOnUiThread {
                    closeProgressDialog()
                    Toast.makeText(context, "加载失败", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call?, response: Response?) {
                val responseText = response!!.body().string()
                var result = false
                if ("province" == type) {
                    result = Utility.handleProvinceResponse(responseText)
                } else if ("city" == type) {
                    result = Utility.handleCityResponse(responseText, selectedProvince!!.id)
                } else if ("county" == type) {
                    result = Utility.handleCountyResponse(responseText, selectedCity!!.id)
                }
                if (result) {
                    activity.runOnUiThread {
                        closeProgressDialog()
                        if ("province" == type) {
                            queryProvinces()
                        } else if ("city" == type) {
                           queryCities()
                        } else if ("county" == type) {
                            queryCounties()
                        }
                    }
                }
            }

        })
    }

    /**
     * 查询选中省内所有的市，优先从数据库查询，如果没有查询到再去服务器上查询。
     */
    private fun queryCities() {
        title_text.text = selectedProvince!!.provinceName
        back_button.visibility =View.VISIBLE

        cityList = DataSupport.where("provinceid = ?",java.lang.String.valueOf(selectedProvince!!.id)).find(City::class.java) as ArrayList<City>
        if (cityList.size > 0) {
            dataList.clear()
            for (city in cityList) {
                dataList.add(city.cityName)
            }
            (list_view.adapter as BaseAdapter).notifyDataSetChanged()
            list_view.setSelection(0)
            currentLevel = LEVEL_CITY
        } else {
            val provinceCode = selectedProvince!!.provinceCode
            val address = "http://guolin.tech/api/china/" + provinceCode
            queryFromServer(address, "city")
        }
    }

    /**
     * 查询选中市内所有的县，优先从数据库查询，如果没有查询到再去服务器上查询。
     */
    private fun queryCounties() {
        title_text.setText(selectedCity!!.cityName)
        back_button.visibility=View.VISIBLE
        countyList = DataSupport.where("cityid = ?", java.lang.String.valueOf(selectedCity!!.id)).find(County::class.java) as ArrayList<County>
        if (countyList.size > 0) {
            dataList.clear()
            for (county in countyList) {
                dataList.add(county.countyName)
            }
            (list_view.adapter as BaseAdapter).notifyDataSetChanged()
            list_view.setSelection(0)
            currentLevel = LEVEL_COUNTY
        } else {
            val provinceCode = selectedProvince!!.provinceCode
            val cityCode = selectedCity!!.cityCode
            val address = "http://guolin.tech/api/china/$provinceCode/$cityCode"
            queryFromServer(address, "county")
        }
    }

    /**
     * 显示进度对话框
     */
     fun showProgressDialog() {
            if(progressDialog ==null){
                progressDialog =   ProgressDialog(activity)
            }
            progressDialog = ProgressDialog(activity)
            progressDialog!!.setMessage("正在加载...")
            progressDialog!!.setCanceledOnTouchOutside(false)
            progressDialog!!.show()
    }

    /**
     * 关闭进度对话框
     */
    private fun closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog!!.dismiss()
        }
    }

}
