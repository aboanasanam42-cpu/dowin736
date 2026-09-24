package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TableView
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Patient
import com.example.ui.components.ClinicCard
import com.example.ui.theme.ClinicButtonBlue
import com.example.ui.theme.ClinicCyanAccent
import com.example.ui.theme.ClinicDarkBackground
import com.example.ui.theme.ClinicDarkCardBorder
import com.example.ui.theme.ClinicDarkSurface
import com.example.ui.theme.ClinicDarkSurfaceVariant
import com.example.ui.theme.ClinicDebtRemainingBg
import com.example.ui.theme.ClinicDebtRemainingGreen
import com.example.ui.theme.ClinicError
import com.example.ui.theme.ClinicHeaderTeal
import com.example.ui.theme.ClinicTealPrimary
import com.example.ui.theme.ClinicTextMuted
import com.example.ui.theme.ClinicTextPrimary
import com.example.ui.theme.ClinicTextSecondary
import com.example.ui.theme.ClinicWarning
import com.example.viewmodel.ClinicViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PatientsScreen(
    viewModel: ClinicViewModel,
    onSelectPatientForHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val patients by viewModel.filteredPatients.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val currency by viewModel.currency.collectAsState()

    val totalDebt by viewModel.totalClinicDebt.collectAsState()
    val totalPaid by viewModel.totalClinicPaid.collectAsState()
    val totalRemaining by viewModel.totalClinicRemaining.collectAsState()

    var filterType by remember { mutableStateOf(0) } // 0: All, 1: Debtors, 2: Settled
    var selectedPatientForPayment by remember { mutableStateOf<Patient?>(null) }
    var paymentAmountInput by remember { mutableStateOf("") }
    var paymentNoteInput by remember { mutableStateOf("") }

    val displayedPatients = remember(patients, filterType) {
        when (filterType) {
            1 -> patients.filter { it.remainingBalance > 0 }
            2 -> patients.filter { it.remainingBalance <= 0 }
            else -> patients
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ClinicDarkBackground)
            .padding(14.dp)
            .testTag("patients_screen")
    ) {

        // Total Clinic Debt Summary Banner
        ClinicCard(
            modifier = Modifier.padding(bottom = 10.dp),
            backgroundColor = ClinicHeaderTeal
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "إجمالي ديون المرضى المتبقية",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = ClinicTextSecondary,
                            fontSize = 11.sp
                        )
                    )
                    Text(
                        text = "${String.format(Locale.US, "%,.0f", totalRemaining)} $currency",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = ClinicDebtRemainingGreen,
                            fontSize = 20.sp
                        )
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "عدد المرضى: ${patients.size}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = ClinicCyanAccent,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Text(
                        text = "المحصل: ${String.format(Locale.US, "%,.0f", totalPaid)} $currency",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = ClinicTextPrimary,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.onSearchQueryChanged(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .testTag("search_patients_input"),
            placeholder = {
                Text(
                    text = "بحث باسم المريض، رقم الهاتف، أو المعالجة...",
                    fontSize = 12.5.sp,
                    color = ClinicTextMuted
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = ClinicCyanAccent,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "مسح",
                            tint = ClinicTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = ClinicDarkSurface,
                unfocusedContainerColor = ClinicDarkSurface,
                focusedBorderColor = ClinicTealPrimary,
                unfocusedBorderColor = ClinicDarkCardBorder,
                focusedTextColor = ClinicTextPrimary,
                unfocusedTextColor = ClinicTextPrimary
            )
        )

        // Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = filterType == 0,
                onClick = { filterType = 0 },
                label = { Text("الكل (${patients.size})", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ClinicTealPrimary,
                    selectedLabelColor = Color.Black,
                    containerColor = ClinicDarkSurfaceVariant,
                    labelColor = ClinicTextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = filterType == 0,
                    borderColor = ClinicDarkCardBorder,
                    selectedBorderColor = ClinicTealPrimary
                )
            )

            FilterChip(
                selected = filterType == 1,
                onClick = { filterType = 1 },
                label = {
                    Text(
                        "المدينون (${patients.count { it.remainingBalance > 0 }})",
                        fontSize = 11.sp
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ClinicError,
                    selectedLabelColor = Color.White,
                    containerColor = ClinicDarkSurfaceVariant,
                    labelColor = ClinicTextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = filterType == 1,
                    borderColor = ClinicDarkCardBorder,
                    selectedBorderColor = ClinicError
                )
            )

            FilterChip(
                selected = filterType == 2,
                onClick = { filterType = 2 },
                label = {
                    Text(
                        "المسددون (${patients.count { it.remainingBalance <= 0 }})",
                        fontSize = 11.sp
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ClinicDebtRemainingGreen,
                    selectedLabelColor = Color.Black,
                    containerColor = ClinicDarkSurfaceVariant,
                    labelColor = ClinicTextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = filterType == 2,
                    borderColor = ClinicDarkCardBorder,
                    selectedBorderColor = ClinicDebtRemainingGreen
                )
            )
        }

        // Patients List
        if (displayedPatients.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "لا يوجد سجلات مرضى مطابقة للبحث",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = ClinicTextMuted,
                        fontSize = 14.sp
                    )
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(displayedPatients, key = { it.id }) { patient ->
                    PatientListItemCard(
                        patient = patient,
                        currency = currency,
                        onAddPayment = { selectedPatientForPayment = patient },
                        onSelect = {
                            viewModel.selectPatientForForm(patient)
                            onSelectPatientForHome()
                        },
                        onCall = { viewModel.callPatient(context, patient.phone) },
                        onWhatsApp = {
                            val msg = "مرحباً ${patient.name}، نود تذكيركم بمراجعة حسابكم في عيادة الرحمن، المتبقي: ${String.format(Locale.US, "%,.0f", patient.remainingBalance)} $currency."
                            viewModel.openWhatsApp(context, patient.phone, msg)
                        },
                        onSms = {
                            val msg = "مرحباً ${patient.name}، نود تذكيركم بمراجعة حسابكم في عيادة الرحمن، المتبقي: ${String.format(Locale.US, "%,.0f", patient.remainingBalance)} $currency."
                            viewModel.openSmsApp(context, patient.phone, msg)
                        },
                        autoSaveEnabled = viewModel.isPatientAutoSaveEnabled(patient.id),
                        onAutoSaveChanged = { enabled -> viewModel.setPatientAutoSave(patient.id, enabled) },
                        onSavePdf = { viewModel.savePatientStatement(context, patient.id, "pdf", true) },
                        onSaveExcel = { viewModel.savePatientStatement(context, patient.id, "excel", true) },
                        onDelete = {
                            viewModel.deletePatient(patient)
                            Toast.makeText(context, "تم حذف المريض ${patient.name}", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }

    // Payment Dialog
    selectedPatientForPayment?.let { patient ->
        AlertDialog(
            onDismissRequest = { selectedPatientForPayment = null },
            title = {
                Text(
                    text = "تسجيل دفعة سداد للمريض: ${patient.name}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = ClinicTextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "الرصيد المتبقي الحالي: ${String.format(Locale.US, "%,.0f", patient.remainingBalance)} $currency",
                        color = ClinicDebtRemainingGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    OutlinedTextField(
                        value = paymentAmountInput,
                        onValueChange = { paymentAmountInput = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("مبلغ السداد ($currency)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ClinicTextPrimary,
                            unfocusedTextColor = ClinicTextPrimary,
                            focusedBorderColor = ClinicTealPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = paymentNoteInput,
                        onValueChange = { paymentNoteInput = it },
                        label = { Text("ملاحظة الدفعة (اختياري)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ClinicTextPrimary,
                            unfocusedTextColor = ClinicTextPrimary,
                            focusedBorderColor = ClinicTealPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = paymentAmountInput.toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            viewModel.addPayment(patient.id, amount, paymentNoteInput) {
                                Toast.makeText(context, "تم تسجيل الدفعة بنجاح!", Toast.LENGTH_SHORT).show()
                                selectedPatientForPayment = null
                                paymentAmountInput = ""
                                paymentNoteInput = ""
                            }
                        } else {
                            Toast.makeText(context, "يرجى إدخال مبلغ صحيح", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ClinicTealPrimary)
                ) {
                    Text("حفظ الدفعة", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedPatientForPayment = null }) {
                    Text("إلغاء", color = ClinicTextSecondary)
                }
            },
            containerColor = ClinicDarkSurface
        )
    }
}

@Composable
private fun PatientListItemCard(
    patient: Patient,
    currency: String,
    onAddPayment: () -> Unit,
    onSelect: () -> Unit,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit,
    onSms: () -> Unit,
    autoSaveEnabled: Boolean,
    onAutoSaveChanged: (Boolean) -> Unit,
    onSavePdf: () -> Unit,
    onSaveExcel: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFmt = remember { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ClinicDarkCardBorder, RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = ClinicDarkSurface),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Row 1: Name and Remaining Balance Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ClinicDarkSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = ClinicCyanAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = patient.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = ClinicTextPrimary
                            )
                        )
                        Text(
                            text = patient.phone,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = ClinicTextSecondary,
                                fontSize = 11.5.sp
                            )
                        )
                    }
                }

                // Balance Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (patient.remainingBalance > 0) ClinicDebtRemainingBg else ClinicDarkSurfaceVariant
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (patient.remainingBalance > 0) {
                            "باقي: ${String.format(Locale.US, "%,.0f", patient.remainingBalance)} $currency"
                        } else {
                            "مسدد بالكامل ✓"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (patient.remainingBalance > 0) ClinicDebtRemainingGreen else ClinicTealPrimary
                        )
                    )
                }
            }

            // حفظ كشف الحساب الخاص بالمريض
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(ClinicDarkSurfaceVariant).padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("الحفظ التلقائي", fontSize = 11.sp, color = ClinicTextPrimary, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(checked = autoSaveEnabled, onCheckedChange = onAutoSaveChanged, modifier = Modifier.size(40.dp), colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ClinicTealPrimary))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(onClick = onSavePdf, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "حفظ PDF", tint = ClinicError, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onSaveExcel, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.TableView, contentDescription = "حفظ Excel", tint = ClinicDebtRemainingGreen, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 2: Treatment & Amounts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "المعالجة: ${patient.lastTreatment.ifBlank { "عامة" }}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = ClinicTextMuted,
                        fontSize = 11.sp
                    )
                )

                Text(
                    text = "الإجمالي: ${String.format(Locale.US, "%,.0f", patient.totalAmount)} | الواصل: ${String.format(Locale.US, "%,.0f", patient.paidAmount)}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = ClinicTextSecondary,
                        fontSize = 11.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 3: Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Call
                    IconButton(
                        onClick = onCall,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "اتصال",
                            tint = ClinicTealPrimary,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // WhatsApp
                    IconButton(
                        onClick = onWhatsApp,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "واتساب",
                            tint = Color(0xFF25D366),
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // SMS
                    IconButton(
                        onClick = onSms,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "رسالة نصية",
                            tint = ClinicCyanAccent,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // Add Payment
                    IconButton(
                        onClick = onAddPayment,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payment,
                            contentDescription = "سداد",
                            tint = ClinicDebtRemainingGreen,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Load in Form
                    OutlinedButton(
                        onClick = onSelect,
                        modifier = Modifier.height(28.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        border = ButtonDefaults.outlinedButtonBorder
                    ) {
                        Text("إضافة معالجة", fontSize = 10.5.sp, color = ClinicCyanAccent)
                    }

                    // Delete
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "حذف",
                            tint = ClinicTextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
