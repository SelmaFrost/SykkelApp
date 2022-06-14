package com.in2000team12.sykkelapp.models

import java.util.*

data class WeatherResponseDto(
    val properties: WeatherProperties
)

data class WeatherProperties(val timeseries: List<WeatherTimeSeries>)

data class WeatherTimeSeries(
    //Might be problems with this as it's a string in json
    val time: Date,
    val data: WeatherData
)

data class WeatherData(
    val instant: WeatherDataInstant,
    val next_1_hours: WeatherDataNextHour
)

data class WeatherDataNextHour(val summary: WeatherDataNextHourSummary)

data class WeatherDataNextHourSummary(val symbol_code: String)

data class WeatherDataInstant(val details: WeatherDataDetails)

data class WeatherDataDetails(
    val air_temperature: Double,
    val precipitation_rate: Double,
    val wind_from_direction: Double,
    val wind_speed: Double,
    val wind_speed_of_gust: Double
)