package me.ismartin.beforesleep.hardware

import android.bluetooth.BluetoothAdapter
import android.content.Context


class BluetoothActions(
    context: Context
) {
    private val TAG = "BTActions"
    private val mContext = context

    fun hasBluetooth(): Boolean {
        BluetoothAdapter.getDefaultAdapter() ?: return false
        return true
    }

    fun isBluetoothEnabled(): Boolean {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        bluetoothAdapter?.let {
            return it.isEnabled
        }
        return true
    }

    fun turnOffBluetooth() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        bluetoothAdapter?.let{
            it.disable()
        }
    }
}