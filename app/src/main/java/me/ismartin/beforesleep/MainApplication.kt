package me.ismartin.beforesleep

import android.app.Application
import android.content.Context
import me.ismartin.beforesleep.utils.PreferencesManager

class MainApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        appContext?.let{
            PreferencesManager.init(it)
        }
    }

    companion object{
        var appContext: Context? = null
    }
}