package com.mau.alarmon

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.PeripheralManager
import java.io.IOException


@Suppress("DEPRECATION")
class MyBroadcastReceiver : BroadcastReceiver() {

    private lateinit var gpio: Gpio

    override fun onReceive(context: Context, intent: Intent) {
        val service = PeripheralManager.getInstance()
        val pinName = "BCM12" // Pin 12 en la Raspberry Pi

        try {
            gpio = service.openGpio(pinName)
            gpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)

            // Enciende el pin
            gpio.value = true

            // Espera 3 segundos
            Thread.sleep(3000)

            // Apaga el pin
            gpio.value = false

        } catch (e: IOException) {
            e.printStackTrace()
        }




        val i = Intent(context, MainActivity::class.java)
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        val pendingIntent = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_IMMUTABLE )
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(2000)

        val builder = NotificationCompat.Builder(context, "Notify")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentText("Alarm Reminders")
            .setContentText("Hey, Wake Up!")
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val notificationManagerCompat = NotificationManagerCompat.from(context)

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.VIBRATE
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        notificationManagerCompat.notify(200, builder.build())

        val sound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.packageName + "/" + R.raw.hola)
        val r = RingtoneManager.getRingtone(context, sound)
        r.play()
    }

    fun onDestroy() {
        try {
            gpio.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
