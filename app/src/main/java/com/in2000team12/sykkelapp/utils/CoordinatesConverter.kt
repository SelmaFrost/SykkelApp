package com.in2000team12.sykkelapp.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import com.in2000team12.sykkelapp.models.Route
import java.util.*

class CoordinatesConverter {
    companion object{
        fun placeFromCoordinates(lat: Double, long:Double, context: Context): List<String>{
            val mGeocoder = Geocoder(context, Locale.getDefault())
            //henter infoen fra koordinatene på ruten
            val addressList: List<Address> = mGeocoder.getFromLocation(lat,long, 1)
            val place = addressList[0].getAddressLine(0)
            val placeList = place.split(",")

            return placeList
        }
    }

}