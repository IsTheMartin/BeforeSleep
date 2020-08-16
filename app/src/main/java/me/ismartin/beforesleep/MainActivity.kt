package me.ismartin.beforesleep

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_main.*
import me.ismartin.beforesleep.hardware.BluetoothActions
import me.ismartin.beforesleep.hardware.WiFiActions
import me.ismartin.beforesleep.services.TimerService
import me.ismartin.beforesleep.utils.Constants.COUNT_DOWN_FINISHED_BROADCAST
import me.ismartin.beforesleep.utils.Constants.COUNT_DOWN_TICK_BROADCAST
import me.ismartin.beforesleep.utils.Constants.TIMER_RUNNING
import me.ismartin.beforesleep.utils.Constants.TIMER_STOPPED
import me.ismartin.beforesleep.utils.PreferencesManager
import me.ismartin.beforesleep.utils.PreferencesManager.DEACTIVATE_BLUETOOTH
import me.ismartin.beforesleep.utils.PreferencesManager.DEACTIVATE_WIFI
import me.ismartin.beforesleep.utils.TimerUtils
import me.ismartin.beforesleep.viewmodel.MainViewModel

class MainActivity : AppCompatActivity(), View.OnClickListener,
    CompoundButton.OnCheckedChangeListener {

    private val TAG = "MainActivity"
    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        registerViewModelObserver()
        setListeners()
        loadHardwareValuesFromPreferences()
        verifyIfHardwareCanBeTurnedOff()
    }

    override fun onStart() {
        super.onStart()
        registerReceivers()
    }

    override fun onStop() {
        super.onStop()
        unregisterReceivers()
    }

    private fun registerReceivers() {
        registerReceiver(countDownTick, IntentFilter(COUNT_DOWN_TICK_BROADCAST))
        registerReceiver(countDownFinished, IntentFilter(COUNT_DOWN_FINISHED_BROADCAST))
    }

    private fun unregisterReceivers() {
        unregisterReceiver(countDownTick)
        unregisterReceiver(countDownFinished)
    }

    private fun registerViewModelObserver() {
        mainViewModel.timeCountDown.observe(this, Observer<String> { t -> etTime.setText(t) })
        mainViewModel.timerState.observe(this, Observer<String> { t -> updateUI(t) })
    }

    private fun setListeners() {
        btnActionTimer.setOnClickListener(this)
        chkWiFi.setOnCheckedChangeListener(this)
        chkBluetooth.setOnCheckedChangeListener(this)
    }

    private fun startTimer() {
        val milliseconds = TimerUtils.convertStringToMillis(etTime.text.toString())
        TimerService.start(this, milliseconds)
    }

    private fun stopTimer() {
        TimerService.stop(this)
    }

    private val countDownTick = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            p1?.extras?.getLong("currentTime")?.let { currentTimeInMillis ->
                mainViewModel.setTimeCountDown(
                    TimerUtils.formatMillisToTimeString(
                        currentTimeInMillis
                    )
                )
            }
            mainViewModel.setTimerState(TIMER_RUNNING)
        }
    }

    private val countDownFinished = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            stopTimer()
            mainViewModel.setTimerState(TIMER_STOPPED)
        }
    }

    override fun onClick(view: View?) {
        view?.let {
            when (it.id) {
                btnActionTimer.id -> {
                    if (btnActionTimer.text == getString(R.string.start_timer))
                        startTimer()
                    else
                        stopTimer()
                }
            }
        }
    }

    override fun onCheckedChanged(button: CompoundButton?, p1: Boolean) {
        button?.let {
            when (it.id) {
                chkWiFi.id -> PreferencesManager.write(DEACTIVATE_WIFI, it.isChecked)
                chkBluetooth.id -> PreferencesManager.write(DEACTIVATE_BLUETOOTH, it.isChecked)
            }
        }
    }

    private fun updateUI(state: String) {
        if (state == TIMER_RUNNING) {
            setUiOnRunningTimer()
        } else {
            setUiOnStoppedTimer()
        }
    }

    private fun setUiOnRunningTimer(){
        btnActionTimer.text = getString(R.string.stop_timer)
        etTime.isEnabled = false
    }

    private fun setUiOnStoppedTimer(){
        btnActionTimer.text = getString(R.string.start_timer)
        etTime.isEnabled = true
        etTime.setText("")
    }

    private fun verifyIfHardwareCanBeTurnedOff() {
        MainApplication.appContext?.let {
            if (!BluetoothActions(it).hasBluetooth()) {
                chkBluetooth.isEnabled = false
                chkBluetooth.isChecked = false
            }
            if (!WiFiActions(it).canTurnOffWiFi()) {
                chkWiFi.isEnabled = false
                chkWiFi.isChecked = false
            }
        }
    }

    private fun loadHardwareValuesFromPreferences(){
        PreferencesManager.read(DEACTIVATE_WIFI, false)?.let {
            chkWiFi.isChecked = it
        }
        PreferencesManager.read(DEACTIVATE_BLUETOOTH, false)?.let {
            chkBluetooth.isChecked = it
        }
    }

}