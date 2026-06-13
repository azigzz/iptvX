package com.iptvx.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PlaceholderScreen(title: String, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
        ScreenHeader(title, trailing = { SecondaryButton("Voltar", onClick = onBack) })
        Spacer(Modifier.height(24.dp))
        Text(
            "Estrutura pronta para alimentar esta tela a partir de Xtream/M3U/EPG.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
