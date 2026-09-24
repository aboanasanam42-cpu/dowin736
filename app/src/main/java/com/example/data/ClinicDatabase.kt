package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.model.Patient
import com.example.model.ScheduledReminder
import com.example.model.TreatmentRecord

@Database(
    entities = [
        Patient::class,
        TreatmentRecord::class,
        ScheduledReminder::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ClinicDatabase : RoomDatabase() {

    abstract fun patientDao(): PatientDao

    companion object {
        @Volatile
        private var INSTANCE: ClinicDatabase? = null

        fun getDatabase(context: Context): ClinicDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ClinicDatabase::class.java,
                    "rahman_clinic_debts.db"
                ).fallbackToDestructiveMigration()
                 .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
