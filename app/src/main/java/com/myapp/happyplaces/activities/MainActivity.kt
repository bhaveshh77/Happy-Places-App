package com.myapp.happyplaces.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.myapp.happyplaces.adapters.HappyPlaceAdapter
import com.myapp.happyplaces.database.DatabaseHandler
import com.myapp.happyplaces.databinding.ActivityMainBinding
import com.myapp.happyplaces.models.HappyPlace
import com.myapp.happyplaces.swipes.SwipeToDeleteCallback
import com.myapp.happyplaces.swipes.SwipeToEditCallback
import java.util.ArrayList

class MainActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private lateinit var addHappyPlaceLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.fbAdd.setOnClickListener {
            val intent = Intent(this, HappyPlaceActivity::class.java)
            addHappyPlaceLauncher.launch(intent)
        }


        addHappyPlaceLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if  (result.resultCode == Activity.RESULT_OK) {
                // Handle data added successfully, and refresh the UI
                getHappyPlacesFromLocalDB()
            }
        }

        getHappyPlacesFromLocalDB()

        val editSwipeHandler = object : SwipeToEditCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                val adapter = binding.happyPlacesList.adapter as HappyPlaceAdapter
                adapter.notifyEditItem(this@MainActivity, viewHolder.adapterPosition, 2)

            }

        }

        val editItemTouchHandler = ItemTouchHelper(editSwipeHandler)
        editItemTouchHandler.attachToRecyclerView(binding.happyPlacesList)


        val deleteSwipeHandler = object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                val adapter = binding.happyPlacesList.adapter as HappyPlaceAdapter
                adapter.deleteItem(viewHolder.adapterPosition)

                getHappyPlacesFromLocalDB()
            }

        }

        val deleteItemTouchHandler = ItemTouchHelper(deleteSwipeHandler)
        deleteItemTouchHandler.attachToRecyclerView(binding.happyPlacesList)

    }

    private fun getHappyPlacesFromLocalDB() {
        val dbHandler = DatabaseHandler(this)
        val happyPlaces = dbHandler.getHappyPlacesList()

        if(happyPlaces.size > 0) {
            binding.happyPlacesList.visibility = View.VISIBLE
            binding.contentDescription.visibility = View.GONE
            showHappyPlaces(happyPlaces)
        } else {
            binding.happyPlacesList.visibility = View.GONE
            binding.contentDescription.visibility = View.VISIBLE
        }
    }

    private fun showHappyPlaces(happyPlaces: ArrayList<HappyPlace>) {

        binding.happyPlacesList.layoutManager = LinearLayoutManager(this)
        val happyPalacesAdapter = HappyPlaceAdapter(happyPlaces, applicationContext, this::onClickListener)
        binding.happyPlacesList.adapter = happyPalacesAdapter
        binding.happyPlacesList.setHasFixedSize(true)
    }

    private fun onClickListener(happyPlace: HappyPlace) {

        val intent = Intent(this@MainActivity, HappyPlaceDetailActivity::class.java)
        Log.d("IntentExtras", "Sending HappyPlace: $happyPlace")
        intent.putExtra("EXTRA_HAPPY_PLACE", happyPlace)
        startActivity(intent)
    }


}