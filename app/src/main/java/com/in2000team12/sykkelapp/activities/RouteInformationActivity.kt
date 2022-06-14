package com.in2000team12.sykkelapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.in2000team12.sykkelapp.R
import com.in2000team12.sykkelapp.databinding.ActivityRouteInformationBinding
import com.in2000team12.sykkelapp.repositories.Cache
import com.in2000team12.sykkelapp.utils.WeatherConverter
import com.in2000team12.sykkelapp.viewmodels.RouteInformationActivityViewModel
import org.parceler.Parcels



class RouteInformationActivity : AppCompatActivity() {
    private lateinit var vm: RouteInformationActivityViewModel
    private lateinit var binding: ActivityRouteInformationBinding
    private val TAG = "RouteInformationActivity.kt"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRouteInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var selectedRouteId = intent.getStringExtra("selectedRouteId")
        if(selectedRouteId == null){
            selectedRouteId = ""
        }
        val route = Cache.getRoute(selectedRouteId)
        val textViewPlace = binding.textViewPlace
        val textViewTime = binding.textViewTime
        val textViewDistance = binding.textViewDistance
        val buttonSeeMap = binding.button
        val btnFavourite = binding.starButton
        val textViewTemp = binding.textViewDegrees
        val imageViewAir = binding.imageViewAir
        val imageViewWeather = binding.imageViewWeather
        val textViewAirStatus = binding.textViewStatus
        val textViewWindData = binding.textViewWindData
        val textViewRain = binding.textViewRainData
        val bottomBar = binding.bottomNavigationView

        if(route.favouriteStatus){
            btnFavourite.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.ic_starfilled))
        }else{
            btnFavourite.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.ic_star))
        }

        textViewPlace.text = route.postCode

        vm = ViewModelProvider(this)[RouteInformationActivityViewModel::class.java]
        vm.setRoute(route)
        vm.routeLiveData.observe(this) { liveDataRoute ->
            textViewTime.text = liveDataRoute.duration?.text?.dropLast(1)
            textViewDistance.text = liveDataRoute.distance?.text
            textViewTemp.text = "${liveDataRoute.weather?.temperature.toString()}\u2103"
            textViewWindData.text = "${liveDataRoute.weather?.windSpeed.toString()} m/s"
            textViewRain.text = liveDataRoute.weather?.precipitationRate.toString()
            setWeatherSummaryIcon(imageViewWeather, liveDataRoute.weather?.summary)
            setAirQuality(liveDataRoute.airQuality, imageViewAir, textViewAirStatus)
        }


        //henter temperatur, luftkvalitet, lengde og tid
        vm.loadTemp(route)
        vm.loadDistanceDuration(route)
        vm.loadAirQuality(route)

        buttonSeeMap.setOnClickListener{
            val switchActivityIntent = Intent(this, MapsActivity::class.java)
            switchActivityIntent.putExtra("selectedRoute", Parcels.wrap(route))
            startActivity(switchActivityIntent)
        }


        if(Cache.isFavourite(route)){
            btnFavourite.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.ic_starfilled))
        }else{
            btnFavourite.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.ic_star))
        }

        // Adds route to favourites or removes favourite if already in it
        btnFavourite.setOnClickListener{
            if(Cache.isFavourite(route)){
                vm.handleFavourite(route)
                btnFavourite.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.ic_star))
                val toast = Toast.makeText(applicationContext, "removed from favourites", Toast.LENGTH_SHORT)
                toast.show()
            }else{
                vm.handleFavourite(route)
                btnFavourite.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.ic_starfilled))
                val toast = Toast.makeText(applicationContext, "saved to favourites", Toast.LENGTH_SHORT)
                toast.show()
            }


        }

        bottomBar.setOnItemSelectedListener{item ->
            when(item.itemId){
                R.id.bike -> {
                    switchToMainActivity()
                    true
                }
                R.id.map -> {
                    switchToMapActivity()
                    true
                }
                R.id.star ->{
                    switchToFavouriteActivity()
                    true
                }
                else -> false
            }
        }
    }

    private fun setWeatherSummaryIcon(imageView: ImageView, summaryString: String?){
        val summaryEnum = WeatherConverter.summaryStringToEnum(summaryString)
        Log.e(TAG, "summaryString: $summaryString")

        val summaryIcon = WeatherConverter.summaryEnumToIcon(summaryEnum)
        imageView.setImageResource(summaryIcon)
    }

    private fun setAirQuality(value: Double, image: ImageView, textView: TextView) {
        //Log.e(TAG, "$value")
        val quality = vm.airQualityString(value)
        textView.text = quality
        val color = vm.airQualityColor(value)
        image.setColorFilter(ContextCompat.getColor(this, color))
    }

    private fun switchToFavouriteActivity(){
        val switchIntent = Intent(this, FavouriteActivity::class.java)
        startActivity(switchIntent)
    }

    private fun switchToMapActivity() {
        val switchActivityIntent = Intent(this, MapsActivity::class.java)
        startActivity(switchActivityIntent)
    }

    private fun switchToMainActivity() {
        val switchActivityIntent = Intent(this, MainActivity::class.java)
        startActivity(switchActivityIntent)
    }
}