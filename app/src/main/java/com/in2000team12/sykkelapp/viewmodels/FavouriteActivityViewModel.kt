package com.in2000team12.sykkelapp.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.in2000team12.sykkelapp.models.Route
import com.in2000team12.sykkelapp.repositories.Cache

class FavouriteActivityViewModel(application: Application): AndroidViewModel(application) {
    val favourites: MutableLiveData<List<Route>> by lazy {
        MutableLiveData<List<Route>>()
    }

    fun updateFavourites(){
        favourites.postValue(Cache.favourites)
    }
}