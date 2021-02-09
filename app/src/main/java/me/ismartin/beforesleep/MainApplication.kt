package me.ismartin.beforesleep

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
    }

    companion object{
        var appContext: Context? = null
    }
}