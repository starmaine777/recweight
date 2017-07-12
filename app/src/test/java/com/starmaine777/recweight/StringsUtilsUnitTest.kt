package com.starmaine777.recweight

import com.starmaine777.recweight.utils.formatInputNumber
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Created by ai on 2017/07/13.
 */

class ExampleInstrumentedTest {
    @Test
    @Throws(Exception::class)
    fun formatInputNumberTest() {
        assertEquals(formatInputNumber("aaa", "0.0"), "0.0")
    }
}
