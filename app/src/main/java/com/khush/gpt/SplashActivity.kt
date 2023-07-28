package com.khush.gpt

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.CookieSyncManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat


@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var activity: SplashActivity
    private lateinit var myEdit: SharedPreferences.Editor

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val sharedPreferences = getSharedPreferences("database", MODE_PRIVATE)
        val appState = sharedPreferences.getInt("appState", 11)
        myEdit = sharedPreferences.edit()
        activity = this
        var status = false

        if(appState==22){
            skipActivity()
        }else{
            val webView: WebView = findViewById(R.id.splashWebView)
            webView.settings.javaScriptEnabled = true
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
            webView.clearCache(true)
            webView.clearHistory()
            clearCookies(applicationContext)
            webView.webViewClient = WebViewClient()

            webView.webViewClient = object : WebViewClient() {
                @Deprecated("Deprecated in Java")
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    view.loadUrl(url)
                    return true
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    status = true
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    if (status) {
                        skipActivity()
                    }
                }
            }
            webView.loadUrl("https://talkai.info/chat/send/")
        }
    }

    private fun skipActivity(){
        with (myEdit) {
            putInt("appState", 22)
            apply()
        }

        val intent = Intent(activity, MainActivity::class.java)
        val bundle = ActivityOptionsCompat.makeCustomAnimation(
            applicationContext,
            android.R.anim.fade_in, android.R.anim.fade_out
        ).toBundle()
        startActivity(intent, bundle)
        activity.finish()
    }

    @SuppressWarnings("deprecation")
    fun clearCookies(context: Context?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            CookieManager.getInstance().removeAllCookies(null)
            CookieManager.getInstance().flush()
        } else if (context != null) {
            val cookieSyncManager = CookieSyncManager.createInstance(context)
            cookieSyncManager.startSync()
            val cookieManager: CookieManager = CookieManager.getInstance()
            cookieManager.removeAllCookie()
            cookieManager.removeSessionCookie()
            cookieSyncManager.stopSync()
            cookieSyncManager.sync()
        }
    }
}