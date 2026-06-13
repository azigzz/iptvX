package com.iptvx.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iptvx.app.IptvUiState

@Composable
fun SettingsScreen(
    state: IptvUiState,
    onBack: () -> Unit,
    onPanelUrl: (String) -> Unit,
    onPerformanceMode: (Boolean) -> Unit
) {
    var panelUrl by remember(state.panelUrl) { mutableStateOf(state.panelUrl) }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Configuracoes", subtitle = state.virtualMac, trailing = { SecondaryButton("Voltar", onClick = onBack) })
        Spacer(Modifier.height(28.dp))
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            OutlinedTextField(value = panelUrl, onValueChange = { panelUrl = it }, label = { Text("URL do painel") }, singleLine = true)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = state.performanceMode, onCheckedChange = onPerformanceMode)
                Text("Modo desempenho")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PrimaryButton("Salvar painel", enabled = panelUrl.startsWith("http"), onClick = { onPanelUrl(panelUrl) })
            }
            Text("Buffer: medio · Tema: escuro · Controle parental/PIN: estrutura preparada")
        }
    }
}
