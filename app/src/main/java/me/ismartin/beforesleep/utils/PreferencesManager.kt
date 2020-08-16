package me.ismartin.beforesleep.utils

import android.content.Context
import android.content.SharedPreferences

object PreferencesManager {

    private lateinit var prefs: SharedPreferences

    private const val PREFS_NAME = "hardware"

    const val DEACTIVATE_WIFI = "wifi"
    const val DEACTIVATE_BLUETOOTH = "bluetooth"

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun read(key: String, value: String): String? {
        return prefs.getString(key, value)
    }

    fun read(key: String, value: Boolean): Boolean? {
        return prefs.getBoolean(key, value)
    }

    fun write(key: String, value: String) {
        val prefsEditor: SharedPreferences.Editor = prefs.edit()
        with(prefsEditor) {
            putString(key, value)
            commit()
        }
    }

    fun write(key: String, value: Boolean) {
        val prefsEditor: SharedPreferences.Editor = prefs.edit()
        with(prefsEditor) {
            putBoolean(key, value)
            commit()
        }
    }

}