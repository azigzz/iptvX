package com.iptvx.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ManualPlaylistScreen(
    loading: Boolean,
    onBack: () -> Unit,
    onSave: (name: String, url: String, epgUrl: String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var epg by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Adicionar lista manual", trailing = { SecondaryButton("Voltar", onClick = onBack) })
        Spacer(Modifier.height(28.dp))
        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { androidx.compose.material3.Text("Nome") }, singleLine = true)
            OutlinedTextField(value = url, onValueChange = { url = it }, label = { androidx.compose.material3.Text("URL M3U/M3U8") }, singleLine = true)
            OutlinedTextField(value = epg, onValueChange = { epg = it }, label = { androidx.compose.material3.Text("EPG opcional") }, singleLine = true)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PrimaryButton(
                    text = if (loading) "Salvando..." else "Testar e salvar",
                    enabled = !loading && name.isNotBlank() && url.startsWith("http"),
                    onClick = { onSave(name, url, epg.ifBlank { null }) }
                )
                SecondaryButton("Cancelar", onClick = onBack)
            }
        }
    }
}
