package com.myapp.happyplaces.activities

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.bumptech.glide.Glide
import com.myapp.happyplaces.R
import com.myapp.happyplaces.databinding.ActivityHappyPlaceDetailBinding
import com.myapp.happyplaces.databinding.ActivityMainBinding
import com.myapp.happyplaces.models.HappyPlace

class HappyPlaceDetailActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityHappyPlaceDetailBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Retrieve data from Intent
        val happyPlace: HappyPlace? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra("EXTRA_HAPPY_PLACE", HappyPlace::class.java)
        } else {
            intent?.getParcelableExtra("EXTRA_HAPPY_PLACE")
        }



        if(happyPlace != null) {
            setSupportActionBar(binding.toolBar)

            supportActionBar!!.title = happyPlace.title
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            binding.toolBar.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            Glide.with(applicationContext)
                .load(happyPlace.image)
                .into(binding.ivPlaceImage)
            binding.tvDescription.text = happyPlace.description
            binding.tvLocation.text = happyPlace.location

            binding.btnViewOnMap.setOnClickListener {
                val intent = Intent(this@HappyPlaceDetailActivity, MapActivity::class.java)
                intent.putExtra("EXTRA_PLACE_DETAILS", happyPlace)
                startActivity(intent)
            }

        }



    }
}