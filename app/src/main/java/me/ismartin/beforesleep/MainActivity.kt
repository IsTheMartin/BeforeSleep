package me.ismartin.beforesleep

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import me.ismartin.beforesleep.services.TimerService
import me.ismartin.beforesleep.utils.Constants.COUNT_DOWN_FINISHED_BROADCAST
import me.ismartin.beforesleep.utils.Constants.COUNT_DOWN_TICK_BROADCAST
import me.ismartin.beforesleep.utils.TimerUtils

const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        registerReceiver(countDownTick, IntentFilter(COUNT_DOWN_TICK_BROADCAST))
        registerReceiver(countDownFinished, IntentFilter(COUNT_DOWN_FINISHED_BROADCAST))
        btnActionTimer.setOnClickListener(this)
    }

    private fun startTimer(){
        val milliseconds = TimerUtils.convertStringToMillis(etTime.text.toString())
        TimerService.start(this, milliseconds)
    }

    private fun stopTimer(){
        TimerService.stop(this)
        resetTimer()
    }

    private val countDownTick = object: BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
            val currentTimeInMillis = p1?.extras?.getLong("currentTime")
            //println("Tick: $currentTimeInMillis")
            etTime.setText(currentTimeInMillis.toString())
        }
    }

    private val countDownFinished = object: BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
            resetTimer()
            //println("Timer finished")
        }
    }

    override fun onClick(view: View?) {
        view?.let{
            when(it.id){
                btnActionTimer.id -> {
                    actionTimer()
                }
            }
        }
    }

    private fun actionTimer(){
        if(btnActionTimer.text == getString(R.string.start_timer)){
            btnActionTimer.text = getString(R.string.stop_timer)
            startTimer()
        } else {
            btnActionTimer.text = getString(R.string.start_timer)
            stopTimer()
        }
    }

    private fun resetTimer(){
        btnActionTimer.text = getString(R.string.start_timer)
        etTime.setText("")
    }
}