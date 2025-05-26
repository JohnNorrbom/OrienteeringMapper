package com.example.orienteeringmapper

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.RadioButton
import java.io.File

class LeafletActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private var lastCheckedRadioButton: RadioButton? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_leaflet)

        // Get paths from Intent
        val ocdPath = intent.getStringExtra("EXTRA_OCD_FILE")
        val svgPath = intent.getStringExtra("EXTRA_SVG_PATH")

        webView = findViewById(R.id.main)
        webView.settings.apply {
            javaScriptEnabled                = true
            allowFileAccess                  = true
            allowFileAccessFromFileURLs      = true
            allowUniversalAccessFromFileURLs = true
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?) =
                false

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                // 1) If an SVG overlay was passed, add it first
                svgPath?.let { path ->
                    val jsSvg = """
                      (function() {
                        var svgOverlay = L.imageOverlay('file://$path', map.getBounds()).addTo(map);
                        map.fitBounds(svgOverlay.getBounds());
                      })();
                    """.trimIndent()
                    Log.d("LeafletActivity", "Injecting SVG overlay: $path")
                    webView.evaluateJavascript(jsSvg, null)
                }

                // 2) If an OCAD file was imported, load & render it
                ocdPath?.let { path ->
                    try {
                        val bytes = File(path).readBytes()
                        val b64   = Base64.encodeToString(bytes, Base64.NO_WRAP)
                        val jsOcad = """
                          (function() {
                            loadOcadFromBase64("$b64");
                          })();
                        """.trimIndent()
                        Log.d("LeafletActivity", "Injecting OCAD file: $path")
                        webView.evaluateJavascript(jsOcad, null)
                    } catch (e: Exception) {
                        Log.e("LeafletActivity", "Failed to read OCAD file", e)
                    }
                }
            }
        }

        webView.loadUrl("file:///android_asset/leaflet_map.html")

        // 3) Wire up your draw-mode radio buttons
        val btnPoint   = findViewById<RadioButton>(R.id.btn_point)
        val btnLine    = findViewById<RadioButton>(R.id.btn_line)
        val btnPolygon = findViewById<RadioButton>(R.id.btn_polygon)
        val btnControls= findViewById<RadioButton>(R.id.btn_controls)

        listOf(btnPoint to "point",
            btnLine to "line",
            btnPolygon to "polygon",
            btnControls to "triangle"
        ).forEach { (btn, mode) ->
            btn.setOnClickListener {
                if (lastCheckedRadioButton == btn) {
                    btn.isChecked = false
                    lastCheckedRadioButton = null
                    callJavaScriptMode("none")
                } else {
                    lastCheckedRadioButton?.isChecked = false
                    btn.isChecked = true
                    lastCheckedRadioButton = btn
                    callJavaScriptMode(mode)
                }
            }
        }
    }

    private fun callJavaScriptMode(mode: String) {
        webView.evaluateJavascript("toggleMode('$mode');", null)
    }

    @Deprecated("Use OnBackPressedDispatcher instead")
    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack()
        else super.onBackPressed()
    }
}
