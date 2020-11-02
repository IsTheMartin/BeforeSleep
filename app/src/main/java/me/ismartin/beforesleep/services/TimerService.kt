package me.ismartin.beforesleep.services

import android.app.*
import me.ismartin.beforesleep.R
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import me.ismartin.beforesleep.MainApplication
import me.ismartin.beforesleep.hardware.BluetoothActions
import me.ismartin.beforesleep.hardware.ScreenActions
import me.ismartin.beforesleep.hardware.WiFiActions
import me.ismartin.beforesleep.utils.Constants.CHANNEL_ID
import me.ismartin.beforesleep.utils.Constants.COUNT_DOWN_FINISHED_BROADCAST
import me.ismartin.beforesleep.utils.Constants.COUNT_DOWN_TICK_BROADCAST
import me.ismartin.beforesleep.utils.Constants.NOTIFICATION_ID
import me.ismartin.beforesleep.utils.Constants.TIME_INTERVAL
import me.ismartin.beforesleep.utils.PreferencesManager
import me.ismartin.beforesleep.utils.PreferencesManager.DEACTIVATE_BLUETOOTH
import me.ismartin.beforesleep.utils.PreferencesManager.DEACTIVATE_SCREEN
import me.ismartin.beforesleep.utils.PreferencesManager.DEACTIVATE_WIFI
import me.ismartin.beforesleep.utils.TimerUtils

class TimerService : Service() {

    private val TAG = "TimerService"
    private var countDownTimer: CountDownTimer? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if(intent?.action == "Stop_Service"){
            sendBroadcast(Intent(COUNT_DOWN_FINISHED_BROADCAST))
            stopForeground(true)
        } else {
            var timerString = "00:00"

            createNotificationChannel(notificationManager)
            var notification = createNotification(timerString)

            intent?.extras?.getLong("alarmTime")?.let {
                countDownTimer = object : CountDownTimer(it, TIME_INTERVAL) {
                    override fun onFinish() {
                        finishService()
                    }

                    override fun onTick(p0: Long) {
                        timerString = TimerUtils.formatMillisToTimeString(p0)

                        notification = createNotification(timerString)
                        notificationManager.notify(NOTIFICATION_ID, notification)

                        sendBroadcast(Intent(COUNT_DOWN_TICK_BROADCAST).let { broadcastIntent ->
                            broadcastIntent.putExtra("currentTime", p0)
                        })
                    }
                }.start()
                startForeground(NOTIFICATION_ID, notification)
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationChannel.setSound(null, null)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun createNotification(timerString: String): Notification {
        val stopSelfIntent = Intent(this, TimerService::class.java)
        stopSelfIntent.setAction("Stop_Service")
        val stopSelfPendingIntent = PendingIntent.getService(this, 0, stopSelfIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Before Sleep")
            .setContentText(timerString)
            .setSmallIcon(R.drawable.ic_logo_notification)
            .setAutoCancel(true)
            .setSound(null)
            .addAction(R.drawable.ic_logo_notification, "Stop", stopSelfPendingIntent)
            .build()
    }

    private fun finishService() {
        sendBroadcast(Intent(COUNT_DOWN_FINISHED_BROADCAST))
        deactivateHardware()
        stopForeground(true)
    }

    private fun deactivateHardware() {
        Log.i(TAG, "deactivateHardware")
        MainApplication.appContext?.let { context ->
            PreferencesManager.read(DEACTIVATE_WIFI, false)?.let {
                if (it) {
                    WiFiActions(context).let { wifiActions ->
                        if (wifiActions.isWiFiActive())
                            wifiActions.turnOffWiFi()
                    }
                }
            }
            PreferencesManager.read(DEACTIVATE_BLUETOOTH, false)?.let {
                if (it) {
                    BluetoothActions(context).let { bluetoothActions ->
                        if (bluetoothActions.isBluetoothEnabled())
                            bluetoothActions.turnOffBluetooth()
                    }
                }
            }
            PreferencesManager.read(DEACTIVATE_SCREEN, false)?.let {
                if(it) {
                    ScreenActions(context).let { screenActions->
                        if(screenActions.isDeviceAdmin())
                            screenActions.turnOffScreen()
                    }
                }
            }
        }
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
