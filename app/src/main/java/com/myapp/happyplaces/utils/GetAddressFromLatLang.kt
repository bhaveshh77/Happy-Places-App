package com.myapp.happyplaces.utils

import android.content.Context
import android.location.Geocoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.lang.StringBuilder
import java.util.Locale

class GetAddressFromLatLang(context: Context, private val latitude : Double, private val longitude : Double) {

    private val geocoder = Geocoder(context, Locale.getDefault())
    suspend fun getAddress() : String {
        val result = StringBuilder()

         try {
            withContext(Dispatchers.IO) {
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)

                if (addresses != null && addresses.isNotEmpty()) {

                    for (i in 0 .. addresses[0].maxAddressLineIndex) {
                        val sb  = addresses[0].getAddressLine(i)

                        result.append(sb)
                    }
                } else {
                    result.append("No Address Found")
                }

                return@withContext result

            }
        } catch (e : IOException) {
            e.printStackTrace()
        }

        return result.toString()

    }

    interface AddressListener {
        fun getAddress(result : String)
        fun onError()
    }
}