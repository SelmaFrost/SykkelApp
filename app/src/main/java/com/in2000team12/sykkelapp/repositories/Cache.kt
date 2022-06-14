package com.in2000team12.sykkelapp.repositories

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.in2000team12.sykkelapp.models.Distance
import com.in2000team12.sykkelapp.models.Duration
import com.in2000team12.sykkelapp.models.Route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object Cache {
    private const val TAG = "Cache.kt"
    lateinit var routes: List<Route>
    var favourites: MutableList<Route> = mutableListOf()
    private lateinit var filesDir: File
    private const val MIN_ROUTE_DISTANCE = 100

    suspend fun load(ds: DataSource, dir: File) {
        filesDir = dir
        routes = loadRoutes()
        if(routes.isEmpty()){
            routes = ds.fetchRoutes()
            for(route in routes){
                ds.fetchRouteDistanceDuration(route)
                ds.fetchPlaces(route)
            }
            writeRoutesToFile()
        }



        val favIds = loadFavourites()
        for (favId in favIds) {
            for (route in routes) {
                if (route.id == favId) {
                    favourites.add(route)
                }
            }
        }
    }

    private suspend fun writeRoutesToFile() {
        val filename = "routes.txt"
        val file = File(filesDir, filename)

        val notShortRoutes = mutableListOf<Route>()
        try {
            for (route in routes) {
                route.distance?.let {
                    if (it.value > MIN_ROUTE_DISTANCE) {
                        notShortRoutes.add(route)
                    }
                }
            }
        } catch (ioe: IOException) {
            Log.e(
                TAG,
                "Error when fetching API, maybe check your internet connection, ${ioe.message}"
            )
        }

        routes = notShortRoutes


        //Routes are written to file in the following format:
        //id; coordinates; durationtext; durationvalue; distancetext; distancevalue; startAddress; endAddress; postCode
        //For example: 22; 0.1525,67.515 0.214,56.134 1.42,40.334; 3min; 180; 3km; 3000; Haugveien 3; Tøyengata 24b; 0655
        var fileString = ""
        withContext(Dispatchers.Default){
            for (route in routes) {
                fileString += "${route.id};"
                for (coordinate in route.coordinates) {
                    fileString += " ${coordinate.latitude},${coordinate.longitude}"
                }
                fileString += "; ${route.duration?.text}; ${route.duration?.value}"
                fileString += "; ${route.distance?.text}; ${route.distance?.value}"
                fileString += "; ${route.startAddress}"
                fileString += "; ${route.endAddress}"
                fileString += "; ${route.postCode}"
                fileString += "\n" //Removing the last linebreak
            }
        }

        Log.e(TAG, "String written to file: $fileString")

        // Run catching is used to prevent warning from blocking calls in coroutines
        kotlin.runCatching {
            file.createNewFile()
        }
        withContext(Dispatchers.IO){
            file.writeText(fileString)
        }
    }

    private suspend fun loadRoutes(): List<Route> {
        val filename = "routes.txt"
        val file = File(filesDir, filename)

        //If it's the first time the users opens the app, it will write a file of all the routes
        //Could also make it so if the file hasn't been updated in x amount of time it will write it again
        if(!file.exists()){
            Log.e(TAG, "No route file present, fetchign and writing to file...")
            return emptyList()
        }

        val bufferedReader = file.bufferedReader()
        var routesString: List<String>
        withContext(Dispatchers.IO) {
            routesString = bufferedReader.readLines()
        }
        val routes: MutableList<Route> = mutableListOf()
        try {
            withContext(Dispatchers.Default) {
                for (routeString in routesString) {
                    val parts = routeString.split(";")
                    val id = parts[0]
                    val coordinates: MutableList<LatLng> = mutableListOf()
                    for (coordinate in parts[1].split(" ")) {
                        if (coordinate != "") {
                            val coordParts = coordinate.split(",")
                            coordinates.add(
                                LatLng(
                                    coordParts[0].toDouble(),
                                    coordParts[1].toDouble()
                                )
                            )
                        }
                    }

                    val durationText = parts[2]
                    val durationValue = parts[3].toDouble()
                    val distanceText = parts[4]
                    val distanceValue = parts[5].toDouble()
                    val startAddress = parts[6]
                    val endAddress = parts[7]
                    val postCode = parts[8]

                    val route = Route(
                        id,
                        coordinates as ArrayList<LatLng>,
                        null,
                        null,
                        0.0,
                        Duration(durationText, durationValue),
                        Distance(distanceText, distanceValue),
                        startAddress,
                        endAddress,
                        postCode
                    )
                    routes.add(route)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Route file corrupt, rewriting...")
            return emptyList()
        }

        return routes
    }

    private suspend fun loadFavourites(): List<String>{
        val favList: MutableList<String> = mutableListOf()
        val filename = "favourites.txt"
        val file = File(filesDir, filename)

        if(!file.exists()){
            file.createNewFile()
            return favList
        }

        var routeIds: List<String>
        withContext(Dispatchers.IO){
            val bufferedReader = file.bufferedReader()
            routeIds = bufferedReader.readLines()
        }

        for(routeId in routeIds){
            favList.add(routeId)
        }
        return favList
    }

    fun isLoaded(): Boolean {
        if (this::routes.isInitialized && routes.isNotEmpty()) {
            return true
        }
        return false
    }

    fun getRoute(id: String): Route {
        for(route in routes){
            if(route.id == id){
                return route
            }
        }
        Log.e(TAG, "Tried to find a route that was not in cache, string received was $id, returning first route")
        return routes.first()
    }

    private fun saveFavourites() {
        Log.e(TAG, "Saving favourites to disk")
        val filename = "favourites.txt"
        val file = File(filesDir, filename)
        if (!file.exists()) {
            kotlin.runCatching {
                file.createNewFile()
            }
        }
        kotlin.runCatching {
            FileOutputStream(file, false).bufferedWriter().use { writer ->
                for (route in favourites) {
                    writer.append(route.id + "\n")
                }
            }
        }
    }

    fun removeFavourite(route: Route) {
        val toBeRemoved = mutableListOf<Route>()
        for (fav in favourites) {
            if (fav.id == route.id) {
                toBeRemoved.add(fav)
            }
        }
        for (remv in toBeRemoved) {
            favourites.remove(remv)
        }
        saveFavourites()
        Log.e(TAG, "Removed favourite")
    }

    fun addFavourite(route: Route) {
        if (route in favourites) {
            Log.e(TAG, "Tried to add a route that was already in favourites")
        } else {
            favourites.add(route)
        }
        saveFavourites()
        Log.e(TAG, "Fav list $favourites")
    }

    fun isFavourite(route: Route): Boolean {
        for (ptr in favourites) {
            if (ptr.id == route.id) {
                if(route !in favourites){
                    Log.e(TAG, "Route id was is favourite, but not the object")
                }
                return true
            }
        }
        return false
    }
}