package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scheduled_reminders")
data class ScheduledReminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val patientId: Long,
    val patientName: String,
    val patientPhone: String,
    val scheduledTimeMillis: Long,
    val timeFormatted: String, // e.g., "15:30"
    val dateFormatted: String, // e.g., "15 / 03 / 2025"
    val dayName: String, // e.g., "الأربعاء"
    val repeatWeekly: Boolean = false,
    val repeatMonthly: Boolean = false,
    val customMessage: String,
    val isSent: Boolean = false,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
