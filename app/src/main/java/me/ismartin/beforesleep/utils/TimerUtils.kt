package me.ismartin.beforesleep.utils

import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit

class TimerUtils {

    companion object {
        @JvmStatic
        fun convertStringToMillis(timeString: String): Long{
             return timeString.toLong() * 1000 * 60
        }

        @JvmStatic
        fun formatMillisToTimeString(timeInMillis: Long): String {
            val minutes = TimeUnit.MILLISECONDS.toMinutes(timeInMillis)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(timeInMillis) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeInMillis))
            val minutesInString = if(minutes < 10) "0$minutes" else minutes.toString()
            val secondsInString = if(seconds < 10) "0$seconds" else seconds.toString()
            return "$minutesInString:$secondsInString"
        }
    }

}