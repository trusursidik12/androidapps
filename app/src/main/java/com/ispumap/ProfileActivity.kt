package com.ispumap
import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ispumap.OnMapAndViewReadyListener.OnGlobalLayoutAndMapReadyListener
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.maps.android.data.geojson.GeoJsonLayer
import com.google.maps.android.data.geojson.GeoJsonPolygonStyle
import com.google.maps.android.ui.IconGenerator
import com.ispumap.fragments.MyFrament
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class ProfileActivity : AppCompatActivity(), OnGlobalLayoutAndMapReadyListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener,OnMapReadyCallback {
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mLocation: Location? = null
    private lateinit var GMap: GoogleMap
    private lateinit var mapFragment : SupportMapFragment
    private var locationManager: LocationManager? = null
    private var mLocationManager: LocationManager? = null
    private var mLocationRequest: LocationRequest? = null
    private var isMapReady: Boolean = false
    private var focusing: Boolean = false
    private var isShowDetail: Boolean = false
    private var zoomview = 8f
    private val UPDATE_INTERVAL = (2 * 1000).toLong()
    private val isLocationEnabled: Boolean
        get() {
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager!!.isProviderEnabled(
                    LocationManager.NETWORK_PROVIDER)
        }
    private lateinit var loading: ProgressBar
    private var selectedMarker: Marker? = null

    private lateinit var icemoticon: ImageView
    private lateinit var txtlokasi: TextView
    private lateinit var txtcategory: TextView
    private lateinit var txtcappm10: TextView
    private lateinit var txtpm10: TextView
    private lateinit var txtcapso2: TextView
    private lateinit var txtso2: TextView
    private lateinit var txtcapco: TextView
    private lateinit var txtco: TextView
    private lateinit var txtcapo3: TextView
    private lateinit var txto3: TextView
    private lateinit var txtcapno2: TextView
    private lateinit var txtno2: TextView
    private lateinit var txtpressure: TextView
    private lateinit var txttemperature: TextView
    private lateinit var txtwinddirection: TextView
    private lateinit var txtwindspeed: TextView
    private lateinit var txthumidity: TextView
    private lateinit var txtrainrate: TextView
    private lateinit var txtsolarradiation: TextView


    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_home -> {
                val i = Intent(this@ProfileActivity, MainActivity::class.java)
                startActivity(i)
            }
            R.id.nav_city_detail -> {
                val i = Intent(this@ProfileActivity, CitiesActivity::class.java)
                startActivity(i)
            }
        }
        false
    }
	
	private val markerClickListener = object : GoogleMap.OnMarkerClickListener {
        override fun onMarkerClick(marker: Marker?): Boolean {
            //loading.visibility = View.VISIBLE
            return false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        icemoticon = findViewById<ImageView>(R.id.emoticon)
        txtlokasi = findViewById<TextView>(R.id.lokasi)
        txtcategory = findViewById<TextView>(R.id.category)
        txtcappm10 = findViewById<TextView>(R.id.cap_pm10)
        txtpm10 = findViewById<TextView>(R.id.pm10)
        txtcapso2 = findViewById<TextView>(R.id.cap_so2)
        txtso2 = findViewById<TextView>(R.id.so2)
        txtcapco = findViewById<TextView>(R.id.cap_co)
        txtco = findViewById<TextView>(R.id.co)
        txtcapo3 = findViewById<TextView>(R.id.cap_o3)
        txto3 = findViewById<TextView>(R.id.o3)
        txtcapno2 = findViewById<TextView>(R.id.cap_no2)
        txtno2 = findViewById<TextView>(R.id.no2)
        txtpressure = findViewById<TextView>(R.id.pressure)
        txttemperature = findViewById<TextView>(R.id.temperature)
        txtwinddirection = findViewById<TextView>(R.id.wind_direction)
        txtwindspeed = findViewById<TextView>(R.id.wind_speed)
        txthumidity = findViewById<TextView>(R.id.humidity)
        txtrainrate = findViewById<TextView>(R.id.rain_rate)
        txtsolarradiation = findViewById<TextView>(R.id.solar_radiation)
        bottom_navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        bottom_navigation.getMenu().findItem(R.id.nav_profile).setChecked(true)
        loading = findViewById<ProgressBar>(R.id.loading)
        loading.visibility = View.VISIBLE
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this@ProfileActivity);

        if (isNetworkAvailable(this@ProfileActivity)) {
            readVersion(this@ProfileActivity)
        } else {
            Toast.makeText(this@ProfileActivity,"Silakan periksa koneksi internet Anda, lalu mulai ulang Aplikasi ini", Toast.LENGTH_SHORT).show()
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

        if(areThereMockPermissionApps(this@ProfileActivity) && isMockSettingsON(this@ProfileActivity)){
            val confirmation = AlertDialog.Builder(this@ProfileActivity)
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
        load_data()
    }

    fun getIndexBackground(ispu:Int) : Int {
        if(ispu <= 50) return R.drawable.bgtext_baik
        else if(ispu <= 100) return R.drawable.bgtext_sedang
        else if(ispu <= 199) return R.drawable.bgtext_tidak_sehat
        else if(ispu <= 299) return R.drawable.bgtext_sangat_tidak
        else return R.drawable.bgtext_berbahaya
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
            load_data()
            val coordinate = LatLng(LATITUDE.toDouble(), LONGITUDE.toDouble())
            if(isMapReady){
                GMap.moveCamera(CameraUpdateFactory.newLatLng(coordinate))
                focusing = true
            }
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
        GMap.addMarker(markerOptions)
    }

    fun drawPolygon(geojson_file: Int,kategori: String = "NONE"){
        val layer = GeoJsonLayer(GMap, geojson_file,this@ProfileActivity)
        layer.addLayerToMap()
        var polyStyle: GeoJsonPolygonStyle = layer.defaultPolygonStyle
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

    fun load_data(){
        loading.visibility = View.VISIBLE
        isShowDetail = false
        LoadShowDetail()
        GET(this@ProfileActivity, "aqmCitiesinfo?trusur_api_key={trusur_api_key}&lat="+ LATITUDE + "&lng="+ LONGITUDE + "&limit=1", object : Callback {
            override fun onResponse(call: Call?, response: Response) {
                val responseData = JSONObject(response.body()?.string())
                if (responseData.getString("status") == "true") {
                    if (responseData.getString("data") != "") {
                        val id_stasiuns = JSONArray(responseData.getString("id_stasiuns"))
                        val ispus = JSONObject(responseData.getString("data"))
                        for (i in 0 until id_stasiuns.length()) {
                            val id_stasiun = id_stasiuns[i].toString()
                            if (id_stasiun != "") {
                                var ispu = JSONObject(ispus.getString(id_stasiun))
                                if(ispu.getString("status") == "true") {
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
                                }
                            }
                        }
                    }
                }
                isShowDetail = true
            }
            override fun onFailure(call: Call?, e: IOException?) {}
        })
    }

    fun ShowDetail() {
        if(category != ""){
            txtlokasi.setText(StasiunName.replace("-","\n"))
            txtcategory.setText("STATUS : " + category)
            if(category == "BAIK"){
                icemoticon.setImageDrawable(getResources().getDrawable(R.drawable.ic_emote_baik))
            } else if(category == "SEDANG"){
                icemoticon.setImageDrawable(getResources().getDrawable(R.drawable.ic_emote_sedang))
            } else if(category == "TIDAK_SEHAT") {
                icemoticon.setImageDrawable(getResources().getDrawable(R.drawable.ic_emote_tidak_sehat))
            } else if(category == "SANGAT_TIDAK_SEHAT") {
                icemoticon.setImageDrawable(getResources().getDrawable(R.drawable.ic_emote_sangat_tidak))
            } else if(category == "BERBAHAYA") {
                icemoticon.setImageDrawable(getResources().getDrawable(R.drawable.ic_emote_berbahaya))
            }
            txtpm10.setText(pm10.toString())
            txtcappm10.setBackgroundDrawable(getResources().getDrawable(getIndexBackground(pm10)))
            txtso2.setText(so2.toString())
            txtcapso2.setBackgroundDrawable(getResources().getDrawable(getIndexBackground(so2)))
            txtco.setText(co.toString())
            txtcapco.setBackgroundDrawable(getResources().getDrawable(getIndexBackground(co)))
            txto3.setText(o3.toString())
            txtcapo3.setBackgroundDrawable(getResources().getDrawable(getIndexBackground(o3)))
            txtno2.setText(no2.toString())
            txtcapno2.setBackgroundDrawable(getResources().getDrawable(getIndexBackground(no2)))
            txtpressure.setText(pressure.toString() + "\nmBar")
            txttemperature.setText(temperature.toString() + "\n°C")
            txtwinddirection.setText(wind_direction.toString() + "°")
            txtwindspeed.setText(wind_speed.toString() + "\nKm/h")
            txthumidity.setText(humidity.toString() + "%")
            txtrainrate.setText(rain_rate.toString() + "\nmm/jam")
            txtsolarradiation.setText(solar_radiation.toString() + "\nwatt/m2")
        }
        loading.visibility = View.GONE
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        isMapReady = true
        GMap = googleMap ?: return
        with(GMap) {
            moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(LATITUDE.toDouble(),LONGITUDE.toDouble()),zoomview))
            uiSettings.setAllGesturesEnabled(true)
            uiSettings.isZoomControlsEnabled = true
            setOnMarkerClickListener(markerClickListener)
            setOnMapClickListener { selectedMarker = null }
            drawMarker(LATITUDE.toDouble(), LONGITUDE.toDouble(),"BAIK","BAIK")
        }
        loading.visibility = View.GONE
    }

    fun LoadShowDetail() {
        if(!isShowDetail) Handler().postDelayed({LoadShowDetail()}, 500)
        else ShowDetail()
    }

    override fun onBackPressed() {
		val i = Intent(this@ProfileActivity, MainActivity::class.java)
        startActivity(i)
    }
}