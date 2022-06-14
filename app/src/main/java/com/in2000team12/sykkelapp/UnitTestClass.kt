package com.in2000team12.sykkelapp

class UnitTestClass {

    fun airQualityString(value:Double) : String {
        if (value > 400.0){
            return "Svært dårlig"
        }else if(value <= 400.0 && value > 200.0){
            return "Dårlig"
        }else if(value in 100.0..200.0){
            return "Moderat"
        }else{
            return "God"
        }
    }

}