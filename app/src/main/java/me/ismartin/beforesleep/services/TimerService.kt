package me.ismartin.beforesleep.services

import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import androidx.core.app.NotificationCompat
import me.ismartin.beforesleep.utils.Constants.CHANNEL_ID
import me.ismartin.beforesleep.utils.Constants.COUNT_DOWN_FINISHED_BROADCAST
import me.ismartin.beforesleep.utils.Constants.COUNT_DOWN_TICK_BROADCAST
import me.ismartin.beforesleep.utils.Constants.NOTIFICATION_ID
import me.ismartin.beforesleep.utils.Constants.TIME_INTERVAL
import me.ismartin.beforesleep.utils.TimerUtils


const val TAG = "TimerService"

class TimerService : Service() {

    var countDownTimer: CountDownTimer? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        var timerString = "00:00"
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(notificationManager)
        var notification = createNotification(timerString)

        intent?.extras?.getLong("alarmTime")?.let {
            countDownTimer = object : CountDownTimer(it, TIME_INTERVAL) {
                override fun onFinish() {
                    sendBroadcast(Intent(COUNT_DOWN_FINISHED_BROADCAST))
                    stopForeground(true)
                }

                override fun onTick(p0: Long) {
                    timerString = TimerUtils.formatMillisToTimeString(p0)

                    notification = createNotification(timerString)
                    notificationManager.notify(NOTIFICATION_ID, notification)

                    sendBroadcast(Intent(COUNT_DOWN_TICK_BROADCAST).let{ broadcastIntent ->
                        broadcastIntent.putExtra("currentTime", p0)
                    })
                }
            }.start()

            startForeground(NOTIFICATION_ID, notification)
        }


        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
        }
    }

    private fun createNotification(timerString: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.alert_dark_frame)
            .setContentTitle("Before Sleep")
            .setContentText(timerString)
            .setAutoCancel(true)
            .build()
    }

    companion object {
        @JvmStatic
        fun start(context: Context, alarmTime: Long) {
            Intent(context, TimerService::class.java).let {
                it.putExtra("alarmTime", alarmTime)
                context.startService(it)
            }
        }

        @JvmStatic
        fun stop(context: Context) {
            Intent(context, TimerService::class.java).let {
                context.stopService(it)
            }
        }
    }
}
