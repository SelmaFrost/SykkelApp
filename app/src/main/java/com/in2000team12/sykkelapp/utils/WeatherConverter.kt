package com.in2000team12.sykkelapp.utils

import android.graphics.drawable.Drawable
import com.in2000team12.sykkelapp.R
import com.in2000team12.sykkelapp.models.WeatherSummary

class WeatherConverter {
    companion object{
        fun summaryStringToEnum(summaryString: String?): WeatherSummary{
            var summaryEnum = WeatherSummary.CLOUDY
            when(summaryString){
                "clearsky" -> summaryEnum = WeatherSummary.SUNNY
                "fair" -> summaryEnum = WeatherSummary.SUNNY
                "fog" -> summaryEnum = WeatherSummary.CLOUDY
                else -> {
                    if (summaryString != null) {
                        if(summaryString.contains("sleet")){summaryEnum = WeatherSummary.SNOW}
                        else if(summaryString.contains("rain")){summaryEnum = WeatherSummary.RAIN}
                        else if(summaryString.contains("snow")){summaryEnum = WeatherSummary.SNOW}
                        else if(summaryString.contains("partlycloudy")){summaryEnum = WeatherSummary.PARTLY_CLOUDY}
                        else if(summaryString.contains("cloud")){summaryEnum = WeatherSummary.CLOUDY}
                    }
                }
            }
            return summaryEnum
        }

        fun summaryEnumToIcon(summaryEnum: WeatherSummary): Int{
            val summaryIcon = when(summaryEnum){
                WeatherSummary.CLOUDY -> R.drawable.ic_cloud
                WeatherSummary.SUNNY -> R.drawable.ic_sunny
                WeatherSummary.PARTLY_CLOUDY -> R.drawable.fair_day
                WeatherSummary.RAIN -> R.drawable.ic_rain_placeholder
                WeatherSummary.SNOW -> R.drawable.ic_snow_placeholder
            }
            return summaryIcon
        }
    }
}