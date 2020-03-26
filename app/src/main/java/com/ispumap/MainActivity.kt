package com.ispumap
import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.maps.android.data.geojson.GeoJsonLayer
import com.google.maps.android.data.geojson.GeoJsonPolygonStyle
import com.google.maps.android.ui.IconGenerator
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.lang.reflect.Field
import java.math.BigInteger

var LATITUDE = ""
var LONGITUDE = ""
var client = OkHttpClient()
var  category = "SEDANG"
var  StasiunName = "Ciwandan"
var  City = "Cilegon"
var  Province = "Banten"
var  pm10 = 100
var  so2 = 200
var  co = 300
var  o3 = 400
var  no2 = 500
var  pressure = 1007.4f
var  temperature = 28.3f
var  wind_speed = 13f
var  wind_direction = 145f
var  humidity = 85f
var  rain_rate = 4.4f
var  solar_radiation = 63f
/*var  pm10_1 = 500
var  so2_1 = 400
var  co_1 = 200
var  o3_1 = 100
var  no2_1 = 300
var  pressure_1 = 1008.4f
var  temperature_1 = 29.3f
var  wind_speed_1 = 14f
var  wind_direction_1 = 146f
var  humidity_1 = 86f
var  rain_rate_1 = 4.5f
var  solar_radiation_1 = 64f*/

fun isNetworkAvailable(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    var activeNetworkInfo: NetworkInfo? = null
    activeNetworkInfo = cm.activeNetworkInfo
    return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting
}

fun readVersion(context: Context){
    val url = context.getResources().getString(R.string.API_HOST) + "../get_version.php"
    val client = OkHttpClient()
    val request = Request.Builder().url(url).build()
    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
        }
        @Throws(IOException::class)
        override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful) {
                val getVersion = response.body()!!.string().toLong()
                if (getVersion  > context.packageManager.getPackageInfo("com.ispumap", 0).versionCode) {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse("https://play.google.com/store/apps/details?id=com.ispumap")
                    context.startActivity(intent)
                }
            }
        }
    })
}

fun GET(context: Context,url: String, callback: Callback): Call {
    val credentials: String = Credentials.basic("admin", "cHQudHJ1c3VydW5nZ3VsdGVrbnVzYQ==")
    val request = Request.Builder()
            .url(context.getResources().getString(R.string.API_HOST) + url.replace("{trusur_api_key}",context.getResources().getString(R.string.trusur_api_key)))
            .header("Authorization", credentials)
            .build()
    val call = client.newCall(request)
    call.enqueue(callback)
    return call
}

class MainActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener,OnMapReadyCallback {
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mLocation: Location? = null
    private var GMap: GoogleMap? = null
    private var locationManager: LocationManager? = null
    private var mLocationManager: LocationManager? = null
    private var mLocationRequest: LocationRequest? = null
    private var focusing: Boolean = false
    private var reloadlayer: Boolean = false
    private var zoomview = 5f
    private var back_clicked = 0
    private var markers_lat = Array<Double>(1000,{0.0})
    private var markers_lng = Array<Double>(1000,{0.0})
    private var markers_title = Array<String>(1000,{""})
    private var markers_cat = Array<String>(1000,{""})
    private var provinces = Array<Int>(50,{0})
    private var categories = Array<String>(50,{"NONE"})
    private val UPDATE_INTERVAL = (2 * 1000).toLong()
    private val isLocationEnabled: Boolean
        get() {
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager!!.isProviderEnabled(
                    LocationManager.NETWORK_PROVIDER)
        }
    private lateinit var loading: ProgressBar

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_home -> {
                recreate()
            }
            R.id.nav_city_detail -> {
                Toast.makeText(this@MainActivity,"nav_city_detail", Toast.LENGTH_SHORT).show()
            }
        }
        false
    }
    private lateinit var popup_stasiun_detail: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        popup_stasiun_detail = Dialog(this@MainActivity);
        bottom_navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        loading = findViewById<ProgressBar>(R.id.loading)
        loading.visibility = View.VISIBLE
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
        if (isNetworkAvailable(this@MainActivity)) {
            readVersion(this@MainActivity)
        } else {
            Toast.makeText(this@MainActivity,"Silakan periksa koneksi internet Anda, lalu mulai ulang Aplikasi ini", Toast.LENGTH_SHORT).show()
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
                || (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED)
        ) {
            val permission_camera = arrayOf(
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            ActivityCompat.requestPermissions(this, permission_camera, 1)
        }

        if(areThereMockPermissionApps(this@MainActivity) && isMockSettingsON(this@MainActivity)){
            val confirmation = AlertDialog.Builder(this@MainActivity)
            confirmation.setTitle("Peringatan")
            confirmation.setMessage("Perangkat Anda terdeteksi mengizinkan akses 'Mock Location', Harap matikan perizinan akses 'Mock Location' tersebut, lalu hidupkan ulang Aplikasi ini kembali.")
            confirmation.setPositiveButton("OK"){dialog, which -> }
            val dialog: AlertDialog = confirmation.create()
            dialog.show()
            dialog.setOnDismissListener {
            }
        }

        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()
        mLocationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        isGPSon()
        if (mGoogleApiClient != null) mGoogleApiClient!!.connect()
        ShowPopup()
    }

    fun getIndexBackground(ispu:Int) : Int {
        if(ispu <= 50) return R.drawable.bgtext_baik
        else if(ispu <= 100) return R.drawable.bgtext_sedang
        else if(ispu <= 199) return R.drawable.bgtext_tidak_sehat
        else if(ispu <= 299) return R.drawable.bgtext_sangat_tidak
        else return R.drawable.bgtext_berbahaya
    }

    fun ShowPopup() {
        popup_stasiun_detail.setContentView(R.layout.popup_stasiun_detail)
        popup_stasiun_detail.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val txt_close: TextView = popup_stasiun_detail.findViewById<View>(R.id.close) as TextView
        val ic_emoticon: ImageView = popup_stasiun_detail.findViewById<View>(R.id.emoticon) as ImageView
        val txt_category: TextView = popup_stasiun_detail.findViewById<View>(R.id.category) as TextView
        val txt_stasiun_name: TextView = popup_stasiun_detail.findViewById<View>(R.id.stasiun_name) as TextView
        val txt_city: TextView = popup_stasiun_detail.findViewById<View>(R.id.city) as TextView
        val txt_province: TextView = popup_stasiun_detail.findViewById<View>(R.id.province) as TextView
        val txt_pm10: TextView = popup_stasiun_detail.findViewById<View>(R.id.pm10) as TextView
        val txt_so2: TextView = popup_stasiun_detail.findViewById<View>(R.id.so2) as TextView
        val txt_co: TextView = popup_stasiun_detail.findViewById<View>(R.id.co) as TextView
        val txt_o3: TextView = popup_stasiun_detail.findViewById<View>(R.id.o3) as TextView
        val txt_no2: TextView = popup_stasiun_detail.findViewById<View>(R.id.no2) as TextView
        val txt_pressure: TextView = popup_stasiun_detail.findViewById<View>(R.id.pressure) as TextView
        val txt_temperature: TextView = popup_stasiun_detail.findViewById<View>(R.id.temperature) as TextView
        val txt_wind_direction: TextView = popup_stasiun_detail.findViewById<View>(R.id.wind_direction) as TextView
        val txt_wind_speed: TextView = popup_stasiun_detail.findViewById<View>(R.id.wind_speed) as TextView
        val txt_humidity: TextView = popup_stasiun_detail.findViewById<View>(R.id.humidity) as TextView
        val txt_rain_rate: TextView = popup_stasiun_detail.findViewById<View>(R.id.rain_rate) as TextView
        val txt_solar_radiation: TextView = popup_stasiun_detail.findViewById<View>(R.id.solar_radiation) as TextView
        /*val txt_pm10_1: TextView = popup_stasiun_detail.findViewById<View>(R.id.pm10_1) as TextView
        val txt_so2_1: TextView = popup_stasiun_detail.findViewById<View>(R.id.so2_1) as TextView
        val txt_co_1: TextView = popup_stasiun_detail.findViewById<View>(R.id.co_1) as TextView
        val txt_o3_1: TextView = popup_stasiun_detail.findViewById<View>(R.id.o3_1) as TextView
        val txt_no2_1: TextView = popup_stasiun_detail.findViewById<View>(R.id.no2_1) as TextView
        val txt_pressure_1: TextView = popup_stasiun_detail.findViewById<View>(R.id.pressure_1) as TextView
        val txt_temperature_1: TextView = popup_stasiun_detail.findViewById<View>(R.id.temperature_1) as TextView
        val txt_wind_direction_1: TextView = popup_stasiun_detail.findViewById<View>(R.id.wind_direction_1) as TextView
        val txt_wind_speed_1: TextView = popup_stasiun_detail.findViewById<View>(R.id.wind_speed_1) as TextView
        val txt_humidity_1: TextView = popup_stasiun_detail.findViewById<View>(R.id.humidity_1) as TextView
        val txt_rain_rate_1: TextView = popup_stasiun_detail.findViewById<View>(R.id.rain_rate_1) as TextView
        val txt_solar_radiation_1: TextView = popup_stasiun_detail.findViewById<View>(R.id.solar_radiation_1) as TextView*/

        txt_close.setOnClickListener(View.OnClickListener { popup_stasiun_detail.dismiss() })
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
        /*txt_pm10_1.setText(pm10_1.toString())
        txt_so2_1.setText(so2_1.toString())
        txt_co_1.setText(co_1.toString())
        txt_o3_1.setText(o3_1.toString())
        txt_no2_1.setText(no2_1.toString())
        txt_pressure_1.setText(pressure_1.toString() + "\nmBar")
        txt_temperature_1.setText(temperature_1.toString() + "\n°C")
        txt_wind_direction_1.setText(wind_direction_1.toString() + "°")
        txt_wind_speed_1.setText(wind_speed_1.toString() + "\nKm/h")
        txt_humidity_1.setText(humidity_1.toString() + "%")
        txt_rain_rate_1.setText(rain_rate_1.toString() + "\nmm/jam")
        txt_solar_radiation_1.setText(solar_radiation_1.toString() + "\nwatt/m2")*/

        popup_stasiun_detail.show()
        loading.visibility = View.GONE
    }


    @SuppressLint("MissingPermission")
    override fun onConnected(p0: Bundle?) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            val permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
            ActivityCompat.requestPermissions(this, permissions,0)
        }
        startLocationUpdates()
        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)
        if (mLocation == null) startLocationUpdates()
    }

    override fun onConnectionSuspended(i: Int) {
        mGoogleApiClient!!.connect()
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {

    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
        if (mGoogleApiClient!!.isConnected()) {
            mGoogleApiClient!!.disconnect()
        }
    }

    @SuppressLint("MissingPermission")
    protected fun startLocationUpdates() {
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(UPDATE_INTERVAL)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            val permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
            ActivityCompat.requestPermissions(this, permissions,0)
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest, this)
    }

    override fun onLocationChanged(location: Location) {
        LATITUDE = java.lang.Double.toString(location.latitude)
        LONGITUDE = java.lang.Double.toString(location.longitude)
        if(!focusing) {
            val coordinate = LatLng(LATITUDE.toDouble(), LONGITUDE.toDouble())
            GMap!!.moveCamera(CameraUpdateFactory.newLatLng(coordinate))
            focusing = true
        }
    }

    private fun isGPSon(): Boolean {
        if (!isLocationEnabled) showAlert_GPSisOff()
        return isLocationEnabled
    }

    private fun showAlert_GPSisOff() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Enable Location")
                .setMessage("Setingan lokasi pada perangkat Anda sedang dimatikan.\nSilakan aktifkan setingan lokasi untuk " + "menggunakan aplikasi ini")
                .setPositiveButton("Location Settings") { paramDialogInterface, paramInt ->
                    val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(myIntent)
                }
                .setNegativeButton("Cancel") { paramDialogInterface, paramInt -> }
        dialog.show()
    }

    fun isMockSettingsON(context: Context): Boolean {
        return if(Settings.Secure.getString( context.getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION ).equals("0")) false
        else true
    }

    fun areThereMockPermissionApps(context: Context): Boolean {
        var count = 0
        val pm = context.packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        for (applicationInfo in packages) {
            try {
                val packageInfo = pm.getPackageInfo(
                        applicationInfo.packageName,
                        PackageManager.GET_PERMISSIONS
                )
                val requestedPermissions = packageInfo.requestedPermissions
                if (requestedPermissions != null) {
                    for (i in requestedPermissions.indices) {
                        if (requestedPermissions[i] == "android.permission.ACCESS_MOCK_LOCATION" && applicationInfo.packageName != context.packageName) count++
                    }
                }
            } catch (t: Throwable) { }
        }
        return if (count > 0) true else false
    }

    fun drawMarker(lat:Double,lng:Double,title:String,category:String){
        var color = getResources().getColor(R.color.NONE)
        if(category == "BAIK") {
            color = getResources().getColor(R.color.BAIK)
        } else if(category == "SEDANG") {
            color = getResources().getColor(R.color.SEDANG)
        } else if(category == "TIDAK_SEHAT") {
            color = getResources().getColor(R.color.TIDAK_SEHAT)
        } else if(category == "SANGAT_TIDAK_SEHAT") {
            color = getResources().getColor(R.color.SANGAT_TIDAK_SEHAT)
        } else if(category == "BERBAHAYA") {
            color = getResources().getColor(R.color.BERBAHAYA)
        }
        val iconFactory = IconGenerator(this)
        iconFactory.setColor(color)
        iconFactory.setTextAppearance(R.style.MarkerTitle);
        val markerOptions = MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(title))).position(LatLng(lat, lng)).anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV()).title(title)
        GMap!!.addMarker(markerOptions)
    }

    fun drawPolygon(geojson_file: Int,kategori: String = "NONE"){
        val layer = GeoJsonLayer(GMap, geojson_file,applicationContext)
        layer.addLayerToMap()
        var polyStyle: GeoJsonPolygonStyle = layer.defaultPolygonStyle
        polyStyle.setFillColor(getResources().getColor(R.color.NONE))
        polyStyle.setStrokeColor(getResources().getColor(R.color.STROKE_NONE))
        polyStyle.setStrokeWidth(2f)
        if(kategori == "BAIK") {
            polyStyle.setStrokeColor(getResources().getColor(R.color.STROKE_BAIK))
            polyStyle.setStrokeWidth(2f)
        } else if(kategori == "SEDANG") {
            polyStyle.setStrokeColor(getResources().getColor(R.color.STROKE_SEDANG))
            polyStyle.setStrokeWidth(2f)
        } else if(kategori == "TIDAK_SEHAT") {
            polyStyle.setStrokeColor(getResources().getColor(R.color.STROKE_TIDAK_SEHAT))
            polyStyle.setStrokeWidth(2f)
        } else if(kategori == "SANGAT_TIDAK_SEHAT") {
            polyStyle.setStrokeColor(getResources().getColor(R.color.STROKE_SANGAT_TIDAK_SEHAT))
            polyStyle.setStrokeWidth(2f)
        } else if(kategori == "BERBAHAYA") {
            polyStyle.setStrokeColor(getResources().getColor(R.color.STROKE_BERBAHAYA))
            polyStyle.setStrokeWidth(2f)
        }
    }

    fun loadLayers(){
        GMap!!.clear();
        val f: Array<Field> = R.raw::class.java.getFields()
        var request_id = 0
        var marker_id = 0
        for (province in f) {
            provinces[request_id] = province.getInt(null)
            GET(this@MainActivity,"aqmprovince?trusur_api_key={trusur_api_key}&provinsi=" + province.name.replace("_","%20") + "&request_id=" + request_id, object: Callback {
                override fun onResponse(call: Call?, response: Response) {
                    val responseData = JSONObject(response.body()?.string())
                    if (responseData.getString("status") == "true") {
                        val resp_request_id = responseData.getString("request_id").toInt()
                        val resumes = JSONObject(responseData.getString("resumes"))
                        categories[resp_request_id] = resumes.getString("category").toUpperCase()
                        if (resumes.getString("ispu") != "") {
                            val ispus = JSONArray(resumes.getString("ispu"))
                            for (i in 0 until ispus.length()){
                                val ispu = JSONObject(ispus[i].toString())
                                if(ispu.getString("lat") != "" && ispu.getString("lng") != "") {
                                    markers_lat[marker_id] = ispu.getString("lat").toDouble()
                                    markers_lng[marker_id] = ispu.getString("lng").toDouble()
                                    markers_cat[marker_id] = ispu.getString("category").toUpperCase()
                                    markers_title[marker_id] = ispu.getString("category")
                                    marker_id++;
                                }
                            }
                        }
                    }
                    reloadlayer = false
                }
                override fun onFailure(call: Call?, e: IOException?) {}
            })
            request_id++
        }
        getReadyReloadLayer()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        googleMap.uiSettings.setAllGesturesEnabled(true)
        googleMap.getUiSettings().setZoomControlsEnabled(true)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(0.toDouble(), 0.toDouble()),zoomview))
        GMap = googleMap
        loadLayers()
    }

    override fun onBackPressed() {
        back_clicked = back_clicked + 1
        Handler().postDelayed({back_clicked = 0}, 2000)
        if (back_clicked > 1){
            moveTaskToBack(true)
            System.exit(-1)
        } else
            Toast.makeText(this@MainActivity,"Tekan \"kembali\" sekali lagi untuk keluar dari aplikasi ini", Toast.LENGTH_SHORT).show()
    }

    fun getReadyReloadLayer(){
        Handler().postDelayed({
            if(!reloadlayer) {
                reloadlayer = true
                Handler().postDelayed({getReadyReloadLayer()}, 500)
            }else{
                provinces.forEachIndexed { index, province ->
                    if(province > 0) {
                        drawPolygon(province, categories[index])
                    }
                }
                markers_lat.forEachIndexed { index, lat ->
                    if(markers_title[index] != "")
                        drawMarker(lat,markers_lng[index],markers_title[index],markers_cat[index])
                }
                loading.visibility = View.GONE
            }
        }, 500)
    }

}