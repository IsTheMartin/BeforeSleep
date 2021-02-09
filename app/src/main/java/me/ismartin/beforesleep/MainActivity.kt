package me.ismartin.beforesleep

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.CompoundButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint
import me.ismartin.beforesleep.databinding.ActivityMainBinding
import me.ismartin.beforesleep.services.TimerService
import me.ismartin.beforesleep.utils.Constants.COUNT_DOWN_FINISHED_BROADCAST
import me.ismartin.beforesleep.utils.Constants.COUNT_DOWN_TICK_BROADCAST
import me.ismartin.beforesleep.utils.Constants.MAX_TIMER_VALUE
import me.ismartin.beforesleep.utils.Constants.MIN_TIMER_VALUE
import me.ismartin.beforesleep.utils.Constants.TIMER_RUNNING
import me.ismartin.beforesleep.utils.Constants.TIMER_STOPPED
import me.ismartin.beforesleep.utils.TimerUtils
import me.ismartin.beforesleep.viewmodel.MainViewModel

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), View.OnClickListener,
    CompoundButton.OnCheckedChangeListener {

    private val TAG = "MainActivity"
    private lateinit var mainViewModel: MainViewModel
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        registerViewModelObserver()
        setListeners()

        loadHardwareValuesFromPreferences()
        prepareUi()
        mainViewModel.checkIfServiceIsRunning(this)
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
        mainViewModel.timeCountDown.observe(this, { t -> binding.etTime.setText(t) })
        mainViewModel.timerState.observe(this, { t -> updateUI(t) })
        mainViewModel.isServiceRunning.observe(this, { t ->
            if (t)
                setUiOnRunningTimer()
            else
                setUiOnStoppedTimer()
        })
    }

    private fun setListeners() {
        binding.btnActionTimer.setOnClickListener(this)
        binding.chkWiFi.setOnCheckedChangeListener(this)
        binding.chkBluetooth.setOnCheckedChangeListener(this)
    }

    private fun startTimer() {
        if (binding.etTime.text.isNotEmpty()) {
            val milliseconds = TimerUtils.convertStringToMillis(binding.etTime.text.toString())
            if (milliseconds < MIN_TIMER_VALUE)
                Toast.makeText(this, getString(R.string.min_time_message), Toast.LENGTH_SHORT)
                    .show()
            else if (milliseconds > MAX_TIMER_VALUE)
                Toast.makeText(this, getString(R.string.max_time_message), Toast.LENGTH_SHORT)
                    .show()
            else {
                TimerService.start(this, milliseconds)
                binding.etTime.filters = arrayOf(InputFilter.LengthFilter(10))
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
                binding.btnActionTimer.id -> {
                    if (binding.btnActionTimer.text == getString(R.string.start_timer))
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
                binding.chkWiFi.id -> mainViewModel.setDeactivateWiFiInPrefs(it.isChecked)
                binding.chkBluetooth.id -> mainViewModel.setDeactivateBluetoothInPrefs(it.isChecked)
                else -> Log.e(TAG, "id not recognized")
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
        binding.btnActionTimer.text = getString(R.string.stop_timer)
        binding.etTime.isEnabled = false
        binding.etTime.filters = arrayOf(InputFilter.LengthFilter(10))
    }

    private fun setUiOnStoppedTimer() {
        binding.btnActionTimer.text = getString(R.string.start_timer)
        binding.etTime.apply {
            isEnabled = true
            setText("")
            filters = arrayOf(InputFilter.LengthFilter(3))
        }
    }

    private fun loadHardwareValuesFromPreferences() {
        binding.chkWiFi.isChecked = mainViewModel.isDeactivatedWiFiFromPrefs()
        binding.chkBluetooth.isChecked = mainViewModel.isTurnOffBluetooth()
    }

    private fun prepareUi() {
        if (!mainViewModel.hasBluetooth())
            binding.chkBluetooth.apply {
                isEnabled = false
                isChecked = false
            }
        if (!mainViewModel.canTurnOffWiFi())
            binding.chkWiFi.apply {
                isEnabled = false
                isChecked = false
            }
        binding.etTime.setOnEditorActionListener(editorAction)
        binding.etTime.requestFocus()
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }

    private val editorAction = TextView.OnEditorActionListener { v, actionId, event ->
        if (actionId == EditorInfo.IME_ACTION_DONE)
            startTimer()
        true
    }

}