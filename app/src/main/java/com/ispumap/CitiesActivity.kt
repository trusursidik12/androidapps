package com.ispumap

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.ispumap.ViewPagerAdapter.CitiesFragmentPagerAdapter
import com.ispumap.fragments.MyFrament
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException


class CitiesActivity : AppCompatActivity() {
    private lateinit var viewpager: ViewPager
    private lateinit var loading: ProgressBar
    private lateinit var id_stasiuns : JSONArray
    private var isShowStasiuns: Boolean = false
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_home -> {
                val i = Intent(this@CitiesActivity, MainActivity::class.java)
                startActivity(i)
            }
            R.id.nav_city_detail -> {
                recreate()
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cities)
        bottom_navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        bottom_navigation.getMenu().findItem(R.id.nav_city_detail).setChecked(true)
        loading = findViewById<ProgressBar>(R.id.loading)
        loading.visibility = View.VISIBLE
        initViews()
        setupViewPager()
    }

    private fun initViews() {
        viewpager = findViewById(R.id.viewpager)
    }

    private fun setupViewPager() {
        isShowStasiuns = false
        LoadShowStasiuns()
        GET(this@CitiesActivity, "aqmCitiesinfo?trusur_api_key={trusur_api_key}", object : Callback {
            override fun onResponse(call: Call?, response: Response) {
                val responseData = JSONObject(response.body()?.string())
                if (responseData.getString("status") == "true") {
                    if (responseData.getString("data") != "") {
                        val data = JSONObject(responseData.getString("data"))
                        id_stasiuns = JSONArray(responseData.getString("id_stasiuns"))
                    }
                }
                isShowStasiuns = true
            }
            override fun onFailure(call: Call?, e: IOException?) {}
        })
    }

    fun ShowStasiuns() {
        if(id_stasiuns.length() > 0) {
            val adapter = CitiesFragmentPagerAdapter(getSupportFragmentManager())
            for (i in 0 until id_stasiuns.length()) {
                val id_stasiun = id_stasiuns[i].toString()
                if (id_stasiun != "") {
                    var Fragmet: MyFrament = MyFrament.newInstance(id_stasiun)
                    adapter.addFragment(Fragmet, id_stasiun)
                    //if(ispu.getString("lat") != "" && ispu.getString("lng") != "") {
                    /*category = responseData.getString("category")
                        StasiunName = responseData.getString("stasiun_name")
                        City = responseData.getString("city")
                        Province = responseData.getString("province")
                        pm10 = responseData.getString("pm10").toInt()
                        so2 = responseData.getString("so2").toInt()
                        co = responseData.getString("co").toInt()
                        o3 = responseData.getString("o3").toInt()
                        no2 = responseData.getString("no2").toInt()
                        pressure = responseData.getString("pressure").toFloat()
                        temperature = responseData.getString("temperature").toFloat()
                        wind_speed = responseData.getString("wind_speed").toInt()
                        wind_direction = responseData.getString("wind_direction").toInt()
                        humidity = responseData.getString("humidity").toInt()
                        rain_rate = responseData.getString("rain_rate").toFloat()
                        solar_radiation = responseData.getString("solar_radiation").toInt()*/
                    //}
                }
            }
            viewpager!!.adapter = adapter
        }
        loading.visibility = View.INVISIBLE
    }

    fun LoadShowStasiuns() {
        if(!isShowStasiuns) Handler().postDelayed({LoadShowStasiuns()}, 100)
        else ShowStasiuns()
    }
}