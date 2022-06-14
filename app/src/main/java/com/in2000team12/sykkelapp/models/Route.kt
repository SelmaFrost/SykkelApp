package com.in2000team12.sykkelapp.models

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Route (
    @SerializedName("id"            ) var id           : String?     = null,
    @SerializedName("coordinates" ) var coordinates : ArrayList<LatLng> = arrayListOf(),
    @SerializedName("properties"    ) var properties   : Properties? = Properties(),
    @SerializedName("weather") var weather: Weather? = null,
    @SerializedName("airQuality") var airQuality: Double = 0.0,
    @SerializedName("duration") var duration: Duration? = null,
    @SerializedName("distance") var distance: Distance? = null,
    @SerializedName("startAddress") var startAddress: String? = null,
    @SerializedName("endAddress") var endAddress: String? = null,
    @SerializedName("postCode") var postCode: String? = null,
    @SerializedName("favouriteStatus") var favouriteStatus: Boolean = false,
) : Parcelable

@Parcelize
data class Duration(var text: String, val value: Double): Parcelable

@Parcelize
data class Distance(var text: String, var value: Double): Parcelable