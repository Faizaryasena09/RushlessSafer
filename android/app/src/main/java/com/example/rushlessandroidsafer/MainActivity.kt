package com.example.rushlessandroidsafer

import android.util.Log

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.net.URLDecoder

import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.ValueCallback
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private var examInProgress = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.action == Intent.ACTION_VIEW) {
            examInProgress = true
            setContentView(R.layout.activity_main)
            val webView: WebView = findViewById(R.id.webview)
            val unlockButton: Button = findViewById(R.id.unlock_button)

            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    url?.let {
                        if (it.contains("/courses/") && it.contains("/do")) {
                            unlockButton.visibility = View.GONE
                        } else {
                            unlockButton.visibility = View.VISIBLE
                        }
                    }
                }
            }
            webView.settings.javaScriptEnabled = true
            webView.settings.domStorageEnabled = true
            webView.settings.userAgentString = webView.settings.userAgentString + " ExamBrowser/1.0"
            webView.addJavascriptInterface(WebAppInterface(this), "Android")

            unlockButton.setOnClickListener { unlock() }

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
        fun postMessage(jsonString: String) {
            Log.d("WebAppInterface", "Received postMessage: $jsonString")
            try {
                val jsonObject = JSONObject(jsonString)
                val type = jsonObject.getString("type")
                if (type == "unlock") {
                    Log.d("WebAppInterface", "Calling context.unlock()")
                    context.unlock()
                } else if (type == "redirect") {
                    val redirectUrl = jsonObject.getString("url")
                    Log.d("WebAppInterface", "Calling context.redirect() with URL: $redirectUrl")
                    context.redirect(redirectUrl)
                }
            } catch (e: Exception) {
                Log.e("WebAppInterface", "Error parsing postMessage JSON", e)
                e.printStackTrace()
            }
        }

        fun unlock() {
            context.examInProgress = false
            context.runOnUiThread {
                context.stopLockTask()
            }
        }

        fun redirect(url: String) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        }
    }

    private fun unlock() {
        Log.d("MainActivity", "unlock() called. examInProgress: $examInProgress")
        examInProgress = false
        runOnUiThread {
            Log.d("MainActivity", "Calling stopLockTask()")
            stopLockTask()
        }
    }

    private fun redirect(url: String) {
        Log.d("MainActivity", "redirect() called with URL: $url")
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}