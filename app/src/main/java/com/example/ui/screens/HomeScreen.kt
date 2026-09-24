package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MergeType
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ClinicCard
import com.example.ui.components.ClinicInputField
import com.example.ui.components.SectionHeader
import com.example.ui.theme.ClinicButtonBlue
import com.example.ui.theme.ClinicCyanAccent
import com.example.ui.theme.ClinicDarkBackground
import com.example.ui.theme.ClinicDarkCardBorder
import com.example.ui.theme.ClinicDarkSurface
import com.example.ui.theme.ClinicDarkSurfaceVariant
import com.example.ui.theme.ClinicDebtRemainingBg
import com.example.ui.theme.ClinicDebtRemainingGreen
import com.example.ui.theme.ClinicHeaderTeal
import com.example.ui.theme.ClinicPaidAmountBg
import com.example.ui.theme.ClinicTealPrimary
import com.example.ui.theme.ClinicTextMuted
import com.example.ui.theme.ClinicTextPrimary
import com.example.ui.theme.ClinicTextSecondary
import com.example.ui.theme.ClinicTotalAmountBg
import com.example.ui.theme.ClinicWarning
import com.example.viewmodel.ClinicViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: ClinicViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Form states from ViewModel
    val patientName by viewModel.patientName.collectAsState()
    val patientPhone by viewModel.patientPhone.collectAsState()
    val treatmentType by viewModel.treatmentType.collectAsState()
    val totalAmount by viewModel.totalAmountInput.collectAsState()
    val paidAmount by viewModel.paidAmountInput.collectAsState()
    val remainingBalance by viewModel.remainingBalanceCalculated.collectAsState()
    val matchedPatient by viewModel.matchedPatient.collectAsState()

    val scheduledTime by viewModel.scheduledTime.collectAsState()
    val scheduledDate by viewModel.scheduledDate.collectAsState()
    val scheduledDay by viewModel.scheduledDay.collectAsState()
    val repeatWeekly by viewModel.repeatWeekly.collectAsState()
    val repeatMonthly by viewModel.repeatMonthly.collectAsState()
    val customSmsMessage by viewModel.customSmsMessage.collectAsState()
    val saveToPhoneBackup by viewModel.saveToPhoneBackup.collectAsState()
    val currency by viewModel.currency.collectAsState()

    // Date and Time Pickers
    val calendar = remember { Calendar.getInstance() }
    val timePickerDialog = remember {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val formattedTime = String.format(Locale.US, "%02d:%02d", hourOfDay, minute)
                viewModel.onScheduledTimeChanged(formattedTime)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
    }

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedCal = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
                val dateFmt = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
                val dayFmt = SimpleDateFormat("EEEE", Locale("ar"))
                viewModel.onScheduledDateChanged(
                    dateFmt.format(selectedCal.time),
                    dayFmt.format(selectedCal.time)
                )
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ClinicDarkBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("home_screen")
    ) {

        // 1. Patient Identification Card (Side by Side: Name & Phone)
        ClinicCard(
            modifier = Modifier.padding(bottom = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ClinicInputField(
                    value = patientName,
                    onValueChange = { viewModel.onPatientNameChanged(it) },
                    label = "اسم المريض",
                    placeholder = "أحمد محمد علي",
                    leadingIcon = Icons.Default.Person,
                    modifier = Modifier.weight(1.1f),
                    testTag = "input_patient_name"
                )

                ClinicInputField(
                    value = patientPhone,
                    onValueChange = { viewModel.onPatientPhoneChanged(it) },
                    label = "رقم الهاتف",
                    placeholder = "+967 770 122 456",
                    leadingIcon = Icons.Default.Phone,
                    modifier = Modifier.weight(0.9f),
                    testTag = "input_patient_phone"
                )
            }

            // Deduplication & Auto-Merge Notification Banner
            AnimatedVisibility(
                visible = matchedPatient != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                matchedPatient?.let { p ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ClinicHeaderTeal.copy(alpha = 0.5f))
                            .border(1.dp, ClinicCyanAccent.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MergeType,
                                contentDescription = null,
                                tint = ClinicCyanAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "مريض مسجل: رصيد سابق ${String.format(Locale.US, "%,.0f", p.remainingBalance)} $currency (سيتم الدمج التلقائي)",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ClinicCyanAccent
                                )
                            )
                        }
                    }
                }
            }
        }

        // 2. Financial & Treatment Details Card
        ClinicCard(
            modifier = Modifier.padding(bottom = 10.dp)
        ) {
            // Treatment Type Box
            ClinicInputField(
                value = treatmentType,
                onValueChange = { viewModel.onTreatmentTypeChanged(it) },
                label = "إجمالي نوع المعالجة",
                placeholder = "حشوة عصب، تقويم، تنظيف وتلميع أسنان...",
                testTag = "input_treatment_type",
                modifier = Modifier.fillMaxWidth()
            )

            // Quick Treatment Suggestion Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("حشوة عصب", "تنظيف وتلميع", "قلع جراحي", "زراعة").forEach { chip ->
                    SuggestionChip(
                        onClick = { viewModel.onTreatmentTypeChanged(chip) },
                        label = { Text(chip, fontSize = 10.5.sp, color = ClinicTextSecondary) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = ClinicDarkSurfaceVariant
                        ),
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            borderColor = ClinicDarkCardBorder,
                            enabled = true
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Three Distinct Financial Boxes (Total, Paid, Remaining)
            FinancialAmountRow(
                label = "إجمالي المبلغ",
                value = totalAmount,
                onValueChange = { viewModel.onTotalAmountChanged(it) },
                backgroundColor = ClinicTotalAmountBg,
                currency = currency,
                testTag = "input_total_amount"
            )

            Spacer(modifier = Modifier.height(6.dp))

            FinancialAmountRow(
                label = "الواصل",
                value = paidAmount,
                onValueChange = { viewModel.onPaidAmountChanged(it) },
                backgroundColor = ClinicPaidAmountBg,
                currency = currency,
                testTag = "input_paid_amount"
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Remaining Balance Box (Auto-calculated)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(ClinicDebtRemainingBg)
                    .border(1.dp, ClinicDebtRemainingGreen.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "الباقي",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ClinicTextPrimary,
                            fontSize = 13.sp
                        )
                    )

                    Text(
                        text = "${String.format(Locale.US, "%,.0f", remainingBalance)} $currency",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = ClinicDebtRemainingGreen,
                            fontSize = 16.sp
                        )
                    )
                }
            }
        }

        // 3. Automated SMS & Custom Reminder Dispatcher Section
        ClinicCard(
            modifier = Modifier.padding(bottom = 10.dp)
        ) {
            SectionHeader(title = "ضبط الرسائل والتذكير تلقائياً")

            // Three Scheduling boxes (Time, Date, Day)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Time Box
                ScheduleControlBox(
                    label = "الوقت",
                    value = scheduledTime,
                    icon = Icons.Default.Schedule,
                    modifier = Modifier.weight(1f),
                    onClick = { timePickerDialog.show() }
                )

                // Date Box
                ScheduleControlBox(
                    label = "التاريخ",
                    value = scheduledDate,
                    icon = Icons.Default.CalendarMonth,
                    modifier = Modifier.weight(1.3f),
                    onClick = { datePickerDialog.show() }
                )

                // Day Box
                ScheduleControlBox(
                    label = "اليوم",
                    value = scheduledDay,
                    icon = Icons.Default.DateRange,
                    modifier = Modifier.weight(1.1f),
                    onClick = { datePickerDialog.show() }
                )
            }

            // Recurrence Toggles (Weekly & Monthly)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { viewModel.onRepeatWeeklyChanged(!repeatWeekly) }
                        .padding(end = 16.dp)
                ) {
                    Checkbox(
                        checked = repeatWeekly,
                        onCheckedChange = { viewModel.onRepeatWeeklyChanged(it) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = ClinicCyanAccent,
                            uncheckedColor = ClinicDarkCardBorder
                        ),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "تكرار أسبوعياً",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = ClinicTextSecondary,
                            fontSize = 12.sp
                        )
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { viewModel.onRepeatMonthlyChanged(!repeatMonthly) }
                ) {
                    Checkbox(
                        checked = repeatMonthly,
                        onCheckedChange = { viewModel.onRepeatMonthlyChanged(it) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = ClinicCyanAccent,
                            uncheckedColor = ClinicDarkCardBorder
                        ),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "تكرار شهرياً",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = ClinicTextSecondary,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Dedicated Manual SMS Input Field
            ClinicInputField(
                value = customSmsMessage,
                onValueChange = { viewModel.onCustomSmsMessageChanged(it) },
                label = "مربع إدخال نص الرسالة يدوياً",
                placeholder = "اكتب نص الرسالة المخصصة هنا...",
                singleLine = false,
                maxLines = 3,
                modifier = Modifier.fillMaxWidth(),
                testTag = "input_custom_sms"
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Prominent Action Button: [تفعيل إرسال الرسالة تلقائياً]
            Button(
                onClick = {
                    viewModel.saveTransactionAndSchedule(
                        context = context,
                        onSuccess = { msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        },
                        onError = { err ->
                            Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("activate_sms_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ClinicButtonBlue
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "تفعيل إرسال الرسالة تلقائياً",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    )
                }
            }
        }

        // 4. Local Backup & Export Section
        ClinicCard(
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            SectionHeader(title = "نسخة الإحتياطية")

            // Backup Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.onSaveToPhoneBackupChanged(!saveToPhoneBackup) }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = saveToPhoneBackup,
                    onCheckedChange = { viewModel.onSaveToPhoneBackupChanged(it) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = ClinicCyanAccent,
                        uncheckedColor = ClinicDarkCardBorder
                    ),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "الحفظ في الهاتف (نسخة احتياطية)",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.5.sp,
                            color = ClinicTextPrimary
                        )
                    )
                    Text(
                        text = "PDF / Excel",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 10.sp,
                            color = ClinicTextMuted
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Two Export Buttons (PDF & Excel) Side by Side
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // PDF Button
                Button(
                    onClick = { viewModel.exportToPdf(context) },
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("export_pdf_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ClinicDarkSurfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = ButtonDefaults.outlinedButtonBorder
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "PDF",
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "حفظ كـ PDF",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = ClinicTextPrimary,
                                fontSize = 12.sp
                            )
                        )
                    }
                }

                // Excel Button
                Button(
                    onClick = { viewModel.exportToExcel(context) },
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("export_excel_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ClinicDarkSurfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = ButtonDefaults.outlinedButtonBorder
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TableChart,
                            contentDescription = "Excel",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "حفظ كـ Excel",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = ClinicTextPrimary,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FinancialAmountRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    backgroundColor: Color,
    currency: String,
    testTag: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(1.dp, ClinicDarkCardBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = ClinicTextSecondary,
                    fontSize = 12.5.sp
                )
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.foundation.text.BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .width(100.dp)
                        .testTag(testTag),
                    textStyle = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ClinicTextPrimary,
                        fontSize = 15.sp,
                        textAlign = TextAlign.End
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = currency,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = ClinicTextMuted,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun ScheduleControlBox(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(ClinicDarkSurfaceVariant)
            .border(1.dp, ClinicDarkCardBorder, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.5.sp,
                    color = ClinicTextMuted
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp,
                    color = ClinicCyanAccent
                ),
                maxLines = 1
            )
        }
    }
}
