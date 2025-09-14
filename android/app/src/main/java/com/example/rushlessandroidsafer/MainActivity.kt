package com.example.rushlessandroidsafer

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.net.URLDecoder

class MainActivity : AppCompatActivity() {

    private var examInProgress = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.action == Intent.ACTION_VIEW) {
            examInProgress = true
            setContentView(R.layout.activity_main)
            val webView: WebView = findViewById(R.id.webview)
            webView.webViewClient = WebViewClient()
            webView.settings.javaScriptEnabled = true
            webView.addJavascriptInterface(WebAppInterface(this), "Android")

            val data: Uri? = intent.data
            val url = data?.getQueryParameter("url")
            val cookies = data?.getQueryParameter("cookies")

            if (url != null) {
                val decodedUrl = URLDecoder.decode(url, "UTF-8")
                if (cookies != null) {
                    val decodedCookies = URLDecoder.decode(cookies, "UTF-8")
                    val cookieManager = CookieManager.getInstance()
                    cookieManager.setAcceptCookie(true)
                    // Split cookies and set them individually
                    decodedCookies.split(';').forEach { cookie ->
                        cookieManager.setCookie(decodedUrl, cookie.trim())
                    }
                    cookieManager.flush()
                }
                webView.loadUrl(decodedUrl)
            } else {
                // Handle error: URL not found
            }
        } else {
            setContentView(R.layout.activity_manual)
        }
    }

    override fun onStart() {
        super.onStart()
        if (examInProgress) {
            val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val admin = ComponentName(this, MyDeviceAdminReceiver::class.java)
            if (dpm.isDeviceOwnerApp(packageName) || dpm.isProfileOwnerApp(packageName)) {
                if (dpm.isLockTaskPermitted(packageName)) {
                    startLockTask()
                } else {
                    Toast.makeText(this, "Lock task not permitted.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "App is not a device owner.", Toast.LENGTH_SHORT).show()
                // For testing without device owner, you can start lock task directly.
                // This will prompt the user for confirmation.
                startLockTask()
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (examInProgress && !hasFocus) {
            // Re-pin the app if it loses focus during the exam
            startLockTask()
        }
    }

    inner class WebAppInterface(private val context: MainActivity) {
        @JavascriptInterface
        fun unlock() {
            examInProgress = false
            runOnUiThread {
                stopLockTask()
            }
        }

        @JavascriptInterface
        fun redirect(url: String) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }
    }
}