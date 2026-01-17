package com.example.data_collect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.data_collect.ui.FarmTheme
import com.example.data_collect.ui.PoultryApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FarmTheme {
                PoultryApp()
            }
        }
    }
}
