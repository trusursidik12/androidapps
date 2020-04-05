package com.ispumap

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import org.json.JSONObject


class MyFragment : Fragment() {
    companion object {
        fun newInstance(message: String): MyFragment {
            val f = MyFragment()
            val bdl = Bundle(1)
            bdl.putString(EXTRA_MESSAGE, message)
            f.setArguments(bdl)
            return f
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    private fun getIndexBackground(ispu:Int) : Int {
        if(ispu <= 50) return R.drawable.bgtext_baik
        else if(ispu <= 100) return R.drawable.bgtext_sedang
        else if(ispu <= 199) return R.drawable.bgtext_tidak_sehat
        else if(ispu <= 299) return R.drawable.bgtext_sangat_tidak
        else return R.drawable.bgtext_berbahaya
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view: View? = inflater.inflate(R.layout.fragment_cities, container, false);
        val ispus = arguments!!.getString(EXTRA_MESSAGE)
        var ispu = JSONObject(ispus)
        if(ispu.getString("status") == "true") {
            val ic_emoticon: ImageView = view!!.findViewById<View>(R.id.emoticon) as ImageView
            val txt_category: TextView = view!!.findViewById<View>(R.id.category) as TextView
            val txt_stasiun_name: TextView = view!!.findViewById<View>(R.id.stasiun_name) as TextView
            val txt_city: TextView = view!!.findViewById<View>(R.id.city) as TextView
            val txt_province: TextView = view!!.findViewById<View>(R.id.province) as TextView
            val txt_pm10: TextView = view!!.findViewById<View>(R.id.pm10) as TextView
            val txt_so2: TextView = view!!.findViewById<View>(R.id.so2) as TextView
            val txt_co: TextView = view!!.findViewById<View>(R.id.co) as TextView
            val txt_o3: TextView = view!!.findViewById<View>(R.id.o3) as TextView
            val txt_no2: TextView = view!!.findViewById<View>(R.id.no2) as TextView
            val txt_pressure: TextView = view!!.findViewById<View>(R.id.pressure) as TextView
            val txt_temperature: TextView = view!!.findViewById<View>(R.id.temperature) as TextView
            val txt_wind_direction: TextView = view!!.findViewById<View>(R.id.wind_direction) as TextView
            val txt_wind_speed: TextView = view!!.findViewById<View>(R.id.wind_speed) as TextView
            val txt_humidity: TextView = view!!.findViewById<View>(R.id.humidity) as TextView
            val txt_rain_rate: TextView = view!!.findViewById<View>(R.id.rain_rate) as TextView
            val txt_solar_radiation: TextView = view!!.findViewById<View>(R.id.solar_radiation) as TextView

            category = ispu.getString("category")
            StasiunName = ispu.getString("stasiun_name")
            City = ispu.getString("city")
            Province = ispu.getString("province")
            pm10 = ispu.getString("pm10").toInt()
            so2 = ispu.getString("so2").toInt()
            co = ispu.getString("co").toInt()
            o3 = ispu.getString("o3").toInt()
            no2 = ispu.getString("no2").toInt()
            pressure = ispu.getString("pressure").toFloat()
            temperature = ispu.getString("temperature").toFloat()
            wind_speed = ispu.getString("wind_speed").toInt()
            wind_direction = ispu.getString("wind_direction").toInt()
            humidity = ispu.getString("humidity").toInt()
            rain_rate = ispu.getString("rain_rate").toFloat()
            solar_radiation = ispu.getString("solar_radiation").toInt()

            txt_category.setText("STATUS : " + category)
            if(category == "BAIK"){
                ic_emoticon.setImageDrawable(getResources().getDrawable(R.drawable.ic_emote_baik))
            } else if(category == "SEDANG"){
                ic_emoticon.setImageDrawable(getResources().getDrawable(R.drawable.ic_emote_sedang))
            } else if(category == "TIDAK_SEHAT") {
                ic_emoticon.setImageDrawable(getResources().getDrawable(R.drawable.ic_emote_tidak_sehat))
            } else if(category == "SANGAT_TIDAK_SEHAT") {
                ic_emoticon.setImageDrawable(getResources().getDrawable(R.drawable.ic_emote_sangat_tidak))
            } else if(category == "BERBAHAYA") {
                ic_emoticon.setImageDrawable(getResources().getDrawable(R.drawable.ic_emote_berbahaya))
            }
            txt_stasiun_name.setText(StasiunName)
            txt_city.setText(City)
            txt_province.setText(Province)
            txt_pm10.setText(pm10.toString())
            txt_pm10.setBackgroundDrawable(getResources().getDrawable(getIndexBackground(pm10)))
            txt_so2.setText(so2.toString())
            txt_so2.setBackgroundDrawable(getResources().getDrawable(getIndexBackground(so2)))
            txt_co.setText(co.toString())
            txt_co.setBackgroundDrawable(getResources().getDrawable(getIndexBackground(co)))
            txt_o3.setText(o3.toString())
            txt_o3.setBackgroundDrawable(getResources().getDrawable(getIndexBackground(o3)))
            txt_no2.setText(no2.toString())
            txt_no2.setBackgroundDrawable(getResources().getDrawable(getIndexBackground(no2)))
            txt_pressure.setText(pressure.toString() + "\nmBar")
            txt_temperature.setText(temperature.toString() + "\n°C")
            txt_wind_direction.setText(wind_direction.toString() + "°")
            txt_wind_speed.setText(wind_speed.toString() + "\nKm/h")
            txt_humidity.setText(humidity.toString() + "%")
            txt_rain_rate.setText(rain_rate.toString() + "\nmm/jam")
            txt_solar_radiation.setText(solar_radiation.toString() + "\nwatt/m2")
        }

        var textView: TextView = view!!.findViewById(R.id.category)
        textView!!.text = category
        return view
    }


}

