package me.ismartin.beforesleep.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import me.ismartin.beforesleep.utils.PreferencesManager
import me.ismartin.beforesleep.utils.PreferencesManager.DEACTIVATE_BLUETOOTH
import me.ismartin.beforesleep.utils.PreferencesManager.DEACTIVATE_WIFI

class MainViewModel: ViewModel() {

    var timeCountDown: MutableLiveData<String> = MutableLiveData()
    var timerState: MutableLiveData<String> = MutableLiveData()

    fun setTimeCountDown(timeString: String) = timeCountDown.postValue(timeString)
    fun setTimerState(state: String) = timerState.postValue(state)

    fun turnOffWiFi(state: Boolean){
        PreferencesManager.write(DEACTIVATE_WIFI, state)
    }
    fun turnOffBluetooth(state: Boolean){
        PreferencesManager.write(DEACTIVATE_BLUETOOTH, state)
    }
}