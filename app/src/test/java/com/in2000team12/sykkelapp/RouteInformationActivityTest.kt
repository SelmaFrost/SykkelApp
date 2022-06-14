package com.in2000team12.sykkelapp

import android.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.appcompat.app.AppCompatActivity
import junit.framework.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
//import org.junit.Test
/*import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test*/

class RouteInformationActivityTest{
    private val unitTestClass = UnitTestClass()

    @Test
    fun airQualityVeryBad() {

        val result = unitTestClass.airQualityString(500.0)
        assertEquals("Svært dårlig", result)
    }

    @Test
    fun airQualityBad() {
        val result = unitTestClass.airQualityString(300.0)
        assertEquals("Dårlig", result)
    }

    @Test
    fun airQualityModerate() {
        val result = unitTestClass.airQualityString(200.0)
        assertEquals("Moderat", result)
    }

    @Test
    fun airQualityGood() {
        val result = unitTestClass.airQualityString(50.0)
        assertEquals("God", result)

    }

    //private lateinit var binding: ActivityRouteInformationBinding

    /*@Test
    fun onCreate() {
        binding = ActivityRouteInformationBinding.inflate(layoutInflater)
        val textViewAirStatus = binding.textViewStatus
        val imageViewAir = binding.imageViewAir
    }

    @Test
    fun setColorAirQuality() {
        val qualityCheck = 35.0
        val routeInformation = RouteInformationActivity()
        //val resultVeryBad = routeInformation.setColorAirQuality(qualityCheck,imageViewAir, textViewAirStatus)

    }*/

}