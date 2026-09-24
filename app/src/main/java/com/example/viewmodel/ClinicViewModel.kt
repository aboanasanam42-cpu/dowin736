package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.RahmanClinicApp
import com.example.data.ClinicRepository
import com.example.model.Patient
import com.example.model.ScheduledReminder
import com.example.model.TreatmentRecord
import com.example.utils.DocumentExporter
import com.example.utils.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ClinicViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ClinicRepository =
        (application as RahmanClinicApp).repository

    val allPatients: StateFlow<List<Patient>> = repository.allPatients
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val debtorPatients: StateFlow<List<Patient>> = repository.debtorPatients
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReminders: StateFlow<List<ScheduledReminder>> = repository.allReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalClinicDebt: StateFlow<Double> = repository.totalClinicDebt
        .combine(MutableStateFlow(0.0)) { dbVal, _ -> dbVal ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalClinicPaid: StateFlow<Double> = repository.totalClinicPaid
        .combine(MutableStateFlow(0.0)) { dbVal, _ -> dbVal ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalClinicRemaining: StateFlow<Double> = repository.totalClinicRemaining
        .combine(MutableStateFlow(0.0)) { dbVal, _ -> dbVal ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Form States (Matches UI Mockup)
    var patientName = MutableStateFlow("أحمد محمد علي")
        private set
    var patientPhone = MutableStateFlow("+967 770 122 456")
        private set
    var treatmentType = MutableStateFlow("حشوة عصب وتجميل أسنان")
        private set
    var totalAmountInput = MutableStateFlow("15000")
        private set
    var paidAmountInput = MutableStateFlow("10000")
        private set

    // Real-time calculated Remaining Balance
    val remainingBalanceCalculated: StateFlow<Double> = combine(
        totalAmountInput,
        paidAmountInput
    ) { totalStr, paidStr ->
        val total = totalStr.toDoubleOrNull() ?: 0.0
        val paid = paidStr.toDoubleOrNull() ?: 0.0
        (total - paid).coerceAtLeast(0.0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 5000.0)

    // Deduplication & Matching Status
    private val _matchedPatient = MutableStateFlow<Patient?>(null)
    val matchedPatient: StateFlow<Patient?> = _matchedPatient.asStateFlow()

    // Scheduler Form Inputs
    private val calendar = Calendar.getInstance()
    private val timeSdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateSdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
    private val daySdf = SimpleDateFormat("EEEE", Locale("ar"))

    var scheduledTime = MutableStateFlow("15:30")
        private set
    var scheduledDate = MutableStateFlow(dateSdf.format(Date()))
        private set
    var scheduledDay = MutableStateFlow(daySdf.format(Date()))
        private set

    var repeatWeekly = MutableStateFlow(false)
        private set
    var repeatMonthly = MutableStateFlow(false)
        private set

    var customSmsMessage = MutableStateFlow(
        "مرحباً بك، نود تذكيرك بموعدك ومتابعة حسابك في عيادة الرحمن. المبلغ المتبقي: 5,000 ريال يمني. أهلاً بك دوماً."
    )
        private set

    var saveToPhoneBackup = MutableStateFlow(true)
        private set

    var clinicName = MutableStateFlow("عيادة الرحمن التخصصية")
        private set
    var doctorName = MutableStateFlow("د. الرحمن")
        private set
    var currency = MutableStateFlow("ريال يمني")
        private set

    // Search query for Patients tab
    var searchQuery = MutableStateFlow("")
        private set

    val filteredPatients: StateFlow<List<Patient>> = combine(
        allPatients,
        searchQuery
    ) { patients, query ->
        if (query.isBlank()) {
            patients
        } else {
            patients.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.phone.contains(query) ||
                        it.lastTreatment.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Initial match check for pre-filled data
        checkForExistingPatient(patientName.value, patientPhone.value)
    }

    // Input handlers
    fun onPatientNameChanged(name: String) {
        patientName.value = name
        checkForExistingPatient(name, patientPhone.value)
        updateDefaultMessageTemplate()
    }

    fun onPatientPhoneChanged(phone: String) {
        patientPhone.value = phone
        checkForExistingPatient(patientName.value, phone)
    }

    fun onTreatmentTypeChanged(type: String) {
        treatmentType.value = type
    }

    fun onTotalAmountChanged(amount: String) {
        totalAmountInput.value = amount.filter { it.isDigit() || it == '.' }
        updateDefaultMessageTemplate()
    }

    fun onPaidAmountChanged(amount: String) {
        paidAmountInput.value = amount.filter { it.isDigit() || it == '.' }
        updateDefaultMessageTemplate()
    }

    fun onScheduledTimeChanged(time: String) {
        scheduledTime.value = time
    }

    fun onScheduledDateChanged(date: String, day: String) {
        scheduledDate.value = date
        scheduledDay.value = day
    }

    fun onRepeatWeeklyChanged(checked: Boolean) {
        repeatWeekly.value = checked
        if (checked) repeatMonthly.value = false
    }

    fun onRepeatMonthlyChanged(checked: Boolean) {
        repeatMonthly.value = checked
        if (checked) repeatWeekly.value = false
    }

    fun onCustomSmsMessageChanged(message: String) {
        customSmsMessage.value = message
    }

    fun onSaveToPhoneBackupChanged(checked: Boolean) {
        saveToPhoneBackup.value = checked
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    fun selectPatientForForm(patient: Patient) {
        patientName.value = patient.name
        patientPhone.value = patient.phone
        treatmentType.value = patient.lastTreatment
        _matchedPatient.value = patient
        updateDefaultMessageTemplate()
    }

    private fun checkForExistingPatient(name: String, phone: String) {
        viewModelScope.launch {
            val match = repository.checkExistingPatient(name, phone)
            _matchedPatient.value = match
        }
    }

    private fun updateDefaultMessageTemplate() {
        val name = patientName.value.ifBlank { "عزيزنا المريض" }
        val remaining = remainingBalanceCalculated.value
        val cur = currency.value
        val formattedRemaining = String.format(Locale.US, "%,.0f", remaining)
        customSmsMessage.value =
            "مرحباً $name، نود تذكيركم بموعدكم ومتابعة حسابكم في ${clinicName.value}. المبلغ المتبقي: $formattedRemaining $cur. نتمنى لكم دوام الصحة."
    }

    /**
     * Executes the Core Operation:
     * - Smart Auto-merge & Deduplication
     * - Saves treatment & running balance
     * - Automatically schedules SMS / reminder if time is configured
     * - Performs backup export if checkbox is selected
     */
    fun saveTransactionAndSchedule(
        context: Context,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val name = patientName.value.trim()
        val phone = patientPhone.value.trim()
        val treatment = treatmentType.value.trim()
        val total = totalAmountInput.value.toDoubleOrNull() ?: 0.0
        val paid = paidAmountInput.value.toDoubleOrNull() ?: 0.0

        if (name.isBlank()) {
            onError("يرجى إدخال اسم المريض")
            return
        }

        viewModelScope.launch {
            try {
                // 1. Save or Merge in Room
                val isMerge = _matchedPatient.value != null
                val (savedPatient, record) = repository.saveOrMergePatientTransaction(
                    name = name,
                    phone = phone,
                    treatmentType = treatment,
                    totalAmount = total,
                    paidAmount = paid
                )

                // 2. Schedule Reminder if scheduled
                val triggerMillis = parseScheduleTimeToMillis(
                    scheduledDate.value,
                    scheduledTime.value
                )

                val reminder = ScheduledReminder(
                    patientId = savedPatient.id,
                    patientName = savedPatient.name,
                    patientPhone = savedPatient.phone,
                    scheduledTimeMillis = triggerMillis,
                    timeFormatted = scheduledTime.value,
                    dateFormatted = scheduledDate.value,
                    dayName = scheduledDay.value,
                    repeatWeekly = repeatWeekly.value,
                    repeatMonthly = repeatMonthly.value,
                    customMessage = customSmsMessage.value,
                    isSent = false,
                    isActive = true
                )

                val reminderId = repository.scheduleReminder(reminder)
                val fullReminder = reminder.copy(id = reminderId)

                // Register with Android AlarmManager
                if (triggerMillis > System.currentTimeMillis()) {
                    ReminderScheduler.schedule(context, fullReminder)
                }

                // 3. Local Backup if toggle is active
                if (saveToPhoneBackup.value) {
                    val currentList = allPatients.value
                    DocumentExporter.exportPatientsToExcel(context, currentList, clinicName.value, currency.value)
                }

                autoSavePatientStatement(context, savedPatient.id)\n\n                val msg = if (isMerge) {
                    "تم بنجاح دمج الحساب مع المريض (${savedPatient.name}) وإضافة المعالجة. الرصيد المتبقي: ${String.format(Locale.US, "%,.0f", savedPatient.remainingBalance)} ${currency.value}"
                } else {
                    "تم تسجيل المريض الجديد (${savedPatient.name}) وجدولة التذكير بنجاح!"
                }

                onSuccess(msg)
            } catch (e: Exception) {
                onError("حدث خطأ أثناء الحفظ: ${e.localizedMessage}")
            }
        }
    }

    private fun parseScheduleTimeToMillis(dateStr: String, timeStr: String): Long {
        return try {
            val combined = "$dateStr $timeStr"
            val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
            val date = sdf.parse(combined)
            date?.time ?: (System.currentTimeMillis() + 3600000L)
        } catch (e: Exception) {
            System.currentTimeMillis() + 3600000L // default 1 hour in future
        }
    }

    // Direct SMS Action (Open SMS app or send)
    fun openSmsApp(context: Context, phone: String, message: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("sms:${Uri.encode(phone)}")
                putExtra("sms_body", message)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "تعذر فتح تطبيق الرسائل: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Direct Phone Call
    fun callPatient(context: Context, phone: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${Uri.encode(phone)}")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "تعذر فتح لوحة الاتصال: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Direct WhatsApp message
    fun openWhatsApp(context: Context, phone: String, message: String) {
        try {
            val cleanPhone = phone.replace("+", "").replace(" ", "").replace("-", "")
            val url = "https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(message)}"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "تعذر فتح واتساب: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Export PDF
    fun exportToPdf(context: Context) {
        val list = allPatients.value
        val file = DocumentExporter.exportPatientsToPdf(
            context = context,
            patients = list,
            clinicName = clinicName.value,
            currency = currency.value
        )
        if (file != null) {
            Toast.makeText(context, "تم حفظ ملف PDF في الهاتف بنجاح: ${file.name}", Toast.LENGTH_LONG).show()
            DocumentExporter.shareFile(context, file, "application/pdf", "كشف ديون عيادة الرحمن PDF")
        } else {
            Toast.makeText(context, "فشل إنشاء ملف PDF", Toast.LENGTH_SHORT).show()
        }
    }

    // Export Excel
    fun exportToExcel(context: Context) {
        val list = allPatients.value
        val file = DocumentExporter.exportPatientsToExcel(
            context = context,
            patients = list,
            clinicName = clinicName.value,
            currency = currency.value
        )
        if (file != null) {
            Toast.makeText(context, "تم حفظ ملف Excel في الهاتف بنجاح: ${file.name}", Toast.LENGTH_LONG).show()
            DocumentExporter.shareFile(context, file, "text/csv", "سجل ديون عيادة الرحمن Excel")
        } else {
            Toast.makeText(context, "فشل إنشاء ملف Excel", Toast.LENGTH_SHORT).show()
        }
    }

    private fun autoSaveEnabled(patientId: Long): Boolean =
        getApplication<Application>().getSharedPreferences("patient_export_settings", Context.MODE_PRIVATE)
            .getBoolean("auto_$patientId", false)

    fun isPatientAutoSaveEnabled(patientId: Long): Boolean = autoSaveEnabled(patientId)

    fun setPatientAutoSave(patientId: Long, enabled: Boolean) {
        getApplication<Application>().getSharedPreferences("patient_export_settings", Context.MODE_PRIVATE)
            .edit().putBoolean("auto_$patientId", enabled).apply()
        if (enabled) savePatientStatement(getApplication(), patientId, "pdf", false)
    }

    fun savePatientStatement(context: Context, patientId: Long, format: String = "pdf", share: Boolean = true) {
        viewModelScope.launch {
            val data = repository.getPatientWithTreatments(patientId).first()
            if (data == null) {
                Toast.makeText(context, "تعذر العثور على كشف المريض", Toast.LENGTH_SHORT).show()
                return@launch
            }
            val file = if (format == "excel") {
                DocumentExporter.exportPatientStatementToExcel(context, data, clinicName.value, currency.value)
            } else {
                DocumentExporter.exportPatientStatementToPdf(context, data, clinicName.value, currency.value)
            }
            if (file != null) {
                Toast.makeText(context, "تم حفظ كشف " + data.patient.name + " بصيغة " + if (format == "excel") "Excel" else "PDF", Toast.LENGTH_SHORT).show()
                if (share) DocumentExporter.shareFile(context, file, if (format == "excel") "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" else "application/pdf", "كشف حساب " + data.patient.name)
            } else {
                Toast.makeText(context, "فشل حفظ كشف حساب المريض", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun autoSavePatientStatement(context: Context, patientId: Long) {
        if (autoSaveEnabled(patientId)) savePatientStatement(context, patientId, "pdf", false)
    }
    // Add Payment to Existing Patient
    fun addPayment(patientId: Long, amount: Double, note: String, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.addPayment(patientId, amount, note)
            autoSavePatientStatement(getApplication(), patientId)
            onDone()
        }
    }

    // Delete Patient
    fun deletePatient(patient: Patient) {
        viewModelScope.launch {
            repository.deletePatient(patient)
        }
    }

    // Delete Reminder
    fun deleteReminder(context: Context, reminder: ScheduledReminder) {
        viewModelScope.launch {
            ReminderScheduler.cancel(context, reminder.id)
            repository.deleteReminder(reminder)
        }
    }

    // Toggle Reminder Active
    fun toggleReminderActive(context: Context, reminder: ScheduledReminder) {
        viewModelScope.launch {
            val updated = reminder.copy(isActive = !reminder.isActive)
            repository.updateReminder(updated)
            if (updated.isActive) {
                ReminderScheduler.schedule(context, updated)
            } else {
                ReminderScheduler.cancel(context, reminder.id)
            }
        }
    }
}
