package com.rushless.safer

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

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

        // Add JavaScript interface for communication from web to app
        webView.addJavascriptInterface(WebAppInterface(this), "Android")

        // Handle the incoming deep link
        val data = intent.data
        if (data != null && data.scheme == "rushless" && data.host == "open") {
            val targetUrl = data.getQueryParameter("url")
            val token = data.getQueryParameter("token")

            if (targetUrl != null) {
                // Here you would typically use the token to authenticate
                // For this example, we'll just show it and load the URL.
                Toast.makeText(this, "Token: $token", Toast.LENGTH_LONG).show()

                // Load the target URL in the WebView
                webView.loadUrl(targetUrl)

                // Here you would "pin" or save the URL and token if needed
                // For example, using SharedPreferences
            } else {
                showError("Target URL not provided in the deep link.")
            }
        } else {
            showError("Invalid deep link.")
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        // Optionally, load a local error page or close the activity
        finish()
    }

    // To handle back press inside WebView
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}

/**
 * JavaScript Interface class. Methods in this class can be called from JavaScript.
 */
class WebAppInterface(private val activity: WebViewActivity) {

    @JavascriptInterface
    fun unlockApp() {
        // This function can be called from the website's JavaScript by using: Android.unlockApp()
        // We can run code on the UI thread to modify the app's UI
        activity.runOnUiThread {
            Toast.makeText(activity, "App Unlocked!", Toast.LENGTH_SHORT).show()
            // Add your unlock logic here, e.g., show a hidden button, navigate away, etc.
        }
    }

    @JavascriptInterface
    fun pinSession(url: String, token: String) {
        // Example of how the web could ask the app to pin the session
        activity.runOnUiThread {
            Toast.makeText(activity, "Session Pinned: $url", Toast.LENGTH_SHORT).show()
            // Save the url and token to SharedPreferences here
        }
    }
}
