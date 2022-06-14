package com.in2000team12.sykkelapp.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.in2000team12.sykkelapp.viewmodels.MainActivityViewModel
import com.in2000team12.sykkelapp.R
import com.in2000team12.sykkelapp.adaptors.RouteAdapter
import com.in2000team12.sykkelapp.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity(){
    private lateinit var binding: ActivityMainBinding
    private val TAG = "MainActivity.kt"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val recyclerView = binding.recyclerView
        val bottomBar = binding.bottomNavigationView
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Setting text to Sykkeruter, in favourites it's "favoritter"
        binding.textViewRoutes.text = "SYKKELRUTER"

        val vm = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        recyclerView.adapter = RouteAdapter(emptyList())
        vm.routes.observe(this, Observer {
            recyclerView.adapter = RouteAdapter(it)
            setTheme(R.style.Theme_SykkelApp)
        })

        //when icons is pushed:
        bottomBar.selectedItemId = R.id.bike
        bottomBar.setOnItemSelectedListener{item ->
            when(item.itemId){
                R.id.bike -> {
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

    private fun switchToFavouriteActivity(){
        val switchIntent = Intent(this, FavouriteActivity::class.java)
        startActivity(switchIntent)
    }

    private fun switchToMapActivity() {
        val switchActivityIntent = Intent(this, MapsActivity::class.java)
        startActivity(switchActivityIntent)
    }
    /*private fun switchToMainActivity() {
        val switchActivityIntent = Intent(this, MainActivity::class.java)
        startActivity(switchActivityIntent)
    }*/
}