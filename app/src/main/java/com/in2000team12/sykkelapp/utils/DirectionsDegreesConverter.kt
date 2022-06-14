package com.in2000team12.sykkelapp.utils

import com.in2000team12.sykkelapp.models.DirectionString

class DirectionsDegreesConverter {
    companion object{
        fun directionsFromDegrees(degrees: Double): DirectionString{
            if(degrees < 22.5 || degrees > 337.5){
                return DirectionString.NORTH
            }
            if(degrees < 67.5){
                return DirectionString.NORTH_EAST
            }
            if(degrees < 112.5){
                return DirectionString.EAST
            }
            if(degrees < 157.5){
                return DirectionString.SOUTH_EAST
            }
            if(degrees < 202.5){
                return DirectionString.SOUTH
            }
            if(degrees < 247.5){
                return DirectionString.SOUTH_WEST
            }
            if(degrees < 292.5){
                return DirectionString.WEST
            }
            return DirectionString.NORTH_WEST

        }
    }
}