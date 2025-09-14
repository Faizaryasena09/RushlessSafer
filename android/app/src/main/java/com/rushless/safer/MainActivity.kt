package com.rushless.safer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check for a pinned session
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val pinnedUrl = prefs.getString(KEY_URL, null)
        val pinnedToken = prefs.getString(KEY_TOKEN, null)

        if (pinnedUrl != null && pinnedToken != null) {
            // If a session is pinned, go directly to WebViewActivity
            val intent = Intent(this, WebViewActivity::class.java).apply {
                putExtra(KEY_URL, pinnedUrl)
                putExtra(KEY_TOKEN, pinnedToken)
            }
            startActivity(intent)
            finish() // Close MainActivity so the user can't navigate back to it
        } else {
            // If no session is pinned, show the welcome screen
            setContentView(R.layout.activity_main)
        }
    }
}
