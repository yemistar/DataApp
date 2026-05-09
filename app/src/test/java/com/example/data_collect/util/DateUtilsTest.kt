package com.example.data_collect.util

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DateUtilsTest {
    @Test
    fun parseDateReturnsLocalDateForIsoDate() {
        assertEquals(LocalDate.of(2026, 5, 8), parseDate("2026-05-08"))
    }

    @Test
    fun parseDateReturnsNullForInvalidInput() {
        assertNull(parseDate("May 8, 2026"))
        assertNull(parseDate(""))
    }
}
