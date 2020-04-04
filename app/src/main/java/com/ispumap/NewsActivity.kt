package com.ispumap

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.webkit.*
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*

class NewsActivity : AppCompatActivity() {
    private lateinit var webview: WebView
    private lateinit var loading: ProgressBar
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_home -> {
                val i = Intent(this@NewsActivity, MainActivity::class.java)
                startActivity(i)
            }
            R.id.nav_city_detail -> {
                val i = Intent(this@NewsActivity, CitiesActivity::class.java)
                startActivity(i)
            }
            R.id.nav_profile -> {
                val i = Intent(this@NewsActivity, ProfileActivity::class.java)
                startActivity(i)
            }
            R.id.nav_dss -> {
                val i = Intent(this@NewsActivity, DssActivity::class.java)
                startActivity(i)
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news)

        loading = findViewById<ProgressBar>(R.id.loading)

        bottom_navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        bottom_navigation.getMenu().findItem(R.id.nav_news).setChecked(true)

        if (isNetworkAvailable(this@NewsActivity)) {
            setUrl(getString(R.string.API_HOST) + "../rssnews")
        } else {
            Toast.makeText(this@NewsActivity,"Silakan periksa koneksi internet Anda, lalu mulai ulang Aplikasi ini", Toast.LENGTH_SHORT).show()
            val i = Intent(this@NewsActivity, MainActivity::class.java)
            startActivity(i)
        }
    }

    fun setUrl(url: String) {
        var webUrl = url
        if (!webUrl!!.startsWith("http")) {
            webUrl = "http://$url"
        }
        webview = findViewById<WebView>(R.id.webView)
        webview.getSettings().javaScriptEnabled = true;
        webview.getSettings().javaScriptCanOpenWindowsAutomatically = true;
        webview.getSettings().setAppCacheMaxSize(10 * 1024 * 1024); // 10MB
        webview.getSettings().setAppCachePath(getApplicationContext().getCacheDir().getAbsolutePath());
        webview.getSettings().setAppCacheEnabled(true);
        webview.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT); // load online by default
        webview.getSettings().setJavaScriptEnabled(true)
        webview.getSettings().setAllowFileAccess(true);
        webview.getSettings().setAllowContentAccess(true);
        webview.clearCache(true);
        webview!!.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                view?.loadUrl(url);
                return true
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
            }
        }

        webview!!.loadUrl(webUrl)
        webview!!.setWebChromeClient(object : WebChromeClient() {
            override fun onShowFileChooser(
                    mWebView: WebView,
                    filePathCallback: ValueCallback<Array<Uri>>,
                    fileChooserParams: WebChromeClient.FileChooserParams
            ): Boolean {
                return true
            }
        })
    }

    override fun onBackPressed() {
        val i = Intent(this@NewsActivity, MainActivity::class.java)
        startActivity(i)
    }

}
