package com.in2000team12.sykkelapp.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.in2000team12.sykkelapp.R
import com.in2000team12.sykkelapp.models.Route
import com.in2000team12.sykkelapp.repositories.Cache
import com.in2000team12.sykkelapp.repositories.DataSource
import com.in2000team12.sykkelapp.utils.CoordinatesConverter
import kotlinx.coroutines.launch

class RouteInformationActivityViewModel(application: Application): AndroidViewModel(application) {
    private val context: Context by lazy { getApplication<Application>().applicationContext }
    private val ds = DataSource(context)
    private val TAG = "RouteInformationActivity.kt"

    val routeLiveData: MutableLiveData<Route> by lazy {
        MutableLiveData<Route>().also {

        }
    }

    val placeLiveData: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun setRoute(route: Route){
        routeLiveData.value = route
    }

    fun loadTemp(route: Route){
        viewModelScope.launch{
            ds.fetchWeatherForRoute(route)
            routeLiveData.postValue(routeLiveData.value)
        }
    }

    fun loadAirQuality(route: Route) {
        viewModelScope.launch {
            ds.fetchAirQualityForRoute(route)
            routeLiveData.postValue(routeLiveData.value)
        }
    }

    fun loadDistanceDuration(route:Route){
        viewModelScope.launch {
            ds.fetchRouteDistanceDuration(route)
            routeLiveData.postValue(routeLiveData.value)
        }
    }

    fun loadPlaces(route: Route, context: Context){
        viewModelScope.launch {
            val startAdress = CoordinatesConverter.placeFromCoordinates(route.coordinates[0].latitude, route.coordinates[0].longitude, context)
            placeLiveData.postValue(startAdress[1])
        }
    }

    fun handleFavourite(route: Route){
        var favIds = ""
        for(routex in Cache.favourites){
            favIds += "- ${routex.id}"
        }
        Log.e(TAG, "Count: ${Cache.favourites.size} Route in question: ${route.id} Routes in favourite: $favIds")
        for(fav in Cache.favourites){
            if(fav.id == route.id){
                Log.e(TAG, "Removing from favourites")
                Cache.removeFavourite(route)
                return
            }
        }
        Log.e(TAG, "Adding to favourites")
        Cache.addFavourite(route)
    }

    fun airQualityString(value:Double) : String {
        if (value > 400.0){
            return "Svært dårlig"
        }else if(value <= 400.0 && value > 200.0){
            return "Dårlig"
        }else if(value in 100.0..200.0){
            return "Moderat"
        }else{
            return "God"
        }
    }

    fun airQualityColor(quality: Double): Int{
        if(quality > 400){
            return R.color.badAir
        }else if(quality <= 400.0 && quality > 200.0){
            return R.color.midAir
        }else if(quality in 100.0..200.0){
            return R.color.yellowAir
        }else{
            return R.color.greenAir
        }
    }
}