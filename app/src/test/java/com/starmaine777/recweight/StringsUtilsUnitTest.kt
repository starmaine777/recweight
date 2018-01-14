package com.starmaine777.recweight

import com.starmaine777.recweight.utils.formatInputNumber
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * StringsUtilのテストクラス
 * Created by ai on 2017/07/13.
 */

class StringsUtilsUnitTest {
    @Test
    @Throws(Exception::class)
    fun formatInputNumberTest() {
        assertEquals(formatInputNumber("", "default"), "default")
        assertEquals(formatInputNumber("", "0.0"), "0.0")
        assertEquals(formatInputNumber("aaa", "0.0"), "0.0")
        assertEquals(formatInputNumber("10", "0.0"), "10.0")
        assertEquals(formatInputNumber("-10", "0.0"), "-10.0")
        assertEquals(formatInputNumber("12.3", "0.0"), "12.3")
        assertEquals(formatInputNumber("-12.3", "0.0"), "-12.3")
        assertEquals(formatInputNumber("12.34", "0.0"), "12.34")
        assertEquals(formatInputNumber("-12.34", "0.0"), "-12.34")
        assertEquals(formatInputNumber("12.344", "0.0"), "12.34")
        assertEquals(formatInputNumber("12.345", "0.0"), "12.35")
        assertEquals(formatInputNumber("-12.344", "0.0"), "-12.34")
        assertEquals(formatInputNumber("-12.345", "0.0"), "-12.35")
    }
}
