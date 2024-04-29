package com.myapp.happyplaces.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.myapp.happyplaces.R
import com.myapp.happyplaces.databinding.ActivityMapBinding
import com.myapp.happyplaces.models.HappyPlace

class MapActivity : AppCompatActivity(), OnMapReadyCallback {


    private val binding by lazy {
        ActivityMapBinding.inflate(layoutInflater)
    }
    private var mapHappyPlace: HappyPlace? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)


        if (intent?.hasExtra("EXTRA_POSITION") == true) {
            mapHappyPlace = intent.getParcelableExtra("EXTRA_POSITION")
        }


        if (mapHappyPlace != null) {
            setSupportActionBar(binding.toolbarMap)

            supportActionBar!!.title = mapHappyPlace!!.title
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            binding.toolbarMap.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            val supportMapFragment : SupportMapFragment =
                supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

            supportMapFragment.getMapAsync(this)

        }
    }

    override fun onMapReady(googleMap: GoogleMap) {

        if(mapHappyPlace != null) {
            val position = LatLng(mapHappyPlace!!.latitude, mapHappyPlace!!.longitude)
            googleMap.addMarker(MarkerOptions().position(position).title(mapHappyPlace!!.location))
            val zoomMarker = CameraUpdateFactory.newLatLngZoom(position, 10f)
            googleMap.animateCamera(zoomMarker)
        }


    }
}