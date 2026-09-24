package com.example.utils

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.model.Patient
import com.example.model.PatientWithTreatments
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DocumentExporter {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    private val fileDateSuffix = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

    /**
     * Exports full clinic debts statement or single patient account to PDF
     */
    fun exportPatientsToPdf(
        context: Context,
        patients: List<Patient>,
        clinicName: String = "عيادة الرحمن التخصصية",
        currency: String = "ريال يمني",
        saveToDownloads: Boolean = true
    ): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 (points)
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        // Background
        val bgPaint = Paint().apply { color = Color.parseColor("#F9FBFB") }
        canvas.drawRect(0f, 0f, 595f, 842f, bgPaint)

        // Header Background Banner
        val headerPaint = Paint().apply { color = Color.parseColor("#0F363A") }
        canvas.drawRect(0f, 0f, 595f, 90f, headerPaint)

        // Header Text
        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 18f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("حسابات ديون المرضى - $clinicName", 595f / 2f, 40f, titlePaint)

        val subTitlePaint = Paint().apply {
            color = Color.parseColor("#80CBC4")
            textSize = 11f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        val dateString = "تاريخ التقرير: " + dateFormat.format(Date())
        canvas.drawText(dateString, 595f / 2f, 65f, subTitlePaint)

        // Summary Statistics Box
        val totalDebt = patients.sumOf { it.totalAmount }
        val totalPaid = patients.sumOf { it.paidAmount }
        val totalRemaining = patients.sumOf { it.remainingBalance }

        val cardPaint = Paint().apply { color = Color.parseColor("#E0F2F1") }
        canvas.drawRoundRect(30f, 105f, 565f, 155f, 10f, 10f, cardPaint)

        val statTextPaint = Paint().apply {
            color = Color.parseColor("#004D40")
            textSize = 11f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("إجمالي المطالبات: ${String.format(Locale.US, "%,.0f", totalDebt)} $currency", 545f, 135f, statTextPaint)
        canvas.drawText("إجمالي الواصل: ${String.format(Locale.US, "%,.0f", totalPaid)} $currency", 370f, 135f, statTextPaint)

        val remStatPaint = Paint().apply {
            color = Color.parseColor("#D32F2F")
            textSize = 11f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("إجمالي الباقي: ${String.format(Locale.US, "%,.0f", totalRemaining)} $currency", 195f, 135f, remStatPaint)

        // Table Header
        val thPaint = Paint().apply { color = Color.parseColor("#00695C") }
        canvas.drawRect(30f, 170f, 565f, 195f, thPaint)

        val thTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 10f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("اسم المريض", 555f, 187f, thTextPaint)
        canvas.drawText("الهاتف", 440f, 187f, thTextPaint)
        canvas.drawText("نوع المعالجة", 345f, 187f, thTextPaint)
        canvas.drawText("الإجمالي", 240f, 187f, thTextPaint)
        canvas.drawText("الواصل", 160f, 187f, thTextPaint)
        canvas.drawText("الباقي", 90f, 187f, thTextPaint)

        // Rows
        val rowTextPaint = Paint().apply {
            color = Color.parseColor("#263238")
            textSize = 9.5f
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }
        val rowRemainingPaint = Paint().apply {
            color = Color.parseColor("#C62828")
            textSize = 9.5f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        val linePaint = Paint().apply {
            color = Color.parseColor("#E0E0E0")
            strokeWidth = 0.8f
        }

        var currentY = 215f
        for ((index, patient) in patients.take(28).withIndex()) {
            val safeName = if (patient.name.length > 20) patient.name.take(18) + ".." else patient.name
            val safeTreatment = if (patient.lastTreatment.length > 16) patient.lastTreatment.take(14) + ".." else patient.lastTreatment

            canvas.drawText(safeName, 555f, currentY, rowTextPaint)
            canvas.drawText(patient.phone, 440f, currentY, rowTextPaint)
            canvas.drawText(safeTreatment, 345f, currentY, rowTextPaint)
            canvas.drawText(String.format(Locale.US, "%,.0f", patient.totalAmount), 240f, currentY, rowTextPaint)
            canvas.drawText(String.format(Locale.US, "%,.0f", patient.paidAmount), 160f, currentY, rowTextPaint)
            canvas.drawText(String.format(Locale.US, "%,.0f", patient.remainingBalance), 90f, currentY, rowRemainingPaint)

            canvas.drawLine(30f, currentY + 6f, 565f, currentY + 6f, linePaint)
            currentY += 21f
        }

        // Footer Stamp
        val footerPaint = Paint().apply {
            color = Color.parseColor("#78909C")
            textSize = 8.5f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("تم إنشاء هذا السند عبر نظام ديون عيادة الرحمن للهواتف الذكية - صادر بتاريخ " + dateFormat.format(Date()), 595f / 2f, 820f, footerPaint)

        pdfDocument.finishPage(page)

        // Save File
        val exportDir = File(context.getExternalFilesDir(null), "exports")
        if (!exportDir.exists()) exportDir.mkdirs()

        val fileName = "كشف_ديون_عيادة_الرحمن_${fileDateSuffix.format(Date())}.pdf"
        val outputFile = File(exportDir, fileName)

        try {
            FileOutputStream(outputFile).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()
            return outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            return null
        }
    }

    /**
     * Exports Patients ledger to Excel (.csv format with UTF-8 BOM so Excel displays Arabic flawlessly)
     */
    fun exportPatientsToExcel(
        context: Context,
        patients: List<Patient>,
        clinicName: String = "عيادة الرحمن التخصصية",
        currency: String = "ريال يمني"
    ): File? {
        val exportDir = File(context.getExternalFilesDir(null), "exports")
        if (!exportDir.exists()) exportDir.mkdirs()

        val fileName = "سجل_ديون_عيادة_الرحمن_${fileDateSuffix.format(Date())}.csv"
        val outputFile = File(exportDir, fileName)

        try {
            FileOutputStream(outputFile).use { fos ->
                // Write UTF-8 BOM so Microsoft Excel correctly renders Arabic text
                fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))

                OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                    writer.appendLine("\"تقرير حسابات وديون المرضى - $clinicName\"")
                    writer.appendLine("\"تاريخ التصدير:\",\"${dateFormat.format(Date())}\"")
                    writer.appendLine("\"العملة:\",\"$currency\"")
                    writer.appendLine()

                    // Headers
                    writer.appendLine("\"م\",\"اسم المريض\",\"رقم الهاتف\",\"نوع المعالجة الأخيرة\",\"إجمالي المبلغ\",\"الواصل (المدفوع)\",\"الباقي (المتبقي)\",\"تاريخ آخر زيارة\"")

                    for ((idx, patient) in patients.withIndex()) {
                        val visitDate = dateFormat.format(Date(patient.updatedAt))
                        writer.appendLine(
                            "\"${idx + 1}\",\"${patient.name}\",\"${patient.phone}\",\"${patient.lastTreatment}\",\"${patient.totalAmount}\",\"${patient.paidAmount}\",\"${patient.remainingBalance}\",\"$visitDate\""
                        )
                    }

                    // Totals row
                    val totalDebt = patients.sumOf { it.totalAmount }
                    val totalPaid = patients.sumOf { it.paidAmount }
                    val totalRemaining = patients.sumOf { it.remainingBalance }
                    writer.appendLine()
                    writer.appendLine("\"الإجمالي العام\",\"\",\"\",\"\",\"$totalDebt\",\"$totalPaid\",\"$totalRemaining\",\"\"")
                    writer.flush()
                }
            }
            return outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Shares or opens exported file
     */
    fun shareFile(context: Context, file: File, mimeType: String, title: String = "مشاركة الملف") {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            context.startActivity(Intent.createChooser(shareIntent, title))
        } catch (e: Exception) {
            Toast.makeText(context, "تعذر فتح المشاركة: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
