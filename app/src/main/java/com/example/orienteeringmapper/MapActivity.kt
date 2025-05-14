package com.example.orienteeringmapper

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.OnMapReadyCallback
import com.mapbox.geojson.*
import org.maplibre.android.maps.Style.*
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.geometry.LatLng
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import com.mapbox.geojson.FeatureCollection


class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(
            applicationContext,
            "",
            WellKnownTileServer.MapLibre
        )
        // Allow content under system bars
        enableEdgeToEdge()
        setContentView(R.layout.activity_map)

        // Initialize MapView
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        // Apply system-window insets as padding on the root
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            insets
        }
    }

    override fun onMapReady(map: MapLibreMap) {
        // Enable user gestures
        map.uiSettings.apply {
            isZoomGesturesEnabled = true
            isScrollGesturesEnabled = true
            isRotateGesturesEnabled = true
        }

        // Load a basic style from MapLibre demo tiles
        val emptyStyle = """
        {
            "version": 8,
            "name": "Empty",
            "sources": {},
            "layers": [],
            "center": [0, 0],
            "zoom": 2
        }
        """.trimIndent()

        map.setStyle(emptyStyle) { style ->

            val pointLayer = SymbolLayer("point-layer", "point-source").withProperties(
                iconImage("black-dot"),
                iconSize(1.0f),
                iconAllowOverlap(true)
            )
            style.addLayer(pointLayer)
            val bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888).apply {
                val canvas = Canvas(this)
                val paint = Paint().apply {
                    color = Color.BLACK
                    isAntiAlias = true
                }
                canvas.drawCircle(5f, 5f, 5f, paint)
            }
            style.addImage("black-dot", bitmap)

            val points = mutableListOf<Feature>()

            map.addOnMapClickListener { latLng ->
                val point = Point.fromLngLat(latLng.longitude, latLng.latitude)
                points.add(Feature.fromGeometry(point))
                true
            }
        }
    }

    // Forward lifecycle events to MapView
    override fun onStart() {
        super.onStart(); mapView.onStart()
    }

    override fun onResume() {
        super.onResume(); mapView.onResume()
    }

    override fun onPause() {
        mapView.onPause(); super.onPause()
    }

    override fun onStop() {
        mapView.onStop(); super.onStop()
    }

    override fun onLowMemory() {
        mapView.onLowMemory(); super.onLowMemory()
    }

    override fun onDestroy() {
        mapView.onDestroy(); super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}