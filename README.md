# تطبيق حسابات ديون المرضى لعيادة الرحمن 🏥
### Patient Debts Management for Al-Rahman Clinic (Android 15 - API 35)

تطبيق أندرويد متكامل واحترافي تم بناؤه باستخدام **Kotlin** و **Jetpack Compose** وفق أحدث معايير **Material Design 3** وتوجيهات أندرويد 15 (API Level 35)، مصمم خصيصاً لإدارة العيادات والمراكز الطبية لمتابعة حسابات وديون المرضى، وجدولة رسائل التذكير التلقائية (SMS)، والتصدير المباشر لتقارير PDF و Excel.

---

## 📱 مميزات التطبيق الرئيسية

### 1. واجهة مستخدم متطابقة وتصميم داكن فاخر (Dark Teal / Cyan)
- مطابقة تامة للتصميم المرفق باللون الكحلي المخضر الداكن ودرجات السيان المتناسقة.
- دعم كامل وشامل للغة العربية واتجاه الكتابة من اليمين لليسار (**RTL Layout**).
- شريط علوي أنيق باسم "حسابات ديون المرضى لعيادة الرحمن" مع أيقونة الملف الشخصي والقائمة.

### 2. تسجيل المرضى والدمج التلقائي الذكي (Smart Auto-Merge & Deduplication)
- حقلا **اسم المريض** و **رقم الهاتف** متجاوران لتسهيل وتسريع الإدخال في العيادة.
- **خوارزمية منع التكرار:** عند إدخال اسم أو رقم هاتف مريض مسجل مسبقاً، يتعرف النظام تلقائياً على هويته ويعرض رصيده المتبقي فوراً.
- عند حفظ معاملة جديدة، **لا يتم تكرار بيانات المريض**؛ بل يتم دمج المعالجة الجديدة وسجل المبالغ المالية تراكمياً وحفظ سجل الزيارات بالكامل.

### 3. العمليات المالية والمعالجة الحسابية اللحظية
- حقل مخصص لـ **نوع المعالجة** مع شرائح اختيار سريعة (حشوة عصب، تنظيف وتلميع، قلع جراحي، زراعة...).
- ثلاث حاويات مالية مستقلة:
  - **إجمالي المبلغ**
  - **الواصل (المدفوع)**
  - **الباقي (المتبقي):** يتم حسابه تلقائياً وبشكل فوري (`إجمالي المبلغ - الواصل`).

### 4. نظام جدولة وإرسال رسائل الـ SMS والتذكير التلقائي (Exact Alarm Dispatcher)
- صناديق تحكم لاختيار **الوقت**، **التاريخ**، و **اليوم** عبر نوافذ تفاعلية.
- خيارات التكرار الدوري: **أسبوعياً** أو **شهرياً**.
- **مربع إدخال نص الرسالة يدوياً:** حقل مرن لكتابة وتعديل نص الرسالة مع قالب تلقائي يدمج اسم المريض والمبلغ المتبقي.
- متوافق مع نظام أندرويد 15 وإدارة الأذونات الحديثة (`SCHEDULE_EXACT_ALARM`, `SEND_SMS`, `POST_NOTIFICATIONS`).

### 5. النسخ الاحتياطي والتصدير الفوري (PDF & Excel)
- خيار تفعيل حفظ نسخة احتياطية في الهاتف تلقائياً.
- **تصدير كـ PDF:** كشف حساب رسمي منسق قياس A4 باللغة العربية جاهز للطباعة أو المشاركة عبر WhatsApp والبريد.
- **تصدير كـ Excel (.csv):** ملف جدول بيانات مشفر بـ UTF-8 BOM لضمان فتح النصوص العربية في Microsoft Excel و Google Sheets بدون مشاكل ترميز.

---

## 🏗️ هيكلية المشروع (Project Architecture)

المشروع مبني باتباع معمارية **MVVM (Model-View-ViewModel)** مع استخدام **Room Database** للتخزين المحلي الدائم:

