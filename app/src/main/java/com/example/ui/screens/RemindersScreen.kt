package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ScheduledReminder
import com.example.ui.components.ClinicCard
import com.example.ui.theme.ClinicButtonBlue
import com.example.ui.theme.ClinicCyanAccent
import com.example.ui.theme.ClinicDarkBackground
import com.example.ui.theme.ClinicDarkCardBorder
import com.example.ui.theme.ClinicDarkSurface
import com.example.ui.theme.ClinicDarkSurfaceVariant
import com.example.ui.theme.ClinicDebtRemainingBg
import com.example.ui.theme.ClinicDebtRemainingGreen
import com.example.ui.theme.ClinicHeaderTeal
import com.example.ui.theme.ClinicTealPrimary
import com.example.ui.theme.ClinicTextMuted
import com.example.ui.theme.ClinicTextPrimary
import com.example.ui.theme.ClinicTextSecondary
import com.example.viewmodel.ClinicViewModel

@Composable
fun RemindersScreen(
    viewModel: ClinicViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val reminders by viewModel.allReminders.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ClinicDarkBackground)
            .padding(14.dp)
            .testTag("reminders_screen")
    ) {
        // Header Banner
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
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = ClinicCyanAccent,
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(
                        text = "جدول رسائل التذكير التلقائية",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                    )
                    Text(
                        text = "يتم إرسال الرسائل والتنبيهات بالموعد الدقيق ومتابعة الديون",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = ClinicTextSecondary,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }

        if (reminders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = null,
                        tint = ClinicTextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "لا توجد رسائل مجدولة حالياً",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = ClinicTextMuted,
                            fontSize = 14.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "يمكنك جدولة تذكير جديد مباشرة من الشاشة الرئيسية",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = ClinicTextSecondary,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(reminders, key = { it.id }) { reminder ->
                    ReminderItemCard(
                        reminder = reminder,
                        onToggleActive = { viewModel.toggleReminderActive(context, reminder) },
                        onSendNow = {
                            viewModel.openSmsApp(context, reminder.patientPhone, reminder.customMessage)
                        },
                        onDelete = {
                            viewModel.deleteReminder(context, reminder)
                            Toast.makeText(context, "تم إلغاء التذكير", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReminderItemCard(
    reminder: ScheduledReminder,
    onToggleActive: () -> Unit,
    onSendNow: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ClinicDarkCardBorder, RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = ClinicDarkSurface),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Row 1: Patient and Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = reminder.patientName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.5.sp,
                            color = ClinicTextPrimary
                        )
                    )
                    Text(
                        text = reminder.patientPhone,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = ClinicTextSecondary,
                            fontSize = 11.5.sp
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (reminder.isActive) "مفعل" else "متوقف",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (reminder.isActive) ClinicCyanAccent else ClinicTextMuted,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Switch(
                        checked = reminder.isActive,
                        onCheckedChange = { onToggleActive() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ClinicTealPrimary,
                            checkedTrackColor = ClinicHeaderTeal,
                            uncheckedThumbColor = ClinicTextMuted,
                            uncheckedTrackColor = ClinicDarkSurfaceVariant
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 2: Date, Time, Day, Recurrence Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Time & Date pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ClinicDarkSurfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${reminder.dayName} ${reminder.dateFormatted} | ${reminder.timeFormatted}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = ClinicCyanAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                // Recurrence pill
                if (reminder.repeatWeekly || reminder.repeatMonthly) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(ClinicDebtRemainingBg)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (reminder.repeatWeekly) "يتكرر أسبوعياً" else "يتكرر شهرياً",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = ClinicDebtRemainingGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 3: Message Text
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(ClinicDarkSurfaceVariant.copy(alpha = 0.6f))
                    .padding(8.dp)
            ) {
                Text(
                    text = reminder.customMessage,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = ClinicTextSecondary,
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 4: Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onSendNow,
                    modifier = Modifier.height(30.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    border = ButtonDefaults.outlinedButtonBorder
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            tint = ClinicButtonBlue,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إرسال رسالة الآن", fontSize = 10.5.sp, color = ClinicButtonBlue)
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "حذف",
                        tint = ClinicTextMuted,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}
