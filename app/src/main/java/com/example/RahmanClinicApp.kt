package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.data.ClinicDatabase
import com.example.data.ClinicRepository
import com.example.model.Patient
import com.example.model.TreatmentRecord
import com.example.receiver.ReminderBroadcastReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RahmanClinicApp : Application() {

    lateinit var database: ClinicDatabase
        private set

    lateinit var repository: ClinicRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = ClinicDatabase.getDatabase(this)
        repository = ClinicRepository(database.patientDao())

        createNotificationChannels()
        seedInitialDataIfNeeded()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                ReminderBroadcastReceiver.CHANNEL_ID,
                ReminderBroadcastReceiver.CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "إشعارات تذكيرات الديون والمواعيد لعيادة الرحمن"
                enableVibration(true)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun seedInitialDataIfNeeded() {
        CoroutineScope(Dispatchers.IO).launch {
            val dao = database.patientDao()
            val existing = dao.getPatientById(1L)
            if (existing == null) {
                // Seed initial patient exactly matching mockup screenshot
                val patient1 = Patient(
                    id = 1L,
                    name = "أحمد محمد علي",
                    phone = "+967 770 122 456",
                    totalAmount = 15000.0,
                    paidAmount = 10000.0,
                    remainingBalance = 5000.0,
                    lastTreatment = "حشوة عصب تجميلية",
                    createdAt = System.currentTimeMillis() - 86400000L * 3,
                    updatedAt = System.currentTimeMillis() - 86400000L * 1,
                    notes = "جلسة أولى مكتملة"
                )
                dao.insertPatient(patient1)
                dao.insertTreatment(
                    TreatmentRecord(
                        patientId = 1L,
                        treatmentType = "حشوة عصب تجميلية",
                        totalAmount = 15000.0,
                        paidAmount = 10000.0,
                        remainingAmount = 5000.0,
                        timestamp = System.currentTimeMillis() - 86400000L * 1,
                        notes = "دفعة أولى 10,000 ريال"
                    )
                )

                // Additional sample patient
                val patient2 = Patient(
                    id = 2L,
                    name = "سالم عبدالله القحطاني",
                    phone = "+967 771 998 844",
                    totalAmount = 28000.0,
                    paidAmount = 15000.0,
                    remainingBalance = 13000.0,
                    lastTreatment = "تنظيف وتلميع أسنان وتركيب زركون",
                    createdAt = System.currentTimeMillis() - 86400000L * 7,
                    updatedAt = System.currentTimeMillis() - 86400000L * 2,
                    notes = "متبقي دفعة التركيب النهائي"
                )
                dao.insertPatient(patient2)
                dao.insertTreatment(
                    TreatmentRecord(
                        patientId = 2L,
                        treatmentType = "تنظيف وتلميع أسنان وتركيب زركون",
                        totalAmount = 28000.0,
                        paidAmount = 15000.0,
                        remainingAmount = 13000.0,
                        timestamp = System.currentTimeMillis() - 86400000L * 2
                    )
                )

                // Third sample patient with zero balance (settled)
                val patient3 = Patient(
                    id = 3L,
                    name = "فاطمة محمد باحميد",
                    phone = "+967 773 456 789",
                    totalAmount = 12000.0,
                    paidAmount = 12000.0,
                    remainingBalance = 0.0,
                    lastTreatment = "قلع ضرس العقل جراحياً",
                    createdAt = System.currentTimeMillis() - 86400000L * 10,
                    updatedAt = System.currentTimeMillis() - 86400000L * 4
                )
                dao.insertPatient(patient3)
                dao.insertTreatment(
                    TreatmentRecord(
                        patientId = 3L,
                        treatmentType = "قلع ضرس العقل جراحياً",
                        totalAmount = 12000.0,
                        paidAmount = 12000.0,
                        remainingAmount = 0.0,
                        timestamp = System.currentTimeMillis() - 86400000L * 4
                    )
                )
            }
        }
    }
}
