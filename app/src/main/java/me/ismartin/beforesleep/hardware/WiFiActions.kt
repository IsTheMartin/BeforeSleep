package me.ismartin.beforesleep.hardware

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WiFiActions @Inject constructor(
    @ApplicationContext context: Context
) {
    private val wifiManager: WifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    fun isWiFiActive(): Boolean {
        return wifiManager.isWifiEnabled
    }

    fun turnOffWiFi() {
        try {
            if (canTurnOffWiFi()) {
                wifiManager.isWifiEnabled = false
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun canTurnOffWiFi(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            return false
        return true
    }

    companion object {
        private const val TAG = "WiFi"
    }

}