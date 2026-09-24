package com.example.data

import com.example.model.Patient
import com.example.model.PatientWithTreatments
import com.example.model.ScheduledReminder
import com.example.model.TreatmentRecord
import kotlinx.coroutines.flow.Flow

class ClinicRepository(private val dao: PatientDao) {

    val allPatients: Flow<List<Patient>> = dao.getAllPatients()
    val debtorPatients: Flow<List<Patient>> = dao.getDebtorPatients()
    val allReminders: Flow<List<ScheduledReminder>> = dao.getAllReminders()
    val totalClinicDebt: Flow<Double?> = dao.getTotalClinicDebt()
    val totalClinicPaid: Flow<Double?> = dao.getTotalClinicPaid()
    val totalClinicRemaining: Flow<Double?> = dao.getTotalClinicRemaining()
    val allTreatments: Flow<List<TreatmentRecord>> = dao.getAllTreatments()

    fun searchPatients(query: String): Flow<List<Patient>> = dao.searchPatients(query)

    fun observePatientById(id: Long): Flow<Patient?> = dao.observePatientById(id)

    fun getPatientWithTreatments(patientId: Long): Flow<PatientWithTreatments?> =
        dao.getPatientWithTreatments(patientId)

    suspend fun getPatientById(id: Long): Patient? = dao.getPatientById(id)

    suspend fun checkExistingPatient(name: String, phone: String): Patient? {
        val cleanName = name.trim()
        val cleanPhone = phone.replace(" ", "").replace("-", "")
        if (cleanName.isEmpty() && cleanPhone.isEmpty()) return null
        return dao.findPatientByNameOrPhone(cleanName, cleanPhone)
    }

    /**
     * Smart Auto-Merge & Deduplication Logic:
     * If the patient exists (by name or phone), merges new debt & payment amounts
     * into the existing patient profile and logs the new treatment entry.
     * Otherwise creates a new patient record.
     */
    suspend fun saveOrMergePatientTransaction(
        name: String,
        phone: String,
        treatmentType: String,
        totalAmount: Double,
        paidAmount: Double,
        notes: String = ""
    ): Pair<Patient, TreatmentRecord> {
        val existingPatient = checkExistingPatient(name, phone)
        val calculatedRemaining = (totalAmount - paidAmount).coerceAtLeast(0.0)

        val patient: Patient
        val patientId: Long

        if (existingPatient != null) {
            val updatedTotal = existingPatient.totalAmount + totalAmount
            val updatedPaid = existingPatient.paidAmount + paidAmount
            val updatedRemaining = updatedTotal - updatedPaid

            patient = existingPatient.copy(
                name = if (name.isNotBlank()) name.trim() else existingPatient.name,
                phone = if (phone.isNotBlank()) phone.trim() else existingPatient.phone,
                totalAmount = updatedTotal,
                paidAmount = updatedPaid,
                remainingBalance = updatedRemaining,
                lastTreatment = treatmentType.ifBlank { existingPatient.lastTreatment },
                updatedAt = System.currentTimeMillis()
            )
            dao.updatePatient(patient)
            patientId = patient.id
        } else {
            val newPatient = Patient(
                name = name.trim(),
                phone = phone.trim(),
                totalAmount = totalAmount,
                paidAmount = paidAmount,
                remainingBalance = calculatedRemaining,
                lastTreatment = treatmentType.trim(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                notes = notes
            )
            patientId = dao.insertPatient(newPatient)
            patient = newPatient.copy(id = patientId)
        }

        val treatmentRecord = TreatmentRecord(
            patientId = patientId,
            treatmentType = treatmentType.trim().ifEmpty { "معالجة عامة" },
            totalAmount = totalAmount,
            paidAmount = paidAmount,
            remainingAmount = calculatedRemaining,
            timestamp = System.currentTimeMillis(),
            notes = notes
        )
        val treatmentId = dao.insertTreatment(treatmentRecord)

        return Pair(patient, treatmentRecord.copy(id = treatmentId))
    }

    suspend fun addPayment(patientId: Long, paymentAmount: Double, note: String = ""): Boolean {
        val patient = dao.getPatientById(patientId) ?: return false
        val newPaid = patient.paidAmount + paymentAmount
        val newRemaining = (patient.totalAmount - newPaid).coerceAtLeast(0.0)

        val updatedPatient = patient.copy(
            paidAmount = newPaid,
            remainingBalance = newRemaining,
            updatedAt = System.currentTimeMillis()
        )
        dao.updatePatient(updatedPatient)

        val paymentRecord = TreatmentRecord(
            patientId = patientId,
            treatmentType = "دفعة سداد: $note",
            totalAmount = 0.0,
            paidAmount = paymentAmount,
            remainingAmount = -paymentAmount,
            timestamp = System.currentTimeMillis(),
            notes = note
        )
        dao.insertTreatment(paymentRecord)
        return true
    }

    suspend fun updatePatient(patient: Patient) = dao.updatePatient(patient)

    suspend fun deletePatient(patient: Patient) = dao.deletePatient(patient)

    // Reminders
    suspend fun scheduleReminder(reminder: ScheduledReminder): Long =
        dao.insertReminder(reminder)

    suspend fun updateReminder(reminder: ScheduledReminder) =
        dao.updateReminder(reminder)

    suspend fun deleteReminder(reminder: ScheduledReminder) =
        dao.deleteReminder(reminder)

    suspend fun getActiveReminders(): List<ScheduledReminder> =
        dao.getActiveRemindersList()

    suspend fun getReminderById(id: Long): ScheduledReminder? =
        dao.getReminderById(id)
}
