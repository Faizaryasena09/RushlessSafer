package com.example.rushlessandroidsafer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

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
            if (url != null) {
                webView.loadUrl(url)
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
            startLockTask()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (examInProgress && !hasFocus) {
            // Re-pin the app if it loses focus during the exam
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
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