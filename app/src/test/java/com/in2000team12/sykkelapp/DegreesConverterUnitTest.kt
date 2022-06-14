package com.in2000team12.sykkelapp

import com.in2000team12.sykkelapp.models.DirectionString
import com.in2000team12.sykkelapp.utils.DirectionsDegreesConverter
import junit.framework.Assert.assertEquals
import org.junit.Test

class DegreesConverterUnitTest {

    private val converter = DirectionsDegreesConverter

    @Test
    fun unitTestOne(){
        val expectedValue = DirectionString.NORTH

        val testValue = converter.directionsFromDegrees(20.0)

        assertEquals(
            expectedValue,
            testValue
        )
    }

    @Test
    fun unitTestTwo(){
        val expectedValue = DirectionString.SOUTH_EAST

        val testValue = converter.directionsFromDegrees(150.0)

        assertEquals(
            expectedValue,
            testValue
        )
    }
}