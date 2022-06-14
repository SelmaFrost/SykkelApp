package com.in2000team12.sykkelapp.adaptors

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.libraries.places.api.Places
import com.in2000team12.sykkelapp.R
import com.in2000team12.sykkelapp.activities.RouteInformationActivity
import com.in2000team12.sykkelapp.models.Route


class RouteAdapter (private val routes: List<Route>) : RecyclerView.Adapter<RouteAdapter.ViewHolder>() {
    private lateinit var  context: Context

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val routeStart: TextView = itemView.findViewById(R.id.routeStart)
        val routeEnd: TextView = itemView.findViewById(R.id.routeEnd)
        val distance: TextView = itemView.findViewById(R.id.txtViewDistance)
        val time: TextView = itemView.findViewById(R.id.txtViewTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.route_element, parent, false)
        context = parent.context

        //retrieving api key
        val ai: ApplicationInfo = context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        val value = ai.metaData["com.google.android.geo.API_KEY"]
        val apiKey = value.toString()

        if (!Places.isInitialized()) {
            Places.initialize(context, apiKey)
        }

        return ViewHolder(view)
    }


    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val route = routes[position]

        viewHolder.routeStart.text = route.startAddress
        viewHolder.routeEnd.text = route.endAddress
        viewHolder.distance.text = route.distance?.text
        viewHolder.time.text = route.duration?.text?.dropLast(1)

        viewHolder.itemView.setOnClickListener {
            val switchActivityIntent = Intent(context, RouteInformationActivity::class.java).apply{
                putExtra("selectedRouteId", route.id)
            }
            context.startActivity(switchActivityIntent)
        }
    }

    override fun getItemCount(): Int {
        return routes.size

    }
}