package com.example.data_collect.util

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Instant
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
fun today(): String = LocalDate.now().toString()

@RequiresApi(Build.VERSION_CODES.O)
fun nowIso(): String = Instant.now().toString()

@RequiresApi(Build.VERSION_CODES.O)
fun parseDate(date: String): LocalDate? =
    runCatching { LocalDate.parse(date) }.getOrNull()
