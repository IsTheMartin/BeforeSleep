package me.ismartin.beforesleep.hardware

import android.bluetooth.BluetoothAdapter
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BluetoothActions @Inject constructor() {
    fun hasBluetooth(): Boolean {
        BluetoothAdapter.getDefaultAdapter()?: return false
        return true
    }

    fun isBluetoothEnabled(): Boolean {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        return bluetoothAdapter?.isEnabled?: false
    }

    fun turnOffBluetooth() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        bluetoothAdapter?.disable()
    }

    companion object {
        private const val TAG = "Bluetooth"
    }
}