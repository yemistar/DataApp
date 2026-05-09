package com.example.data_collect.ui.home.capture

internal enum class CaptureMode(val label: String) {
    Feed("Feed"),
    Mortality("Mortality"),
    Eggs("Eggs"),
    Treatment("Treatment"),
    Environment("Environment")
}

internal fun captureModesForFlock(flockType: String): List<CaptureMode> = buildList {
    add(CaptureMode.Feed)
    add(CaptureMode.Mortality)
    if (flockType.equals("layers", ignoreCase = true)) {
        add(CaptureMode.Eggs)
    }
    add(CaptureMode.Treatment)
    add(CaptureMode.Environment)
}
