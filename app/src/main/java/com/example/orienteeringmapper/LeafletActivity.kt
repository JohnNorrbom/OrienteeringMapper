package com.example.orienteeringmapper

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.webkit.WebViewClient
import android.widget.RadioButton

class LeafletActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private var lastCheckedRadioButton: RadioButton? = null
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_leaflet)
        webView = findViewById(R.id.main)
        webView.settings.javaScriptEnabled = true
        WebViewClient().also { webView.webViewClient = it }
        webView.loadUrl("file:///android_asset/leaflet_map.html")
        val btnPoint = findViewById<RadioButton>(R.id.btn_point)
        val btnLine = findViewById<RadioButton>(R.id.btn_line)
        val btnPolygon = findViewById<RadioButton>(R.id.btn_polygon)
        val btnControls = findViewById<RadioButton>(R.id.btn_controls)

        val buttons = listOf(btnPoint, btnLine, btnPolygon, btnControls)

        buttons.forEach { btn ->
            btn.setOnClickListener {
                if (lastCheckedRadioButton == btn) {
                    // Deselect if clicked again
                    btn.isChecked = false
                    lastCheckedRadioButton = null
                    callJavaScriptMode("none") // Turn off draw mode
                } else {
                    lastCheckedRadioButton?.isChecked = false
                    btn.isChecked = true
                    lastCheckedRadioButton = btn
                    // Call the corresponding JS function
                    when (btn.id) {
                        R.id.btn_point -> callJavaScriptMode("point")
                        R.id.btn_line -> callJavaScriptMode("line")
                        R.id.btn_polygon -> callJavaScriptMode("polygon")
                        R.id.btn_controls -> callJavaScriptMode("triangle")
                    }
                }
            }
        }

    }
    private fun callJavaScriptMode(mode: String) {
        webView.evaluateJavascript("toggleMode('$mode');", null)
    }
    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}


