package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.model.Patient
import com.example.model.PatientWithTreatments
import com.example.model.ScheduledReminder
import com.example.model.TreatmentRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface PatientDao {

    // Patients
    @Query("SELECT * FROM patients ORDER BY updatedAt DESC")
    fun getAllPatients(): Flow<List<Patient>>

    @Query("SELECT * FROM patients WHERE remainingBalance > 0 ORDER BY remainingBalance DESC")
    fun getDebtorPatients(): Flow<List<Patient>>

    @Query("SELECT * FROM patients WHERE id = :id LIMIT 1")
    suspend fun getPatientById(id: Long): Patient?

    @Query("SELECT * FROM patients WHERE id = :id LIMIT 1")
    fun observePatientById(id: Long): Flow<Patient?>

    @Query("SELECT * FROM patients WHERE REPLACE(phone, ' ', '') = REPLACE(:phone, ' ', '') OR LOWER(TRIM(name)) = LOWER(TRIM(:name)) LIMIT 1")
    suspend fun findPatientByNameOrPhone(name: String, phone: String): Patient?

    @Query("""
        SELECT * FROM patients 
        WHERE name LIKE '%' || :query || '%' 
           OR phone LIKE '%' || :query || '%' 
        ORDER BY updatedAt DESC
    """)
    fun searchPatients(query: String): Flow<List<Patient>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatient(patient: Patient): Long

    @Update
    suspend fun updatePatient(patient: Patient)

    @Delete
    suspend fun deletePatient(patient: Patient)

    // Treatment Records
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTreatment(record: TreatmentRecord): Long

    @Query("SELECT * FROM treatment_records WHERE patientId = :patientId ORDER BY timestamp DESC")
    fun getTreatmentsForPatient(patientId: Long): Flow<List<TreatmentRecord>>

    @Query("SELECT * FROM treatment_records ORDER BY timestamp DESC")
    fun getAllTreatments(): Flow<List<TreatmentRecord>>

    @Transaction
    @Query("SELECT * FROM patients WHERE id = :patientId")
    fun getPatientWithTreatments(patientId: Long): Flow<PatientWithTreatments?>

    // Reminders
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ScheduledReminder): Long

    @Update
    suspend fun updateReminder(reminder: ScheduledReminder)

    @Delete
    suspend fun deleteReminder(reminder: ScheduledReminder)

    @Query("SELECT * FROM scheduled_reminders ORDER BY scheduledTimeMillis ASC")
    fun getAllReminders(): Flow<List<ScheduledReminder>>

    @Query("SELECT * FROM scheduled_reminders WHERE isActive = 1 ORDER BY scheduledTimeMillis ASC")
    suspend fun getActiveRemindersList(): List<ScheduledReminder>

    @Query("SELECT * FROM scheduled_reminders WHERE id = :id LIMIT 1")
    suspend fun getReminderById(id: Long): ScheduledReminder?

    // Financial Summaries
    @Query("SELECT SUM(totalAmount) FROM patients")
    fun getTotalClinicDebt(): Flow<Double?>

    @Query("SELECT SUM(paidAmount) FROM patients")
    fun getTotalClinicPaid(): Flow<Double?>

    @Query("SELECT SUM(remainingBalance) FROM patients")
    fun getTotalClinicRemaining(): Flow<Double?>
}
