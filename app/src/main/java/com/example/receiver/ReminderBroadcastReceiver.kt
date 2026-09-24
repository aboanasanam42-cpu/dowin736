package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SmsManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.ClinicDatabase
import com.example.utils.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class ReminderBroadcastReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "clinic_reminders_channel"
        const val CHANNEL_NAME = "تنبيهات ورسائل عيادة الرحمن"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra(ReminderScheduler.EXTRA_REMINDER_ID, 0L)
        val patientName = intent.getStringExtra(ReminderScheduler.EXTRA_PATIENT_NAME) ?: "مريض"
        val patientPhone = intent.getStringExtra(ReminderScheduler.EXTRA_PATIENT_PHONE) ?: ""
        val message = intent.getStringExtra(ReminderScheduler.EXTRA_MESSAGE) ?: "تذكير بموعد وحساب عيادة الرحمن"

        // 1. Show Local Push Notification
        showNotification(context, reminderId.toInt(), patientName, message)

        // 2. Dispatch SMS if permission is granted
        if (patientPhone.isNotBlank() && ContextCompat.checkSelfPermission(context, android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            sendSmsDirectly(patientPhone, message)
        }

        // 3. Update Database and handle recurrence
        if (reminderId > 0L) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = ClinicDatabase.getDatabase(context)
                    val reminder = db.patientDao().getReminderById(reminderId)
                    if (reminder != null) {
                        if (reminder.repeatWeekly || reminder.repeatMonthly) {
                            val nextInterval = if (reminder.repeatWeekly) {
                                TimeUnit.DAYS.toMillis(7)
                            } else {
                                TimeUnit.DAYS.toMillis(30)
                            }
                            val nextTrigger = reminder.scheduledTimeMillis + nextInterval
                            val updatedReminder = reminder.copy(
                                scheduledTimeMillis = nextTrigger,
                                isSent = false
                            )
                            db.patientDao().updateReminder(updatedReminder)
                            ReminderScheduler.schedule(context, updatedReminder)
                        } else {
                            db.patientDao().updateReminder(reminder.copy(isSent = true, isActive = false))
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    private fun showNotification(context: Context, notifId: Int, patientName: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "قناة إرسال تنبيهات المواعيد والديون للمرضى"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notifId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("تذكير عيادة الرحمن: $patientName")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(if (notifId != 0) notifId else 101, notification)
    }

    private fun sendSmsDirectly(phone: String, text: String) {
        try {
            val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Application Context or system service for Android 12+
                SmsManager.getDefault()
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
            val parts = smsManager.divideMessage(text)
            if (parts.size > 1) {
                smsManager.sendMultipartTextMessage(phone, null, parts, null, null)
            } else {
                smsManager.sendTextMessage(phone, null, text, null, null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
