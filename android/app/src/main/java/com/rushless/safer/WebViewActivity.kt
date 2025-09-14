package com.rushless.safer

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.net.URLDecoder

const val PREFS_NAME = "RushlessSaferPrefs"
const val KEY_URL = "pinned_url"
const val KEY_TOKEN = "pinned_token"

class WebViewActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)

        webView = findViewById(R.id.webView)

        // Basic WebView settings
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true // Needed for some websites

        // Set custom user agent
        val defaultUserAgent = webView.settings.userAgentString
        val customUserAgent = "$defaultUserAgent ExamBrowser/1.0"
        webView.settings.userAgentString = customUserAgent

        // Enable cookies
        CookieManager.getInstance().setAcceptCookie(true)

        // Add JavaScript interface for communication from web to app
        webView.addJavascriptInterface(WebAppInterface(this), "Android")

        // Set WebViewClient to handle navigation within the app
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // Keep the app open as long as we're on a /courses/x/do page
                if (url != null && (url.contains("/courses/") && url.contains("/do"))) {
                    // We're on the right page, keep the app open
                } else {
                    // Optionally handle navigation away from the exam page
                }
            }
        }

        // --- LOGIC TO HANDLE INCOMING INTENT ---
        val deepLinkData = intent.data
        if (deepLinkData != null) {
            when {
                // Handle rushless://open scheme
                deepLinkData.scheme == "rushless" && deepLinkData.host == "open" -> {
                    val targetUrl = deepLinkData.getQueryParameter("url")
                    val token = deepLinkData.getQueryParameter("token")

                    if (targetUrl != null && token != null) {
                        pinSession(targetUrl, token)
                        loadUrlWithToken(targetUrl, token)
                    } else {
                        showError("URL or Token not provided in the deep link.")
                    }
                }
                
                // Handle rushless-safer://exam scheme
                deepLinkData.scheme == "rushless-safer" && deepLinkData.host == "exam" -> {
                    val encodedUrl = deepLinkData.getQueryParameter("url")
                    val encodedCookies = deepLinkData.getQueryParameter("cookies")

                    if (encodedUrl != null) {
                        try {
                            val targetUrl = URLDecoder.decode(encodedUrl, "UTF-8")
                            val cookies = if (encodedCookies != null) URLDecoder.decode(encodedCookies, "UTF-8") else ""
                            
                            // Set cookies if provided
                            if (cookies.isNotEmpty()) {
                                val cookieManager = CookieManager.getInstance()
                                // Split cookies by semicolon and set each one
                                val cookieList = cookies.split(";").map { it.trim() }
                                for (cookie in cookieList) {
                                    if (cookie.isNotEmpty()) {
                                        cookieManager.setCookie(targetUrl, cookie)
                                    }
                                }
                                cookieManager.flush()
                            }
                            
                            // Load the URL
                            webView.loadUrl(targetUrl)
                        } catch (e: Exception) {
                            showError("Error processing URL: ${e.message}")
                        }
                    } else {
                        showError("URL not provided in the deep link.")
                    }
                }
                
                else -> {
                    showError("Unsupported deep link format.")
                }
            }
        } else {
            // Case 2: Opened from MainActivity (checking for a pinned session)
            val targetUrl = intent.getStringExtra(KEY_URL)
            val token = intent.getStringExtra(KEY_TOKEN)

            if (targetUrl != null && token != null) {
                loadUrlWithToken(targetUrl, token)
            } else {
                showError("Invalid session data.")
            }
        }
    }

    private fun loadUrlWithToken(url: String, token: String) {
        Toast.makeText(this, "Token: $token", Toast.LENGTH_LONG).show()
        webView.loadUrl(url)
    }

    private fun pinSession(url: String, token: String) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
        prefs.putString(KEY_URL, url)
        prefs.putString(KEY_TOKEN, token)
        prefs.apply()
        Toast.makeText(this, "Session Pinned!", Toast.LENGTH_SHORT).show()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        finish()
    }

    override fun onBackPressed() {
        // Only allow back navigation if we're not on a /courses/x/do page
        val currentUrl = webView.url
        if (currentUrl != null && (currentUrl.contains("/courses/") && currentUrl.contains("/do"))) {
            // On exam page, prevent back navigation
            Toast.makeText(this, "Anda tidak dapat meninggalkan halaman ujian", Toast.LENGTH_SHORT).show()
        } else {
            if (webView.canGoBack()) {
                webView.goBack()
            } else {
                super.onBackPressed()
            }
        }
    }
}

class WebAppInterface(private val activity: WebViewActivity) {

    @JavascriptInterface
    fun unlockApp() {
        activity.runOnUiThread {
            Toast.makeText(activity, "App Unlocked!", Toast.LENGTH_SHORT).show()
        }
    }

    @JavascriptInterface
    fun pinSession(url: String, token: String) {
        activity.runOnUiThread {
            Toast.makeText(activity, "Session Pinned: $url", Toast.LENGTH_SHORT).show()
            val prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            prefs.putString(KEY_URL, url)
            prefs.putString(KEY_TOKEN, token)
            prefs.apply()
        }
    }
}