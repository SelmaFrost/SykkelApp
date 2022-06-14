package com.in2000team12.sykkelapp.models

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import org.parceler.Parcel



data class Routes (

    @SerializedName("type"           ) var type           : String?             = null,
    @SerializedName("features"       ) var features       : ArrayList<Features> = arrayListOf(),
    @SerializedName("totalFeatures"  ) var totalFeatures  : Int?                = null,
    @SerializedName("numberMatched"  ) var numberMatched  : Int?                = null,
    @SerializedName("numberReturned" ) var numberReturned : Int?                = null,
    @SerializedName("timeStamp"      ) var timeStamp      : String?             = null,
    @SerializedName("crs"            ) var crs            : Crs?                = Crs()

)

data class Geometry (

    @SerializedName("type"        ) var type        : String?                      = null,
    @SerializedName("coordinates" ) var coordinates : ArrayList<ArrayList<Double>> = arrayListOf()

)

@Parcelize
data class Properties (

    @SerializedName("objectid"          ) var objectid        : Int?    = null,
    @SerializedName("id"                ) var id              : Int?    = null,
    @SerializedName("rute"              ) var rute            : Int?    = null,
    @SerializedName("tillegg"           ) var tillegg         : String? = null,
    @SerializedName("tiltak"            ) var tiltak          : String? = null,
    @SerializedName("tid"               ) var tid             : String? = null,
    @SerializedName("gdb_geomattr_data" ) var gdbGeomattrData : String? = null

) : Parcelable

data class Features (

    @SerializedName("type"          ) var type         : String?     = null,
    @SerializedName("id"            ) var id           : String?     = null,
    @SerializedName("geometry"      ) var geometry     : Geometry = Geometry(),
    @SerializedName("geometry_name" ) var geometryName : String?     = null,
    @SerializedName("properties"    ) var properties   : Properties = Properties()

)

data class Properties2 (

    @SerializedName("name" ) var name : String? = null

)

data class Crs (

    @SerializedName("type"       ) var type       : String?     = null,
    @SerializedName("properties" ) var properties : Properties? = Properties()

)