package com.in2000team12.sykkelapp.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.os.Parcelable
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.maps.android.PolyUtil
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator
import com.in2000team12.sykkelapp.viewmodels.MapsActivityViewModel
import com.in2000team12.sykkelapp.R
import com.in2000team12.sykkelapp.adaptors.RouteInfoWindowAdapter
import com.in2000team12.sykkelapp.models.MarkerItem
import com.in2000team12.sykkelapp.repositories.DataSource
import org.parceler.Parcels


class MapsActivity : AppCompatActivity(), OnMapReadyCallback/*, GoogleMap.OnMarkerClickListener*/ {

    private lateinit var mMap: GoogleMap
    private lateinit var clusterManager: ClusterManager<MarkerItem>
    private val TAG = "MapsActivity.kt"
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private val PERMISSION_ID = 41
    private lateinit var vm: MapsActivityViewModel
    private lateinit var selectedRoutePolyline: Polyline
    private lateinit var routeInfoWindowAdapter: RouteInfoWindowAdapter

    private lateinit var ds: DataSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ds = DataSource(this)

        vm = ViewModelProvider(this)[MapsActivityViewModel::class.java]
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        getLastLocation()

        val bottomBar = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomBar.selectedItemId = R.id.map
        bottomBar.setOnItemSelectedListener{item ->
            when(item.itemId){
                R.id.bike -> {
                    switchToMainActivity()
                    true
                }
                R.id.map -> {
                    true
                }
                R.id.star -> {
                    switchToFavouriteActivity()
                    true
                }
                else -> false
            }
        }
        routeInfoWindowAdapter = RouteInfoWindowAdapter(this)

    }

    private fun switchToMainActivity() {
        val switchActivityIntent = Intent(this, MainActivity::class.java)
        startActivity(switchActivityIntent)
    }

    private fun switchToFavouriteActivity(){
        val switchIntent = Intent(this, FavouriteActivity::class.java)
        startActivity(switchIntent)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        Log.e(TAG, "GIKK INN I ONMAP")
        mMap = googleMap

        setUpClusterer()

        // Add a marker in Sydney and move the camera
        val oslo = LatLng(59.93, 10.75)
        mMap.moveCamera(CameraUpdateFactory.zoomTo(12.0f))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(oslo))
        mMap.setMinZoomPreference(6.0f)
        mMap.setMaxZoomPreference(20.0f)


        //Settings for ui til google maps
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true

        //Setting the map so that it doesn't go behind the bottom bar
        val bottomBar = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        Log.e(TAG, "BottomBar height: ${bottomBar.height}")

        //Having problems with the bottom bar height being 0 after switching to activity from RouteAdapter
        //Hardcoded the height as a(perhaps temporary) solution
        val paddingInDp = 60
        val scale = resources.displayMetrics.density
        val paddingInPx = (paddingInDp * scale + 0.5f).toInt()

        mMap.setPadding(0,0,0,paddingInPx)

        val selectedRoute = intent.getParcelableExtra<Parcelable>("selectedRoute")
        if(selectedRoute != null){
            vm.setSelectedRoute(Parcels.unwrap(selectedRoute))
        }

        //Draws all the routes to the map(without using Directions API)
        vm.routes.observe(this) {

            //Making custom marker icon
            val bikeBitmapDescriptor = bitmapDescriptorFromVector(applicationContext,
                R.drawable.ic_bike_red
            )

            //Adding markers for all routes
            for(route in it){
                clusterManager.addItem(MarkerItem(
                    route.coordinates[0].latitude,
                    route.coordinates[0].longitude,
                    route.duration?.text,
                    route.distance?.text,
                    route,
                    bikeBitmapDescriptor
                ))
            }
        }

        vm.selectedRouteDirections.observe(this){
            if(this::selectedRoutePolyline.isInitialized){
                selectedRoutePolyline.remove()
            }
            val polylineString = it.overview_polyline.points
            val decodedPoints = PolyUtil.decode((polylineString))

            selectedRoutePolyline = mMap.addPolyline(PolylineOptions().addAll(decodedPoints))
            // Tried to make the camera smoothly change position with animeCamera, but this is very laggy
            mMap.moveCamera(CameraUpdateFactory
                .newLatLngBounds(LatLngBounds(LatLng(it.bounds.southwest.lat, it.bounds.southwest.lng),
                LatLng(it.bounds.northeast.lat, it.bounds.northeast.lng)), 150))
        }
    }

    private fun setUpClusterer() {

        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        clusterManager = ClusterManager(this.baseContext, mMap)

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        mMap.setOnCameraIdleListener(clusterManager)
        mMap.setOnMarkerClickListener(clusterManager)
        mMap.setOnInfoWindowClickListener(clusterManager)

        //Setting custom renderer with custom icon for clusterItem Marker
        clusterManager.renderer = RouteMarkerRenderer()

        //clusterManager.markerCollection.setInfoWindowAdapter(RouteInfoWindowAdapter(this))


        clusterManager.setOnClusterItemClickListener{
            markerItem -> vm.setSelectedRoute(markerItem.getRoute())
            val infoWindow = routeInfoWindowAdapter.getInfoWindow(markerItem)

            // For our custom info window
            infoWindow.setOnClickListener {
                val switchActivityIntent = Intent(this, RouteInformationActivity::class.java).apply{
                    putExtra("selectedRouteId", markerItem.getRoute().id)
                }
                startActivity(switchActivityIntent)
            }
            val popupWindow = PopupWindow(infoWindow, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true)
            popupWindow.showAtLocation(LinearLayout(this), Gravity.TOP, 0, 0)
            Log.e(TAG, "Inne i markeritem")
            true //Disables the default info window
        }
    }



    inner class RouteMarkerRenderer : DefaultClusterRenderer<MarkerItem>(
        applicationContext,
        mMap,
        clusterManager
    ) {
        private val mIconGenerator = IconGenerator(applicationContext)
        private val markerIcon = bitmapDescriptorFromVector(applicationContext,
            R.drawable.ic_bike_red
        )
        private val mImageView: ImageView = ImageView(applicationContext)

        override fun onBeforeClusterItemRendered(markeritem: MarkerItem, markerOptions: MarkerOptions) {
            markerOptions
                .icon(markerIcon)
                .title(markeritem.title)
                .snippet(markeritem.snippet)
        }

        override fun onClusterItemUpdated(markerItem: MarkerItem, marker: Marker) {
            // Same implementation as onBeforeClusterItemRendered() (to update cached markers)
            marker.setIcon(markerIcon)
            marker.title = markerItem.title
        }

        init {
            mIconGenerator.setContentView(mImageView)
        }
    }

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(context, vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }



    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        // check if permissions are given
        if (checkPermissions()) {

            // check if location is enabled
            if (isLocationEnabled()) {
                // getting last
                // location from
                // FusedLocationClient
                // object
                mFusedLocationClient.lastLocation
                    .addOnCompleteListener { task ->
                        val location: Location? = task.result
                        if(location == null){
                            Log.e(TAG, "Requesting new location")
                            requestNewLocationData()
                        }else{
                            if(this::mMap.isInitialized){
                                mMap.isMyLocationEnabled = true
                                mMap.uiSettings.isMyLocationButtonEnabled = true
                            }
                            Log.e(TAG, "Current location: Lat: ${location.latitude}, Lon: ${location.longitude}")
                        }
                    }
            } else {
                Toast.makeText(this, "Please turn on" + " your location...", Toast.LENGTH_LONG)
                    .show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            // if permissions aren't available,
            // request for permissions
            requestPermissions()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {

        // Initializing LocationRequest
        // object with appropriate methods
        val mLocationRequest = LocationRequest.create()
        mLocationRequest.priority = PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 5
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        // setting LocationRequest
        // on FusedLocationClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        Looper.myLooper()?.let {
            mFusedLocationClient.requestLocationUpdates(
                mLocationRequest,
                mLocationCallback,
                it
            )
        }
    }

    private val mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {

            val mLastLocation: Location = locationResult.lastLocation
            Log.e(TAG, "Last location: Lat: ${mLastLocation.latitude} Lon: ${mLastLocation.longitude}")
            //Can now access current location with mLastLocation.getLatitude() and mLastLocation.getLongitude()
        }
    }

    // method to check for permissions
    private fun checkPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        // If we want background location
        // on Android 10.0 and higher,
        // use:
        // ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    // method to request for permissions
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ), PERMISSION_ID
        )
    }

    // method to check
    // if location is enabled
    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    // If everything is alright then
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_ID) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (checkPermissions()) {
            getLastLocation()
        }
    }
}