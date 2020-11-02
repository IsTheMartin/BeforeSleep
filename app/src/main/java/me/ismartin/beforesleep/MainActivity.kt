package me.ismartin.beforesleep

import android.app.admin.DevicePolicyManager
import android.content.*
import android.os.Bundle
import android.text.InputFilter
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_main.*
import me.ismartin.beforesleep.administration.DeviceAdmin
import me.ismartin.beforesleep.hardware.BluetoothActions
import me.ismartin.beforesleep.hardware.WiFiActions
import me.ismartin.beforesleep.services.TimerService
import me.ismartin.beforesleep.utils.Constants.COUNT_DOWN_FINISHED_BROADCAST
import me.ismartin.beforesleep.utils.Constants.COUNT_DOWN_TICK_BROADCAST
import me.ismartin.beforesleep.utils.Constants.MAX_TIMER_VALUE
import me.ismartin.beforesleep.utils.Constants.MIN_TIMER_VALUE
import me.ismartin.beforesleep.utils.Constants.TIMER_RUNNING
import me.ismartin.beforesleep.utils.Constants.TIMER_STOPPED
import me.ismartin.beforesleep.utils.HelperUtils
import me.ismartin.beforesleep.utils.TimerUtils
import me.ismartin.beforesleep.viewmodel.MainViewModel


class MainActivity : AppCompatActivity(), View.OnClickListener,
    CompoundButton.OnCheckedChangeListener {

    private val TAG = "MainActivity"
    private lateinit var mainViewModel: MainViewModel
    private lateinit var localComponentName: ComponentName
    private lateinit var devicePolicyManager: DevicePolicyManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        localComponentName = ComponentName(this@MainActivity, DeviceAdmin::class.java)
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

        registerViewModelObserver()
        setListeners()
    }

    override fun onStart() {
        super.onStart()
        registerReceivers()
    }

    override fun onResume() {
        super.onResume()
        loadHardwareValuesFromPreferences()
        verifyIfHardwareCanBeTurnedOff()
        checkIfServiceIsRunning()
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
        chkScreen.setOnCheckedChangeListener(this)
    }

    private fun startTimer() {
        if (etTime.text.isNotEmpty()) {
            val milliseconds = TimerUtils.convertStringToMillis(etTime.text.toString())
            if (milliseconds < MIN_TIMER_VALUE)
                Toast.makeText(this, getString(R.string.min_time_message), Toast.LENGTH_SHORT)
                    .show()
            else if (milliseconds > MAX_TIMER_VALUE)
                Toast.makeText(this, getString(R.string.max_time_message), Toast.LENGTH_SHORT)
                    .show()
            else {
                TimerService.start(this, milliseconds)
                etTime.filters = arrayOf(InputFilter.LengthFilter(10))
            }

        } else {
            Toast.makeText(this, getString(R.string.empty_time), Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopTimer() {
        TimerService.stop(this)
        mainViewModel.setTimerState(TIMER_STOPPED)
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
                chkWiFi.id -> mainViewModel.turnOffWiFi(it.isChecked)
                chkBluetooth.id -> mainViewModel.turnOffBluetooth(it.isChecked)
                chkScreen.id -> {
                    if (chkScreen.isChecked && !devicePolicyManager.isAdminActive(localComponentName))
                        openDeviceAdminPermission()
                    else if (chkScreen.isChecked && devicePolicyManager.isAdminActive(localComponentName))
                        mainViewModel.turnOffScreen(it.isChecked)
                    else
                        mainViewModel.turnOffScreen(it.isChecked)
                }
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

    private fun setUiOnRunningTimer() {
        btnActionTimer.text = getString(R.string.stop_timer)
        etTime.isEnabled = false
    }

    private fun setUiOnStoppedTimer() {
        btnActionTimer.text = getString(R.string.start_timer)
        etTime.isEnabled = true
        etTime.setText("")
        etTime.filters = arrayOf(InputFilter.LengthFilter(3))
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

    private fun loadHardwareValuesFromPreferences() {
        mainViewModel.isTurnOffWiFi()?.let {
            chkWiFi.isChecked = it
        }
        mainViewModel.isTurnOffBluetooth()?.let {
            chkBluetooth.isChecked = it
        }
        mainViewModel.isTurnOffScreen()?.let {
            chkScreen.isChecked = it
        }
    }

    private fun openDeviceAdminPermission() {
        val deviceAdminIntent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, localComponentName)
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Hi! we need this permission for turn off your screen"
            )
        }
        startActivityForResult(deviceAdminIntent, 7001)
    }

    private fun checkIfServiceIsRunning() {
        if (!HelperUtils.isMyServiceRunning(this, TimerService::class.java)) {
            setUiOnStoppedTimer()
        } else {
            setUiOnRunningTimer()
        }
    }

}