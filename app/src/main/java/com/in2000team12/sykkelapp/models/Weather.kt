package com.in2000team12.sykkelapp.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Weather(
    @SerializedName("temperature") var temperature: Double = 0.0,
    @SerializedName("precipitationRate") var precipitationRate: Double = 0.0,
    @SerializedName("windSpeed") var windSpeed: Double = 0.0,
    @SerializedName("windDirection") var windDirection: DirectionString = DirectionString.NORTH,
    @SerializedName("summary") var summary: String = ""
): Parcelable