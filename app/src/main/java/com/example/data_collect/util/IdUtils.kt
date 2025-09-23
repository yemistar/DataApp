package com.example.data_collect.util

import java.util.UUID

fun uid(): String = UUID.randomUUID().toString().replace("-", "").take(8)
