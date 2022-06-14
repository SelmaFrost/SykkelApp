package com.in2000team12.sykkelapp.models

import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class MarkerItem(
    lat: Double,
    lng: Double,
    title: String?,
    snippet: String?,
    route: Route,
    icon: BitmapDescriptor?
) : ClusterItem {

    private val position: LatLng
    private val title: String?
    private val snippet: String?
    private val route: Route
    private val icon: BitmapDescriptor?

    override fun getPosition(): LatLng {
        return position
    }

    override fun getTitle(): String? {
        return title
    }

    override fun getSnippet(): String? {
        return snippet
    }

    fun getRoute(): Route{
        return route
    }

    init {
        position = LatLng(lat, lng)
        this.title = title
        this.snippet = snippet
        this.route = route
        this.icon = icon
    }
}