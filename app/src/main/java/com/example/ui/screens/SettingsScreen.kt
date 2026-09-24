package com.example.ui.screens

import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ClinicCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.ClinicButtonBlue
import com.example.ui.theme.ClinicCyanAccent
import com.example.ui.theme.ClinicDarkBackground
import com.example.ui.theme.ClinicDarkCardBorder
import com.example.ui.theme.ClinicDarkSurface
import com.example.ui.theme.ClinicDarkSurfaceVariant
import com.example.ui.theme.ClinicDebtRemainingGreen
import com.example.ui.theme.ClinicHeaderTeal
import com.example.ui.theme.ClinicTealPrimary
import com.example.ui.theme.ClinicTextMuted
import com.example.ui.theme.ClinicTextPrimary
import com.example.ui.theme.ClinicTextSecondary
import com.example.utils.ReminderScheduler
import com.example.viewmodel.ClinicViewModel

@Composable
fun SettingsScreen(
    viewModel: ClinicViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var clinicNameInput by remember { mutableStateOf(viewModel.clinicName.value) }
    var doctorNameInput by remember { mutableStateOf(viewModel.doctorName.value) }
    var currencyInput by remember { mutableStateOf(viewModel.currency.value) }

    val canExactAlarm = remember { ReminderScheduler.canScheduleExact(context) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ClinicDarkBackground)
            .verticalScroll(scrollState)
            .padding(14.dp)
            .testTag("settings_screen")
    ) {
        // Clinic Header
        ClinicCard(
            modifier = Modifier.padding(bottom = 12.dp),
            backgroundColor = ClinicHeaderTeal
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocalHospital,
                    contentDescription = null,
                    tint = ClinicCyanAccent,
                    modifier = Modifier.size(32.dp)
                )
                Column {
                    Text(
                        text = "إعدادات عيادة الرحمن",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    )
                    Text(
                        text = "تخصيص البيانات والعملة والنسخ الاحتياطي",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = ClinicTextSecondary,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }

        // Clinic Profile Card
        ClinicCard(modifier = Modifier.padding(bottom = 12.dp)) {
            SectionHeader(title = "بيانات العيادة والتقارير")

            OutlinedTextField(
                value = clinicNameInput,
                onValueChange = {
                    clinicNameInput = it
                    viewModel.clinicName.value = it
                },
                label = { Text("اسم العيادة") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = null,
                        tint = ClinicCyanAccent,
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = ClinicTextPrimary,
                    unfocusedTextColor = ClinicTextPrimary,
                    focusedBorderColor = ClinicTealPrimary,
                    unfocusedBorderColor = ClinicDarkCardBorder
                ),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = currencyInput,
                onValueChange = {
                    currencyInput = it
                    viewModel.currency.value = it
                },
                label = { Text("العملة المعتمدة (مثال: ريال يمني، SAR، USD)") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = null,
                        tint = ClinicDebtRemainingGreen,
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = ClinicTextPrimary,
                    unfocusedTextColor = ClinicTextPrimary,
                    focusedBorderColor = ClinicTealPrimary,
                    unfocusedBorderColor = ClinicDarkCardBorder
                ),
                shape = RoundedCornerShape(8.dp)
            )
        }

        // System & Android 15 Permissions Diagnostic
        ClinicCard(modifier = Modifier.padding(bottom = 12.dp)) {
            SectionHeader(title = "حالة نظام التنبيهات والأذونات (Android 15)")

            DiagnosticItem(
                title = "التنبيهات المجدولة الدقيقة (SCHEDULE_EXACT_ALARM)",
                status = if (canExactAlarm) "مفعل ومتاح" else "مطلوب الإذن من الإعدادات",
                isOk = canExactAlarm,
                onFix = {
                    val intent = ReminderScheduler.requestExactAlarmPermissionIntent(context)
                    if (intent != null) {
                        context.startActivity(intent)
                    }
                }
            )

            Spacer(modifier = Modifier.height(6.dp))

            DiagnosticItem(
                title = "نظام التشغيل المدعوم",
                status = "Android 15 (Target SDK 35)",
                isOk = true
            )
        }

        // Manual Backup & Direct Export Actions
        ClinicCard(modifier = Modifier.padding(bottom = 16.dp)) {
            SectionHeader(title = "تصدير فوري للتقارير")

            Text(
                text = "يمكنك تصدير كشف كامل بجميع المرضى والمبالغ المتبقية بضغطة زر واحدة:",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = ClinicTextSecondary,
                    fontSize = 11.5.sp
                ),
                modifier = Modifier.padding(bottom = 10.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { viewModel.exportToPdf(context) },
                    modifier = Modifier.weight(1f).height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ClinicDarkSurfaceVariant),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("تقرير PDF", fontSize = 12.sp, color = ClinicTextPrimary)
                }

                Button(
                    onClick = { viewModel.exportToExcel(context) },
                    modifier = Modifier.weight(1f).height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ClinicDarkSurfaceVariant),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TableChart,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("جدول Excel", fontSize = 12.sp, color = ClinicTextPrimary)
                }
            }
        }
    }
}

@Composable
private fun DiagnosticItem(
    title: String,
    status: String,
    isOk: Boolean,
    onFix: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(ClinicDarkSurfaceVariant)
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        color = ClinicTextPrimary,
                        fontSize = 11.5.sp
                    )
                )
                Text(
                    text = status,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (isOk) ClinicDebtRemainingGreen else Color(0xFFFFB74D),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            if (!isOk && onFix != null) {
                Button(
                    onClick = onFix,
                    modifier = Modifier.height(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ClinicTealPrimary),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                ) {
                    Text("منح الإذن", fontSize = 10.sp, color = Color.Black)
                }
            } else {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = ClinicDebtRemainingGreen,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
