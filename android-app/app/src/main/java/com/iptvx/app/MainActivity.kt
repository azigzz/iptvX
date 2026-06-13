package com.iptvx.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iptvx.app.ui.screens.IptvApp
import com.iptvx.app.ui.theme.IptvTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            IptvTheme {
                IptvApp(state = state, viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.uiState.value.paired) {
            viewModel.syncNow(silent = true)
        }
    }
}
