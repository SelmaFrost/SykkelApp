package com.in2000team12.sykkelapp.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.in2000team12.sykkelapp.*
import com.in2000team12.sykkelapp.adaptors.RouteAdapter
import com.in2000team12.sykkelapp.databinding.ActivityMainBinding
import com.in2000team12.sykkelapp.viewmodels.FavouriteActivityViewModel

class FavouriteActivity: AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val TAG = "FavouriteActivity.kt"
    private lateinit var vm: FavouriteActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val recyclerView = binding.recyclerView
        val bottomBar = binding.bottomNavigationView

        binding.textViewRoutes.text = "FAVORITTER"

        recyclerView.layoutManager = LinearLayoutManager(this)

        vm = ViewModelProvider(this)[FavouriteActivityViewModel::class.java]
        vm.updateFavourites()

        vm.favourites.observe(this) { favourites ->
            recyclerView.adapter = RouteAdapter(favourites)
        }

        bottomBar.selectedItemId = R.id.star
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
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume(){
        super.onResume()
        vm.updateFavourites()
    }

    private fun switchToMainActivity(){
        val switchIntent = Intent(this, MainActivity::class.java)
        startActivity(switchIntent)
    }

    private fun switchToMapActivity() {
        val switchActivityIntent = Intent(this, MapsActivity::class.java)
        startActivity(switchActivityIntent)
    }
}