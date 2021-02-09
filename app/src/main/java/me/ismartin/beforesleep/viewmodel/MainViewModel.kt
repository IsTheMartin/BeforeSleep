package me.ismartin.beforesleep.viewmodel

import android.content.Context
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import me.ismartin.beforesleep.data.repositories.PreferencesRepository
import me.ismartin.beforesleep.hardware.BluetoothActions
import me.ismartin.beforesleep.hardware.WiFiActions
import me.ismartin.beforesleep.services.TimerService
import me.ismartin.beforesleep.utils.HelperUtils


class MainViewModel @ViewModelInject constructor(
    private val bluetooth: BluetoothActions,
    private val wifi: WiFiActions,
    private val preferences: PreferencesRepository
) : ViewModel() {

    var timeCountDown: MutableLiveData<String> = MutableLiveData()
    var timerState: MutableLiveData<String> = MutableLiveData()
    var isServiceRunning = MutableLiveData<Boolean>()

    fun hasBluetooth() = bluetooth.hasBluetooth()
    fun canTurnOffWiFi() = wifi.canTurnOffWiFi()

    fun setTimeCountDown(timeString: String) = timeCountDown.postValue(timeString)
    fun setTimerState(state: String) = timerState.postValue(state)

    fun setDeactivateWiFiInPrefs(state: Boolean) =
        CoroutineScope(Dispatchers.IO + Job()).launch {
            preferences.setDeactivateWiFi(state)
        }

    fun setDeactivateBluetoothInPrefs(state: Boolean) =
        CoroutineScope(Dispatchers.IO + Job()).launch {
            preferences.setDeactivateBluetooth(state)
        }

    fun isDeactivatedWiFiFromPrefs(): Boolean = runBlocking {
            preferences.willDeactivateWiFi()
        }

    fun isTurnOffBluetooth(): Boolean = runBlocking {
        preferences.willDeactivateBluetooth()
    }

    fun checkIfServiceIsRunning(context: Context) {
        val isRunning = HelperUtils.isMyServiceRunning(context, TimerService::class.java)
        Log.i("VM", "service is running: $isRunning")
        isServiceRunning.value = isRunning
    }
}