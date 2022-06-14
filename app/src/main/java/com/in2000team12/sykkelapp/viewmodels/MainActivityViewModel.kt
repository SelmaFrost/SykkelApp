package com.in2000team12.sykkelapp.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.in2000team12.sykkelapp.models.Route
import com.in2000team12.sykkelapp.repositories.Cache
import com.in2000team12.sykkelapp.repositories.DataSource

class MainActivityViewModel(application: Application): AndroidViewModel(application) {
    private val context: Context by lazy { getApplication<Application>().applicationContext }
    private val ds = DataSource(context)
    val routes: LiveData<List<Route>> = liveData {
        //ds.writeRoutesToFile(context.filesDir, ds.loadRoutes())
        if(!Cache.isLoaded()){
            Cache.load(ds, context.filesDir)
        }
        emit(Cache.routes)

        //emit(ds.loadRoutesFromFile(context.filesDir))
    }
}