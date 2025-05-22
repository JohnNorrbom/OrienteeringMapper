package com.example.orienteeringmapper

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.RadioButton
import android.util.Log

class LeafletActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private var lastCheckedRadioButton: RadioButton? = null
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_leaflet)
        // Retrieve optional SVG path extra
        val svgPath = intent.getStringExtra("EXTRA_SVG_PATH")
        webView = findViewById(R.id.main)
        webView.settings.apply {
            javaScriptEnabled = true
            allowFileAccess = true
        }
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                svgPath?.let { path ->
                    // Add SVG overlay and zoom map to its bounds
                    val js = """
                        (function() {
                          var svgOverlay = L.imageOverlay('file://$path', map.getBounds()).addTo(map);
                          map.fitBounds(svgOverlay.getBounds());
                        })();
                    """.trimIndent()
                    Log.d("SVG", "$path")
                    webView.evaluateJavascript(js, null)
                }
            }
        }
        webView.loadUrl("file:///android_asset/leaflet_map.html")
        val btnPoint = findViewById<RadioButton>(R.id.btn_point)
        val btnLine = findViewById<RadioButton>(R.id.btn_line)
        val btnPolygon = findViewById<RadioButton>(R.id.btn_polygon)
        val btnControls = findViewById<RadioButton>(R.id.btn_controls)
        val buttons = listOf(btnPoint, btnLine, btnPolygon, btnControls)
        buttons.forEach { btn ->
            btn.setOnClickListener {
                if (lastCheckedRadioButton == btn) {
                    btn.isChecked = false
                    lastCheckedRadioButton = null
                    callJavaScriptMode("none")
                } else {
                    lastCheckedRadioButton?.isChecked = false
                    btn.isChecked = true
                    lastCheckedRadioButton = btn
                    when (btn.id) {
                        R.id.btn_point   -> callJavaScriptMode("point")
                        R.id.btn_line    -> callJavaScriptMode("line")
                        R.id.btn_polygon -> callJavaScriptMode("polygon")
                        R.id.btn_controls-> callJavaScriptMode("triangle")
                    }
                }
            }
        }
    }
    private fun callJavaScriptMode(mode: String) {
        webView.evaluateJavascript("toggleMode('$mode');", null)
    }
    @Deprecated("Use OnBackPressedDispatcher instead")
    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }
}