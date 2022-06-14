package com.in2000team12.sykkelapp.adaptors

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.in2000team12.sykkelapp.R
import com.in2000team12.sykkelapp.models.MarkerItem

class RouteInfoWindowAdapter(context: Context)/*: GoogleMap.InfoWindowAdapter*/ {
    private val TAG = "RouteInfoWindowAdapter.kt"
    private val mWindow: View = LayoutInflater.from(context).inflate(R.layout.route_info_window, null)

    fun getInfoWindow(markerItem: MarkerItem): View{
        val time = markerItem.title
        val distance = markerItem.snippet
        Log.e(TAG, "Kom hit med time: $time og distance $distance")
        val txtViewTime = mWindow.findViewById<TextView>(R.id.txtViewTime);
        val txtViewDistance = mWindow.findViewById<TextView>(R.id.txtViewDistance);
        txtViewTime.text = time
        txtViewDistance.text = distance
        mWindow.setOnClickListener {

        }
        return mWindow
    }
}