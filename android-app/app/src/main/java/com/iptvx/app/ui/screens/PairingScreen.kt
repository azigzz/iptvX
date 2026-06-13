package com.iptvx.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iptvx.app.IptvUiState

@Composable
fun PairingScreen(
    state: IptvUiState,
    onSync: () -> Unit,
    onRefreshCode: () -> Unit,
    onManual: () -> Unit,
    onSettings: () -> Unit
) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
        ScreenHeader(
            title = "Parear dispositivo",
            subtitle = "Acesse o painel web e digite este MAC/ID virtual e codigo para adicionar sua lista."
        )

        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(28.dp)) {
                Text("MAC/ID virtual", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    state.virtualMac ?: "Gerando...",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(24.dp))
                Text("Codigo", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    state.pairingCode ?: "------",
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 6.sp
                )
                Spacer(Modifier.height(24.dp))
                Text("Painel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(state.panelUrl, fontSize = 22.sp)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            PrimaryButton("Ja adicionei, sincronizar", enabled = !state.loading, onClick = onSync)
            SecondaryButton("Novo codigo", enabled = !state.loading, onClick = onRefreshCode)
            SecondaryButton("Adicionar manualmente", enabled = !state.loading, onClick = onManual)
            SecondaryButton("Configuracoes", enabled = !state.loading, onClick = onSettings)
        }
    }
}
