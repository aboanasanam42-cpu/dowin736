package com.example.model

import androidx.room.Embedded
import androidx.room.Relation

data class PatientWithTreatments(
    @Embedded val patient: Patient,
    @Relation(
        parentColumn = "id",
        entityColumn = "patientId"
    )
    val treatments: List<TreatmentRecord> = emptyList()
)
