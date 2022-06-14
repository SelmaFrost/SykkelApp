package com.in2000team12.sykkelapp

import com.in2000team12.sykkelapp.models.WeatherSummary
import com.in2000team12.sykkelapp.utils.WeatherConverter
import junit.framework.Assert.assertEquals
import org.junit.Test

class FromStringtoEnumUnitTest {

    private val converter = WeatherConverter

    @Test
    fun unitTestOne(){

        val expectedValue = WeatherSummary.SNOW

        val testValue = converter.summaryStringToEnum("sleetstorm")

        assertEquals(
            expectedValue,
            testValue
        )
    }

    @Test
    fun unitTestTwo(){

        val expectedValue = WeatherSummary.SUNNY

        val testValue = converter.summaryStringToEnum("clearsky")

        assertEquals(
            expectedValue,
            testValue
        )
    }
}