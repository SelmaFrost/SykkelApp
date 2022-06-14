package com.in2000team12.sykkelapp.models

import com.google.android.gms.maps.model.LatLng

data class DirectionsResponseDto(val routes: List<DirectionsRoute>, val status: String)

data class DirectionsRoute(val bounds: DirectionsBounds, val legs: List<DirectionsLeg>, val overview_polyline: DirectionsPolyline)

data class DirectionsPolyline(val points: String)

data class DirectionsBounds(val northeast: DirectionLatLng, val southwest: DirectionLatLng)

data class DirectionLatLng(val lat: Double, val lng: Double)

data class DirectionsLeg(val distance: DirectionsDistance, val duration: DirectionsDuration)

data class DirectionsDistance(val text: String, val value: Int)

data class DirectionsDuration(val text: String, val value: Int)