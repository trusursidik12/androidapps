package com.ispumap

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*

class DssActivity : AppCompatActivity(){
    private lateinit var txtLogin: TextView
    private lateinit var txtdesc: TextView
    var isLogin: Boolean = true
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_home -> {
                val i = Intent(this@DssActivity, MainActivity::class.java)
                startActivity(i)
            }
            R.id.nav_city_detail -> {
                val i = Intent(this@DssActivity, CitiesActivity::class.java)
                startActivity(i)
            }
            R.id.nav_news -> {

            }
            R.id.nav_profile -> {
                val i = Intent(this@DssActivity, ProfileActivity::class.java)
                startActivity(i)
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dss)
        txtdesc = findViewById<TextView>(R.id.desc)
        txtLogin = findViewById<TextView>(R.id.login)
        txtLogin.setOnClickListener {
            if(isLogin) {
                txtdesc.setText("Fitur ini dalam pengembangan..")
                txtLogin.setText("Home")
                isLogin = false
            } else {
                val i = Intent(this@DssActivity, MainActivity::class.java)
                startActivity(i)
            }
        }

        bottom_navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        bottom_navigation.getMenu().findItem(R.id.nav_dss).setChecked(true)
    }

    override fun onBackPressed() {
		val i = Intent(this@DssActivity, MainActivity::class.java)
        startActivity(i)
    }
}