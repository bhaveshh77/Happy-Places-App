package com.myapp.happyplaces.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.myapp.happyplaces.activities.HappyPlaceActivity
import com.myapp.happyplaces.activities.MainActivity
import com.myapp.happyplaces.database.DatabaseHandler
import com.myapp.happyplaces.databinding.ItemHappyPlaceBinding
import com.myapp.happyplaces.models.HappyPlace
import java.util.ArrayList

class HappyPlaceAdapter(private val happyPlaces : ArrayList<HappyPlace>, val context: Context, private val onClick : (HappyPlace) -> Unit) : RecyclerView.Adapter<HappyPlaceAdapter.HappyPlaceViewHolder> (){


    class HappyPlaceViewHolder(val binding : ItemHappyPlaceBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HappyPlaceViewHolder {

        return HappyPlaceViewHolder(ItemHappyPlaceBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount() = happyPlaces.size
    override fun onBindViewHolder(holder: HappyPlaceViewHolder, position: Int) {

        val happyPlace = happyPlaces[position]

        Glide.with(holder.itemView.context)
            .load(happyPlace.image)
            .into(holder.binding.ivPlaceImage)

        holder.binding.tvTitle.text = happyPlace.title
        holder.binding.tvDescription.text = happyPlace.description


        holder.itemView.setOnClickListener {
            onClick(happyPlace)
        }
    }

    fun notifyEditItem(activity: Activity, position: Int, requestCode : Int) {
        // Handle the update and initiation of the edit activity here
        // For example, you might remove the item from the list or update its data
        // and then initiate the edit activity.

        // Create an intent to start the edit activity
        val intent = Intent(context, HappyPlaceActivity::class.java)

        // Pass relevant data to the edit activity using intent extras
        intent.putExtra("EXTRA_POSITION", happyPlaces[position])

        // Start the edit activity
        activity.startActivityForResult(intent, requestCode)
        notifyItemChanged(position)
    }

    fun deleteItem(position: Int) {

        val dbHandler = DatabaseHandler(context)

       val isDeleted = dbHandler.deleteHappyPlace(happyPlaces[position].id)

        if(isDeleted > 0) {
            happyPlaces.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}