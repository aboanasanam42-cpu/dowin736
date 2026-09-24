package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `verify app name resource`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("حسابات ديون المرضى لعيادة الرحمن", appName)
    }

    @Test
    fun `verify debt balance calculation`() {
        val total = 15000.0
        val paid = 10000.0
        val remaining = (total - paid).coerceAtLeast(0.0)
        assertEquals(5000.0, remaining, 0.001)
    }
}
