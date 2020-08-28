package com.example.basardemo

import android.Manifest
import com.example.basardemo.R
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.huawei.hms.maps.CameraUpdateFactory
import com.huawei.hms.maps.HuaweiMap
import com.huawei.hms.maps.MapView
import com.huawei.hms.maps.OnMapReadyCallback
import com.huawei.hms.maps.model.*
import com.huawei.hms.maps.model.CameraPosition
import com.huawei.hms.maps.util.LogM


class MapActivity  : AppCompatActivity(), OnMapReadyCallback {
    //HUAWEI map
    private var hmap: HuaweiMap? = null
    private var mMarker: Marker? = null
    private var mMapView: MapView? = null
    private var mCircle: Circle? = null
    private val REQUEST_CODE = 100

    private val RUNTIME_PERMISSIONS = arrayOf<String>(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET

    )

    companion object {
        private const val TAG = "MapViewDemoActivity"
        private const val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        LogM.d("Map Activity", "map onCreate:")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        if (!hasPermissions(this, *RUNTIME_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, RUNTIME_PERMISSIONS, REQUEST_CODE)
        }


        mMapView = findViewById(R.id.mapView)
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY)
        }

        (mMapView as MapView).onCreate(mapViewBundle)
        (mMapView as MapView).getMapAsync(this)

    }

    override fun onMapReady(map: HuaweiMap) {
        Log.d("Map Activity", "onMapReady: ")


        hmap = map
        hmap!!.isMyLocationEnabled = true


        val build = CameraPosition.Builder().target(LatLng(60.0, 60.0)).zoom(10f).build()

        val cameraUpdate = CameraUpdateFactory.newCameraPosition(build)
        hmap!!.animateCamera(cameraUpdate)
        hmap!!.setMaxZoomPreference(15F)
        hmap!!.setMinZoomPreference(2F)

        // mark can be add by HuaweiMap

        // mark can be add by HuaweiMap
        mMarker = hmap!!.addMarker(
            MarkerOptions().position(LatLng(60.0, 60.0))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.badge_ph))
                .clusterable(true)
        )

        mMarker!!.showInfoWindow()

        // circle can be add by HuaweiMap

        // circle can be add by HuaweiMap
        mCircle = hmap!!.addCircle(
            CircleOptions().center(LatLng(60.0, 60.0)).radius(5000.0)
                .fillColor(Color.GREEN)
        )
        mCircle!!.fillColor = Color.TRANSPARENT


    }
    override fun onStart() {
        super.onStart()
        mMapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mMapView?.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mMapView?.onDestroy()
    }

    override fun onPause() {
        mMapView?.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        mMapView?.onResume()
    }
    override fun onLowMemory() {
        super.onLowMemory()
        mMapView!!.onLowMemory()
    }
    private fun hasPermissions(context: Context, vararg permissions: String): Boolean {
        for (permission in permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }
}