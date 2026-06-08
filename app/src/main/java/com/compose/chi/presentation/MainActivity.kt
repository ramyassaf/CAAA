package com.compose.chi.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.compose.chi.analytics.AnalyticsLogger
import com.compose.chi.analytics.AnalyticsLoggerImpl

class MainActivity : ComponentActivity(), AnalyticsLogger by AnalyticsLoggerImpl() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        registerLifecycleOwner(this)

        setContent {
            ChiPresentationRoot()
        }
    }
}
