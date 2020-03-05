package com.ispumap

import android.annotation.TargetApi
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import okhttp3.*
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import android.graphics.Color
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.support.v4.app.NotificationCompat
import android.view.View
import android.webkit.*
import android.widget.ProgressBar


var TOKEN = ""

fun isNetworkAvailable(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    var activeNetworkInfo: NetworkInfo? = null
    activeNetworkInfo = cm.activeNetworkInfo
    return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting
}

fun readNotification(context: Context){
    if(TOKEN != "") {
        val url = context.getResources().getString(R.string.SERVER_HOST) + "read_notification.php?token="+TOKEN
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }
            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseGET = response.body()!!.string()
                    if (responseGET != "") {
                        val responseGETs = responseGET.split("]]]")
                        if (responseGETs.size > 0) {
                            responseGETs.forEach {
                                val responses = it.split("|||")
                                if (responses.size > 1) {
                                    showNotification(context.applicationContext, context.getResources().getString(R.string.app_name), responses[1].toString(), responses[0].toInt())
                                }
                            }
                        }
                    }
                }
            }
        })
    }
    //Handler().postDelayed({ readNotification(context) }, 5000)
}

fun readVersion(context: Context){
    val url = context.getResources().getString(R.string.SERVER_HOST) + "get_version.php"
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

fun showNotification(context: Context,title:String,message:String,mNotificationId: Int = 1000){
    lateinit var mNotification: Notification
    var notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val notifyIntent = Intent(context, MainActivity::class.java)

    notifyIntent.putExtra("mNotified", true)
    notifyIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

    val pendingIntent = PendingIntent.getActivity(context, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    val res = context.resources
    val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val CHANNEL_ID = "Ispumap"
        val name = "Ispumap"
        val Description = "Ispumap Channel"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
        mChannel.description = Description
        mChannel.enableLights(true)
        mChannel.lightColor = Color.RED
        mChannel.enableVibration(true)
        mChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
        mChannel.setShowBadge(false)
        notificationManager.createNotificationChannel(mChannel)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setLargeIcon(BitmapFactory.decodeResource(res, R.mipmap.ic_launcher))
                .setPriority(Notification.PRIORITY_MAX)
                .setContentTitle(title)
                .setSound(uri)
                .setContentText(message)

        val resultIntent = Intent(context, MainActivity::class.java)
        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addParentStack(MainActivity::class.java)
        stackBuilder.addNextIntent(resultIntent)
        val resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

        builder.setContentIntent(resultPendingIntent)

        notificationManager.notify(mNotificationId, builder.build())
    } else {
        mNotification = Notification.Builder(context)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setLargeIcon(BitmapFactory.decodeResource(res, R.mipmap.ic_launcher))
                .setPriority(Notification.PRIORITY_MAX)
                .setContentTitle(title)
                .setStyle(Notification.BigTextStyle()
                        .bigText(message))
                .setSound(uri)
                .setContentText(message).build()
        notificationManager.notify(mNotificationId, mNotification)
    }

}

class MainActivity : AppCompatActivity() {
    private var mUploadMessage: ValueCallback<Uri>? = null
    private var mUploadMessages: ValueCallback<Array<Uri>>? = null
    private val FILECHOOSER_RESULTCODE = 1
    private val KITKAT_RESULTCODE = 2
    private lateinit var mCapturedImageURI: Uri
    private lateinit var webview: WebView

    internal var chromeClient: WebChromeClient = object : WebChromeClient() {
        fun openFileChooser(uploadMsg: ValueCallback<Uri>) { }
        fun openFileChooser(uploadMsg: ValueCallback<*>, acceptType: String) { }
        fun openFileChooser(uploadMsg: ValueCallback<Uri>, acceptType: String, capture: String) {
            mUploadMessage = uploadMsg
            val i = Intent(Intent.ACTION_GET_CONTENT)
            i.addCategory(Intent.CATEGORY_OPENABLE)
            i.type = "*/*"
            this@MainActivity.startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE)
        }
        fun showPicker(uploadMsg: ValueCallback<Uri>) {
            mUploadMessage = uploadMsg
            val i = Intent(Intent.ACTION_GET_CONTENT)
            i.addCategory(Intent.CATEGORY_OPENABLE)
            i.type = "*/*"
            this@MainActivity.startActivityForResult(Intent.createChooser(i, "File Chooser"), KITKAT_RESULTCODE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        webview = findViewById(R.id.webview) as WebView
        var progressBar: ProgressBar = findViewById<View>(R.id.progressBar1) as ProgressBar

        if (isNetworkAvailable(this@MainActivity)) {
            val path = this@MainActivity.getFilesDir()
            val filename = File(path.toString() + "/bWFya29wZWxhZ28=.dat")
            if(filename.exists()){
                TOKEN = FileInputStream(filename).bufferedReader().use { it.readText() }
            }
            webview = WebView(this)
            webview.getSettings().setAppCacheMaxSize( 10 * 1024 * 1024 ); // 10MB
            webview.getSettings().setAppCachePath( getApplicationContext().getCacheDir().getAbsolutePath() );
            webview.getSettings().setAppCacheEnabled(true);
            webview.getSettings().setCacheMode( WebSettings.LOAD_DEFAULT ); // load online by default
            webview.getSettings().setJavaScriptEnabled(true)
            webview.addJavascriptInterface(chromeClient, "jsi" );
            webview.getSettings().setAllowFileAccess(true);
            webview.getSettings().setAllowContentAccess(true);
            webview.clearCache(true);
            webview!!.webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                    // TODO Auto-generated method stub
                    super.onPageStarted(view, url, favicon)
                }
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {


                    if(url != null) {
                        if (url.toString().contains("https://api.whatsapp.com/send")) {
                            view?.getContext()?.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                        }else{
                            view?.loadUrl(url);

                            if (url.toString().contains("set_apps_token=")) {
                                val temp = url.split("set_apps_token=")
                                TOKEN = temp[1]
                                if(TOKEN!="") {
                                    val path = this@MainActivity.getFilesDir()
                                    val filename = File(path.toString() + "/bWFya29wZWxhZ28=.dat")
                                    filename.writeText(TOKEN)
                                    webview!!.loadUrl(url.replace("set_apps_token=" + TOKEN,""))
                                }
                            }
                            if (url.toString().contains("logout_success=1")) {
                                val path = this@MainActivity.getFilesDir()
                                val filename = File(path.toString() + "/bWFya29wZWxhZ28=.dat")
                                TOKEN = ""
                                filename.writeText(TOKEN )
                                webview!!.loadUrl(url.replace("logout_success=1",""))
                            }
                        }
                    } else {
                        view?.loadUrl(url);
                    }

                    return true
                }
                override fun onPageFinished(view: WebView, url: String) {
                    // TODO Auto-generated method stub
                    super.onPageFinished(view, url)
                    progressBar.setVisibility(View.GONE)
                }
            }
            //webview!!.loadUrl(this@MainActivity.getResources().getString(R.string.SERVER_HOST) + "android_apps.php?token=" + TOKEN)
            webview!!.loadUrl(this@MainActivity.getResources().getString(R.string.SERVER_HOST))

            webview!!.setWebChromeClient(object : WebChromeClient() {
                fun openFileChooser(uploadMsg: ValueCallback<Uri>, acceptType: String = "") {
                    mUploadMessage = uploadMsg
                    openImageChooser()
                }
                override fun onShowFileChooser(mWebView: WebView, filePathCallback: ValueCallback<Array<Uri>>, fileChooserParams: WebChromeClient.FileChooserParams): Boolean {
                    mUploadMessages = filePathCallback
                    openImageChooser()
                    return true
                }
                fun openFileChooser(uploadMsg: ValueCallback<Uri>, acceptType: String, capture: String) {
                    openFileChooser(uploadMsg, acceptType)
                }
            })
            setContentView(webview)

            //readWebView(this@MainActivity,webview)
            //readVersion(this@MainActivity)
            //readNotification(this@MainActivity)

        } else {
            Toast.makeText(this@MainActivity,"Please check your internet connection, then restart this App",Toast.LENGTH_SHORT).show()
        }
    }

    private fun openImageChooser() {
        try {
            val imageStorageDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "FolderName")
            if (!imageStorageDir.exists()) {
                imageStorageDir.mkdirs()
            }
            val file = File(imageStorageDir.toString() + File.separator + "IMG_" + System.currentTimeMillis().toString() + ".jpg")
            mCapturedImageURI = Uri.fromFile(file)

            val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI)

            val i = Intent(Intent.ACTION_GET_CONTENT)
            i.addCategory(Intent.CATEGORY_OPENABLE)
            i.type = "image/*"

            val chooserIntent = Intent.createChooser(i, "Image Chooser")
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf<Parcelable>(captureIntent))

            startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    protected override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage && null == mUploadMessages) {
                return
            }
            if (null != mUploadMessage) {
                handleUploadMessage(requestCode, resultCode, intent)
            } else if (mUploadMessages != null) {
                handleUploadMessages(requestCode, resultCode, intent)
            }
        }
    }

    private fun handleUploadMessage(requestCode: Int, resultCode: Int, intent: Intent?) {
        var result: Uri? = null
        try {
            if (resultCode != Activity.RESULT_OK) {
                result = null
            } else {
                result = if (intent == null) mCapturedImageURI else intent.data
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            mUploadMessage!!.onReceiveValue(result)
            mUploadMessage = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun handleUploadMessages(requestCode: Int, resultCode: Int, intent: Intent?) {
        var results: Array<Uri>? = null
        try {
            if (resultCode != Activity.RESULT_OK) {
                results = null
            } else {
                if (intent != null) {
                    val dataString = intent.dataString
                    val clipData = intent.clipData
                    if (clipData != null) {
                        //results = arrayOfNulls(clipData.itemCount)
                        for (i in 0 until clipData.itemCount) {
                            val item = clipData.getItemAt(i)
                            results!![i] = item.uri
                        }
                    }
                    if (dataString != null) {
                        results = arrayOf(Uri.parse(dataString))
                    }
                } else {
                    results = arrayOf<Uri>(mCapturedImageURI)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            mUploadMessages!!.onReceiveValue(results)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            mUploadMessages = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun onBackPressed() {
        if (webview.canGoBack()) {
            webview.goBack()
        } else {
            moveTaskToBack(true)
            System.exit(-1)
        }
    }
}