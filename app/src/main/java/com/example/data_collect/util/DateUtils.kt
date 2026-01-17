package com.example.data_collect.util

import java.time.Instant
import java.time.LocalDate

fun today(): String = LocalDate.now().toString()

fun nowIso(): String = Instant.now().toString()

fun parseDate(date: String): LocalDate? =
    runCatching { LocalDate.parse(date) }.getOrNull()
