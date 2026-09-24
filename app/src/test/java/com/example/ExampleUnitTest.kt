package com.example

import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testPatientDebtMergeCalculation() {
        val initialTotal = 15000.0
        val initialPaid = 10000.0
        val initialRemaining = initialTotal - initialPaid
        assertEquals(5000.0, initialRemaining, 0.001)

        // New treatment added to same patient
        val newTreatmentCost = 8000.0
        val newPayment = 3000.0

        val mergedTotal = initialTotal + newTreatmentCost
        val mergedPaid = initialPaid + newPayment
        val mergedRemaining = mergedTotal - mergedPaid

        assertEquals(23000.0, mergedTotal, 0.001)
        assertEquals(13000.0, mergedPaid, 0.001)
        assertEquals(10000.0, mergedRemaining, 0.001)
    }
}
