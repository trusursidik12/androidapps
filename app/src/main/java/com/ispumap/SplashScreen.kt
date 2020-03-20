package com.ispumap
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        val txtVersion: TextView = findViewById(R.id.txtVersion) as TextView
        txtVersion!!.setText("V." + this.packageManager.getPackageInfo(packageName, 0).versionName)

        Handler().postDelayed({
            val i = Intent(this@SplashScreen, MainActivity::class.java)
            startActivity(i)
        }, 2000)
    }
}
