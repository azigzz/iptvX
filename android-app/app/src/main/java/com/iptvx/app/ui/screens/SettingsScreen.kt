package com.iptvx.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iptvx.app.IptvUiState
import com.iptvx.app.R

private val SettingsYellow = Color(0xFFF4D21B)

private data class SettingsAction(
    val title: String,
    val subtitle: String,
    val icon: Int,
    val onClick: () -> Unit
)

@Composable
fun SettingsScreen(
    state: IptvUiState,
    onBack: () -> Unit,
    onPanelUrl: (String) -> Unit,
    onPerformanceMode: (Boolean) -> Unit,
    onManual: () -> Unit,
    onPairing: () -> Unit,
    onEpg: () -> Unit,
    onFavorites: () -> Unit,
    onHistory: () -> Unit,
    onSync: () -> Unit
) {
    var panelUrl by remember(state.panelUrl) { mutableStateOf(state.panelUrl) }
    val deviceLabel = listOfNotNull(
        state.virtualMac?.let { "mac: $it" },
        state.displayDeviceId.takeIf { it.isNotBlank() }?.let { "id: $it" }
    ).joinToString("  |  ")

    val actions = listOf(
        SettingsAction("Listas", "M3U/M3U8 manual", R.drawable.ic_playlist, onManual),
        SettingsAction("Pareamento", "Login pelo site", R.drawable.ic_device, onPairing),
        SettingsAction("Sincronizar", "Buscar listas do painel", R.drawable.ic_sync, onSync),
        SettingsAction("EPG", "Guia de programacao", R.drawable.ic_epg, onEpg),
        SettingsAction("Favoritos", "Canais marcados", R.drawable.ic_favorite, onFavorites),
        SettingsAction("Historico", "Ultimos assistidos", R.drawable.ic_history, onHistory)
    )

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val compact = maxWidth < 560.dp

        Column(Modifier.fillMaxSize()) {
            ScreenHeader(
                title = "Ajustes",
                subtitle = deviceLabel.ifBlank { "Dispositivo" },
                trailing = { SecondaryButton("Voltar", onClick = onBack) }
            )
            Spacer(Modifier.height(if (compact) 16.dp else 24.dp))

            if (compact) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    DeviceSummary(state)
                    ActionGrid(actions = actions, compact = true)
                    PanelSettings(
                        panelUrl = panelUrl,
                        onPanelUrlChange = { panelUrl = it },
                        canSave = panelUrl.trim().startsWith("http"),
                        onSave = { onPanelUrl(panelUrl.trim()) },
                        performanceMode = state.performanceMode,
                        onPerformanceMode = onPerformanceMode
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(0.9f),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        DeviceSummary(state)
                        PanelSettings(
                            panelUrl = panelUrl,
                            onPanelUrlChange = { panelUrl = it },
                            canSave = panelUrl.trim().startsWith("http"),
                            onSave = { onPanelUrl(panelUrl.trim()) },
                            performanceMode = state.performanceMode,
                            onPerformanceMode = onPerformanceMode
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1.3f),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text("Ferramentas", color = SettingsYellow, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        ActionGrid(actions = actions, compact = false)
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceSummary(state: IptvUiState) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101318)),
        border = BorderStroke(1.dp, Color(0xFF2F3440)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Image(painter = painterResource(R.drawable.brand_mark), contentDescription = null, modifier = Modifier.size(36.dp))
                Column {
                    Text("IPTVX", color = SettingsYellow, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    Text(
                        if (state.paired) "Conectado ao painel" else "Aguardando pareamento",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            }
            InfoLine("MAC", state.virtualMac ?: "...")
            InfoLine("ID", state.displayDeviceId.ifBlank { "..." })
            InfoLine("Listas", state.playlists.size.toString())
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        Text(
            value,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PanelSettings(
    panelUrl: String,
    onPanelUrlChange: (String) -> Unit,
    canSave: Boolean,
    onSave: () -> Unit,
    performanceMode: Boolean,
    onPerformanceMode: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101318)),
        border = BorderStroke(1.dp, Color(0xFF2F3440)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Painel e desempenho", color = SettingsYellow, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = panelUrl,
                onValueChange = onPanelUrlChange,
                label = { Text("URL do painel") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Modo desempenho", color = Color.White, fontWeight = FontWeight.SemiBold)
                    Text("Interface mais leve para TV Box", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                }
                Switch(checked = performanceMode, onCheckedChange = onPerformanceMode)
            }
            PrimaryButton("Salvar painel", enabled = canSave, onClick = onSave)
        }
    }
}

@Composable
private fun ActionGrid(actions: List<SettingsAction>, compact: Boolean) {
    if (compact) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            actions.forEach { action ->
                SettingsActionTile(action = action, modifier = Modifier.fillMaxWidth())
            }
        }
        return
    }

    actions.chunked(3).forEach { rowActions ->
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            rowActions.forEach { action ->
                SettingsActionTile(action = action, modifier = Modifier.weight(1f))
            }
            repeat(3 - rowActions.size) {
                Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SettingsActionTile(action: SettingsAction, modifier: Modifier = Modifier) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()

    Card(
        onClick = action.onClick,
        interactionSource = interaction,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = if (focused) Color(0xFF171A20) else Color(0xFF101318)),
        border = BorderStroke(if (focused) 3.dp else 1.dp, if (focused) SettingsYellow else Color(0xFF2F3440)),
        modifier = modifier
            .height(112.dp)
            .focusable(interactionSource = interaction)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(painter = painterResource(action.icon), contentDescription = null, modifier = Modifier.size(38.dp))
            Column(Modifier.weight(1f)) {
                Text(action.title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(action.subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
