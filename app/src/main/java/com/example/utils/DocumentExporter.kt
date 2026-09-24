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


    fun exportPatientStatementToPdf(context: Context, patient: PatientWithTreatments, clinicName: String, currency: String): File? {
        val document = PdfDocument()
        val page = document.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
        val canvas = page.canvas
        canvas.drawColor(Color.WHITE)
        val header = Paint().apply { color = Color.parseColor("#0F363A") }
        canvas.drawRect(0f, 0f, 595f, 100f, header)
        val title = Paint().apply { color = Color.WHITE; textSize = 20f; textAlign = Paint.Align.CENTER; isAntiAlias = true; typeface = Typeface.DEFAULT_BOLD }
        canvas.drawText("كشف حساب المريض", 297f, 40f, title)
        val sub = Paint().apply { color = Color.WHITE; textSize = 11f; textAlign = Paint.Align.CENTER; isAntiAlias = true }
        canvas.drawText(clinicName, 297f, 65f, sub)
        canvas.drawText("تاريخ التقرير: " + dateFormat.format(Date()), 297f, 84f, sub)
        val p = Paint().apply { color = Color.DKGRAY; textSize = 12f; textAlign = Paint.Align.RIGHT; isAntiAlias = true }
        canvas.drawText("اسم المريض: ${patient.patient.name}", 555f, 135f, p)
        canvas.drawText("الهاتف: ${patient.patient.phone}", 555f, 158f, p)
        canvas.drawText("الإجمالي: ${patient.patient.totalAmount} $currency   الواصل: ${patient.patient.paidAmount} $currency   الباقي: ${patient.patient.remainingBalance} $currency", 555f, 181f, p)
        val h = Paint().apply { color = Color.parseColor("#00695C") }
        canvas.drawRect(30f, 205f, 565f, 232f, h)
        val w = Paint().apply { color = Color.WHITE; textSize = 9f; textAlign = Paint.Align.RIGHT; isAntiAlias = true; typeface = Typeface.DEFAULT_BOLD }
        canvas.drawText("التاريخ",555f,223f,w); canvas.drawText("البيان",450f,223f,w); canvas.drawText("الإجمالي",320f,223f,w); canvas.drawText("الواصل",220f,223f,w); canvas.drawText("الباقي",120f,223f,w)
        val row = Paint().apply { color = Color.DKGRAY; textSize = 8.5f; textAlign = Paint.Align.RIGHT; isAntiAlias = true }
        var y = 253f
        patient.treatments.take(25).forEach { t ->
            canvas.drawText(dateFormat.format(Date(t.timestamp)),555f,y,row); canvas.drawText(t.treatmentType.take(22),450f,y,row)
            canvas.drawText(String.format(Locale.US,"%,.0f",t.totalAmount),320f,y,row); canvas.drawText(String.format(Locale.US,"%,.0f",t.paidAmount),220f,y,row); canvas.drawText(String.format(Locale.US,"%,.0f",t.remainingAmount),120f,y,row)
            y += 22f
        }
        document.finishPage(page)
        val dir = File(context.getExternalFilesDir(null), "patient_statements").apply { mkdirs() }
        val safe = patient.patient.name.replace(Regex("[\\\\/:*?\"<>|]"), "_").trim().ifBlank { "مريض_${patient.patient.id}" }
        val file = File(dir, "${safe}_كشف_الحساب.pdf")
        return try { FileOutputStream(file).use { document.writeTo(it) }; document.close(); file } catch(e:Exception){ document.close(); null }
    }

    fun exportPatientStatementToExcel(context: Context, patient: PatientWithTreatments, clinicName: String, currency: String): File? {
        val dir = File(context.getExternalFilesDir(null), "patient_statements").apply { mkdirs() }
        val safe = patient.patient.name.replace(Regex("[\\\\/:*?\"<>|]"), "_").trim().ifBlank { "مريض_${patient.patient.id}" }
        val file = File(dir, "${safe}_كشف_الحساب.xlsx")
        fun esc(s:String)=s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;")
        fun cell(s:String)="<c t=\"inlineStr\"><is><t>${esc(s)}</t></is></c>"
        fun row(v:List<String>)="<row>${v.joinToString(""){cell(it)}}</row>"
        val rows=mutableListOf(row(listOf("كشف حساب المريض",patient.patient.name)),row(listOf("العيادة",clinicName)),row(listOf("الهاتف",patient.patient.phone)),row(listOf("الإجمالي","${patient.patient.totalAmount} $currency","الواصل","${patient.patient.paidAmount} $currency","الباقي","${patient.patient.remainingBalance} $currency")),row(listOf("التاريخ","البيان","الإجمالي","الواصل","الباقي","الملاحظات")))
        patient.treatments.forEach{t->rows+=row(listOf(dateFormat.format(Date(t.timestamp)),t.treatmentType,t.totalAmount.toString(),t.paidAmount.toString(),t.remainingAmount.toString(),t.notes))}
        val types="""<?xml version="1.0" encoding="UTF-8"?><Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/><Default Extension="xml" ContentType="application/xml"/><Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/><Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/></Types>"""
        val rels="""<?xml version="1.0" encoding="UTF-8"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/></Relationships>"""
        val wb="""<?xml version="1.0" encoding="UTF-8"?><workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"><sheets><sheet name="كشف الحساب" sheetId="1" r:id="rId1"/></sheets></workbook>"""
        val wbr="""<?xml version="1.0" encoding="UTF-8"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/></Relationships>"""
        val sheet="""<?xml version="1.0" encoding="UTF-8"?><worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><sheetData>${rows.joinToString("")}</sheetData></worksheet>"""
        return try{java.util.zip.ZipOutputStream(FileOutputStream(file)).use{z->fun put(n:String,d:String){z.putNextEntry(java.util.zip.ZipEntry(n));z.write(d.toByteArray(StandardCharsets.UTF_8));z.closeEntry()};put("[Content_Types].xml",types);put("_rels/.rels",rels);put("xl/workbook.xml",wb);put("xl/_rels/workbook.xml.rels",wbr);put("xl/worksheets/sheet1.xml",sheet)};file}catch(e:Exception){null}
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
