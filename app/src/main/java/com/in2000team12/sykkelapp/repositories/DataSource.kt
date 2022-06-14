package com.in2000team12.sykkelapp.repositories

import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitString
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.in2000team12.sykkelapp.models.*
import com.in2000team12.sykkelapp.utils.DirectionsDegreesConverter
import com.in2000team12.sykkelapp.utils.UTM2Deg
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.util.*
import kotlin.collections.ArrayList


class DataSource(context: Context) {

    var googleApiKey: String?
    private val TAG = "DataSource.kt"
    private val gson = Gson()
    var contextT: Context = context

    init {
        //Henter google API Key fra metaData som er deklarert i AndroidManifest
        context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA).apply {
            googleApiKey = metaData.getString("com.google.android.geo.API_KEY")
            //Log.e(TAG, "Api key: $googleApiKey")
        }
    }

    suspend fun fetchPlaces(route: Route){
        val mGeocoder = Geocoder(contextT, Locale.getDefault())
        //henter infoen fra koordinatene på ruten

        var addressList: List<Address> = withContext(Dispatchers.IO){
            mGeocoder.getFromLocation(route.coordinates[0].latitude, route.coordinates[0].longitude, 1)
        }
        var place = addressList[0].getAddressLine(0)
        var placeList = place.split(",")
        val startAddress = placeList[0]
        val postCode = placeList[1]

        addressList = withContext(Dispatchers.IO){
            mGeocoder.getFromLocation(route.coordinates[route.coordinates.size-1].latitude, route.coordinates[route.coordinates.size-1].longitude, 1)
        }
        place = addressList[0].getAddressLine(0)
        placeList = place.split(",")
        val endAddress = placeList[0]

        route.startAddress = startAddress
        route.endAddress = endAddress
        route.postCode = postCode
    }

    //Loads all routes from BYM Oslo Kommune API
    //The waypoints are up to 1km apart from eachother, meaning directions API is needed to calculate the the path between these coordinates
    suspend fun fetchRoutes(): MutableList<Route>{
        val path = "https://geoserver.data.oslo.systems/geoserver/bym/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=bym%3Abyruter&outputFormat=application/json"

        val routes: MutableList<Route> = mutableListOf()
        try {
            val res = gson.fromJson(Fuel.get(path).awaitString(), Routes::class.java)

            //Changing format of route to make them easier to use
            for(i in 0 until res.features.size){
                val feature = res.features[i]
                val coordinates: ArrayList<LatLng> = arrayListOf()

                for(j in 0 until feature.geometry.coordinates.size) {
                    val latlng = UTM2Deg(feature.geometry.coordinates[j][0], feature.geometry.coordinates[j][1])
                    coordinates.add(
                        LatLng(
                            latlng.latitude ,
                            latlng.longitude
                        )
                    )
                }
                val route = Route(feature.id, coordinates, feature.properties)

                //Log.e(TAG, "Air quality for route id: ${route.id} coords: ${route.coordinates[0].latitude}, ${route.coordinates[0].longitude} is ${route.airQuality}")
                routes.add(route)
            }

        }catch (exception: Exception){
            Log.e(TAG, "Error when fetching route data, $exception")
        }
        //Log.e(TAG, ruter.toString())
        //Destructuring the response to a list of coordinates.
        return routes
    }

    //Returns the direction of a route, given a list of coordinates
    suspend fun fetchDirections(coords: List<LatLng>): DirectionsRoute {
        //Building the path for Directions API Request
        var path = "https://maps.googleapis.com/maps/api/directions/json?"
        path += "&mode=bicycling"

        val origin = coords[0]
        val destination = coords.last()
        val waypoints = coords.subList(1, coords.size-1)

        path += "&origin=${origin.latitude},${origin.longitude}&destination=${destination.latitude},${destination.longitude}"

        path += "&waypoints="
        for(latlng in waypoints){
            path += "${latlng.latitude},${latlng.longitude}|"
        }
        path.dropLast(1)

        path += "&key=${googleApiKey}"

        //Log.e(TAG, "Directions API fetch Path: $path")


        //Sending API Request, mapping to DirectionsResponseDto and returning it
        try {
            val res = Fuel.get(path).awaitString()
            val route = gson.fromJson(res, DirectionsResponseDto::class.java)
            if(route.status != "OK"){
                throw java.lang.Exception("Error from Directions API fetch, status: ${route.status}")
            }
            //Log.e(TAG, "bounds: ne: ${route.routes[0].bounds.northeast.lat}, ${route.routes[0].bounds.northeast.lng} - sw: ${route.routes[0].bounds.southwest.lat}, ${route.routes[0].bounds.southwest.lng}")
            //Log.e(TAG, "res polyline: ${route.routes[0].overview_polyline.points}")
            return route.routes[0]

        }catch (exception: Exception){
            Log.e(TAG, "Error when fetching route directions data, $exception")
        }
        Log.e(TAG, "Something went wrong, returning null from loadDirections")
        return DirectionsRoute(DirectionsBounds(DirectionLatLng(0.0, 0.0), DirectionLatLng(0.0, 0.0)), emptyList(), DirectionsPolyline(""))
    }

    //Updates the distance and duration attribute for the given route
    suspend fun fetchRouteDistanceDuration(route: Route){
        var path = "https://maps.googleapis.com/maps/api/distancematrix/json?"
        path += "destinations=${route.coordinates.last().latitude},${route.coordinates.last().longitude}"
        path += "&mode=bicycling"
        path += "&origins=${route.coordinates[0].latitude},${route.coordinates[0].longitude}"
        path += "&key=$googleApiKey"

        try {
            //path = "https://api.met.no/weatherapi/nowcast/2.0/complete?lat=59.9333&lon=10.7166"
            //Log.e(TAG, "Path from weather: $path")
            val res = Fuel.get(path).awaitString()
            val distanceMatrixDto = gson.fromJson(res, DistanceMatrixResponseDto::class.java)

            val durationDistance = distanceMatrixDto.rows[0].elements[0]
            if(durationDistance.status == "OK"){
                route.distance = Distance(durationDistance.distance.text, durationDistance.distance.value)
                route.duration = Duration(durationDistance.duration.text, durationDistance.duration.value)
            }else{
                throw Exception("Status of DirectionMatrix response is ${durationDistance.status}")
            }
        }catch (exception: Exception){
            Log.e(TAG, "Error when fetching route weather data, $exception")
        }
    }

    //Updates the weather attribute of the given route
    suspend fun fetchWeatherForRoute(route: Route){
        //Getting weather data for the start of route
        //For now we are only checking the weather at the start of the route
        //This is because the routes are so small it will make no practical difference
        var path = "https://api.met.no/weatherapi/nowcast/2.0/complete?"
            val startCoords = route.coordinates[0]
            path += "lat=${startCoords.latitude}"
            path += "&lon=${startCoords.longitude}"
            try {
                //path = "https://api.met.no/weatherapi/nowcast/2.0/complete?lat=59.9333&lon=10.7166"
                //Log.e(TAG, "Path from weather: $path")
                val res = Fuel.get(path).header("User-Agent", "https://github.uio.no/baldert/IN2000Team12 mavkot@hotmail.com").awaitString()
                val weatherDto = gson.fromJson(res, WeatherResponseDto::class.java)


                //Now we are checking the least recent forecast available
                //Could make a function that returns the index of the most recent available forecast
                val timesIndex = 0
                val details = weatherDto.properties.timeseries[timesIndex].data.instant.details
                val windDirection = DirectionsDegreesConverter.directionsFromDegrees(details.wind_from_direction)
                val weather = Weather(details.air_temperature, details.precipitation_rate, details.wind_speed, windDirection, weatherDto.properties.timeseries[timesIndex].data.next_1_hours.summary.symbol_code)
                route.weather = weather
            }catch (exception: Exception){
                Log.e(TAG, "Error when fetching route weather data, $exception")
            }
    }

    //Updates the air quality attribute of the given route
    suspend fun fetchAirQualityForRoute(route: Route){
        val radiusDistance = 2
        var path = "https://api.nilu.no/aq/utd/"
        path += "${route.coordinates[0].latitude}/"
        path += "${route.coordinates[0].longitude}/$radiusDistance"
        try {
            val res = Fuel.get(path).awaitString()
            val airQualityResponseDto = gson.fromJson(res, Array<AirQualityResponseDto>::class.java)
            route.airQuality = airQualityResponseDto[0].value
        }catch (exception: Exception){
            Log.e(TAG, "Error when fetching air quality data, $exception")
        }
    }


}