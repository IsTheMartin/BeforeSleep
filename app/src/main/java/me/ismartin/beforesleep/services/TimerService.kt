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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import me.ismartin.beforesleep.data.repositories.PreferencesRepository
import me.ismartin.beforesleep.hardware.BluetoothActions
import me.ismartin.beforesleep.hardware.WiFiActions
import me.ismartin.beforesleep.utils.Constants
import me.ismartin.beforesleep.utils.TimerUtils
import javax.inject.Inject

@AndroidEntryPoint
class TimerService : Service() {

    @Inject
    lateinit var wifi: WiFiActions

    @Inject
    lateinit var bluetooth: BluetoothActions

    @Inject
    lateinit var preferences: PreferencesRepository

    private var countDownTimer: CountDownTimer? = null
    private lateinit var notificationManager: NotificationManager
    private lateinit var context: Context

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "Stop_Service") {
            sendBroadcast(Intent(Constants.COUNT_DOWN_FINISHED_BROADCAST))
            stopForeground(true)
        } else {
            var timerString = TIMER_INIT_STRING

            createNotificationChannel(notificationManager)
            var notification = createNotification(timerString)

            intent?.extras?.getLong("alarmTime")?.let {
                countDownTimer = object : CountDownTimer(it, Constants.TIME_INTERVAL) {
                    override fun onFinish() {
                        finishService()
                    }

                    override fun onTick(tick: Long) {
                        timerString = TimerUtils.formatMillisToTimeString(tick)

                        notification = createNotification(timerString)
                        notificationManager.notify(Constants.NOTIFICATION_ID, notification.build())

                        sendBroadcast(
                            Intent(Constants.COUNT_DOWN_TICK_BROADCAST)
                                .putExtra("currentTime", tick)
                        )
                    }
                }.start()
                startForeground(Constants.NOTIFICATION_ID, notification.build())
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
                Constants.CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationChannel.setSound(null, null)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun createNotification(timerString: String): NotificationCompat.Builder {
        val stopSelfIntent = Intent(context, TimerService::class.java).apply {
            action = "Stop_Service"
        }
        val stopSelfPendingIntent = PendingIntent.getService(
            context,
            0,
            stopSelfIntent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )

        return NotificationCompat.Builder(context, Constants.CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(timerString)
            .setSmallIcon(R.drawable.ic_logo_notification)
            .setSound(null)
            .addAction(R.drawable.ic_logo_notification, "Stop", stopSelfPendingIntent)
    }

    private fun finishService() {
        sendBroadcast(Intent(Constants.COUNT_DOWN_FINISHED_BROADCAST))
        deactivateHardware()
        stopForeground(true)
    }

    private fun deactivateHardware() {
        Log.i(TAG, "deactivating hardware")

        CoroutineScope(Dispatchers.IO + Job()).launch {
            if(preferences.willDeactivateWiFi())
                wifi.apply {
                    if(isWiFiActive())
                        turnOffWiFi()
                }
            if(preferences.willDeactivateBluetooth())
                bluetooth.apply {
                    if(isBluetoothEnabled())
                        turnOffBluetooth()
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

        private const val TIMER_INIT_STRING = "00:00"
        private const val TAG = "TimerService"
    }
}
