package me.ismartin.beforesleep.hardware

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build

class WiFiActions(
    context: Context
) {
    private val TAG = "WifiAction"
    private val mContext: Context = context
    private val wifiManager: WifiManager

    init {
        wifiManager =
            mContext.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

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

}