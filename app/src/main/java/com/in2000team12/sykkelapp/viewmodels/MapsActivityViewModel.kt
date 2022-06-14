package com.in2000team12.sykkelapp.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import com.google.android.gms.maps.model.LatLng
import com.in2000team12.sykkelapp.repositories.Cache
import com.in2000team12.sykkelapp.models.DirectionsRoute
import com.in2000team12.sykkelapp.models.Route
import com.in2000team12.sykkelapp.repositories.DataSource


class MapsActivityViewModel(application: Application): AndroidViewModel(application) {
    private val context: Context by lazy { getApplication<Application>().applicationContext }
    private val ds = DataSource(context)
    private val TAG = "MapsActivityViewModel.kt"
    private val maxWaypoints = 27

    private val selectedRoute = MutableLiveData<Route>()
    private val selectedRoutes = MutableLiveData<MutableList<Route>>()

    init {
        selectedRoutes.value = mutableListOf()
    }

    val routes: LiveData<List<Route>> = liveData {
        emit(Cache.routes)
    }

    val selectedRouteDirections: LiveData<DirectionsRoute> = selectedRoute.switchMap {
        liveData {
            //Google Directions API takes maximum 25 waypoints(27 points total with origin and destination)
            //Therefore we need to check for this and handle it
            //Currently we just remove waypoints "randomly", we might want to make a better algorithm for this later
            emit(ds.fetchDirections(trimWayPoints(it.coordinates)))
        }
    }

    fun setSelectedRoute(route: Route){
        selectedRoute.value = route
    }

    private fun trimWayPoints(waypoints: ArrayList<LatLng>): ArrayList<LatLng>{
        var i = 1
        var trimmedWaypoints = waypoints.map{
            LatLng(it.latitude, it.longitude)
        }
        trimmedWaypoints = ArrayList(trimmedWaypoints)
        while(trimmedWaypoints.size > maxWaypoints){
            trimmedWaypoints.removeAt(i)
            i += 4
            if(i >= trimmedWaypoints.size){
                i = 3
            }
        }
        return trimmedWaypoints
    }
}