```
app/
├── src/
│   ├── main/
│   │   ├── AndroidManifest.xml              # الأذونات والإعلانات لـ Android 15
│   │   ├── java/com/example/
│   │   │   ├── MainActivity.kt              # النشاط الرئيسي والتنقل السفلي
│   │   │   ├── RahmanClinicApp.kt           # تطبيق التطبيق وقناة الإشعارات والبيانات الأولية
│   │   │   ├── data/
│   │   │   │   ├── ClinicDatabase.kt        # قاعدة بيانات Room المحلية
│   │   │   │   ├── ClinicRepository.kt      # مستودع البيانات ومنطق الدمج التلقائي
│   │   │   │   └── PatientDao.kt            # استعلامات SQL ومعاملات المرضى
│   │   │   ├── model/
│   │   │   │   ├── Patient.kt               # جدول المرضى
│   │   │   │   ├── TreatmentRecord.kt       # سجل المعالجات والمدفوعات
│   │   │   │   ├── ScheduledReminder.kt     # جدول رسائل التذكير المجدولة
│   │   │   │   └── PatientWithTreatments.kt # علاقة 1-to-many للمريض ومعالجاته
│   │   │   ├── receiver/
│   │   │   │   ├── ReminderBroadcastReceiver.kt # مستقبل التنبيهات وإرسال SMS
│   │   │   │   └── BootReceiver.kt          # إعادة جدولة التنبيهات بعد إعادة تشغيل الهاتف
│   │   │   ├── ui/
│   │   │   │   ├── components/
│   │   │   │   │   └── CommonComponents.kt  # المكونات المشتركة والأشرطة العلوية
│   │   │   │   ├── screens/
│   │   │   │   │   ├── HomeScreen.kt        # الشاشة الرئيسية المطابقة للصورة المرفقة
│   │   │   │   │   ├── PatientsScreen.kt    # سجل وبحث وفلترة المرضى وسداد الديون
│   │   │   │   │   ├── RemindersScreen.kt   # إدارة ومتابعة التذكيرات المجدولة
│   │   │   │   │   └── SettingsScreen.kt    # إعدادات العيادة، العملة، وفحص الأذونات
│   │   │   │   └── theme/
│   │   │   │       ├── Color.kt             # ألوان عيادة الرحمن الداكنة
│   │   │   │       ├── Theme.kt             # سمة Material 3
│   │   │   │       └── Type.kt              # الخطوط والتنسيقات
│   │   │   ├── utils/
│   │   │   │   ├── DocumentExporter.kt      # مولد تقارير PDF وجداول Excel (CSV)
│   │   │   │   └── ReminderScheduler.kt     # إدارة منبهات AlarmManager الدقيقة
│   │   │   └── viewmodel/
│   │   │       └── ClinicViewModel.kt       # إدارة الحالة والمنطق الحسابي
│   │   └── res/
│   │       ├── drawable/                    # الأيقونات والشعارات التكيفية
│   │       ├── values/
│   │       │   ├── strings.xml              # نصوص التطبيق باللغة العربية
│   │       │   └── themes.xml               # أنماط أندرويد
│   │       └── xml/
│   │           └── file_paths.xml           # مسارات FileProvider لمشاركة الملفات
│   └── test/                                # اختبارات JVM و Robolectric Unit Tests
├── .github/
│   └── workflows/
│       └── build-apk.yml                    # خط أنابيب CI/CD التلقائي لبناء APK
├── build.gradle.kts                         # إعدادات البناء الرئيسية
└── settings.gradle.kts
```

---

## 💻 أهم الشيفرات البرمجية الأساسية (Core Source Code)

### 1. منطق الدمج التلقائي الذكي ومنع تكرار المرضى (`ClinicRepository.kt`)
```kotlin
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
```

### 2. جدولة منبهات وإرسال الرسائل بدقة في Android 15 (`ReminderScheduler.kt`)
```kotlin
fun schedule(context: Context, reminder: ScheduledReminder) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

    val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
        putExtra(EXTRA_REMINDER_ID, reminder.id)
        putExtra(EXTRA_PATIENT_NAME, reminder.patientName)
        putExtra(EXTRA_PATIENT_PHONE, reminder.patientPhone)
        putExtra(EXTRA_MESSAGE, reminder.customMessage)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        reminder.id.toInt(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val triggerTime = reminder.scheduledTimeMillis
    if (triggerTime <= System.currentTimeMillis()) return

    try {
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    } catch (e: SecurityException) {
        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
    }
}
```

### 3. تصدير تقارير PDF وجداول Excel المتوافقة مع اللغة العربية (`DocumentExporter.kt`)
```kotlin
// كتابة ملف CSV مشفر بـ UTF-8 BOM لضمان دعم اللغة العربية في Microsoft Excel
FileOutputStream(outputFile).use { fos ->
    fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
    OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
        writer.appendLine("\"تقرير حسابات وديون المرضى - $clinicName\"")
        writer.appendLine("\"تاريخ التصدير:\",\"${dateFormat.format(Date())}\"")
        writer.appendLine("\"العملة:\",\"$currency\"")
        writer.appendLine()
        writer.appendLine("\"م\",\"اسم المريض\",\"رقم الهاتف\",\"نوع المعالجة الأخيرة\",\"إجمالي المبلغ\",\"الواصل (المدفوع)\",\"الباقي (المتبقي)\",\"تاريخ آخر زيارة\"")
        for ((idx, patient) in patients.withIndex()) {
            writer.appendLine(
                "\"${idx + 1}\",\"${patient.name}\",\"${patient.phone}\",\"${patient.lastTreatment}\",\"${patient.totalAmount}\",\"${patient.paidAmount}\",\"${patient.remainingBalance}\",\"${dateFormat.format(Date(patient.updatedAt))}\""
            )
        }
        writer.flush()
    }
}
```

---

## 🚀 كيفية الرفع إلى GitHub والحصول على ملف الـ APK تلقائياً

### الطريقة الأولى: عبر واجهة Google AI Studio مباشرة (موصى بها)
1. من أعلى واجهة المتصفح، اضغط على **قائمة الإعدادات أو زر الـ Export / GitHub**.
2. اختر **"Push to GitHub"**.
3. قم بتسجيل الدخول إلى حسابك في GitHub وحدد اسم المستودع (مثلاً: `rahman-clinic-debts`).
4. سيتم رفع كافة ملفات المشروع وملف الأتمتة `.github/workflows/build-apk.yml` إلى مستودعك الجديد مباشرة.

### الطريقة الثانية: عبر سطر الأوامر (Git CLI)
إذا قمت بتنزيل المشروع وتريد رفعه يدوياً:
```bash
# 1. تهيئة المستودع وإضافة الملفات
git init
git add .
git commit -m "الإصدار الأولي لتطبيق حسابات ديون عيادة الرحمن (Android 15)"

# 2. ربط المستودع بمستودع GitHub الخاص بك (استبدل YOUR_USERNAME و YOUR_REPO)
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO.git

# 3. الرفع إلى GitHub
git push -u origin main
```

---

## ⚙️ تشغيل خط البناء التلقائي على GitHub وتنزيل الـ APK:
1. ادخل إلى مستودعك على **GitHub**.
2. انتقل إلى تبويب **Actions**.
3. ستجد سير العمل: **"Build & Release Android APK"**.
4. يمكنك الضغط على زر **Run workflow** للتشغيل الفوري.
5. بعد اكتمال البناء (يستغرق حوالي 2 إلى 3 دقائق):
   - ستجد ملف الـ APK جاهزاً للتنزيل في قسم **Artifacts** باسم `Rahman-Clinic-Debts-APKs`.
   - أو انتقل إلى صفحة **Releases** لتحميل الـ APK مباشرة وتثبيته على هاتفك فوراً!
