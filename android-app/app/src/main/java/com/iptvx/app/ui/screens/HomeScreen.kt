package com.iptvx.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iptvx.app.IptvUiState
import com.iptvx.app.data.model.PlaylistConfig

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    state: IptvUiState,
    onSync: () -> Unit,
    onPairing: () -> Unit,
    onManual: () -> Unit,
    onLive: () -> Unit,
    onVod: () -> Unit,
    onSeries: () -> Unit,
    onEpg: () -> Unit,
    onFavorites: () -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit,
    onPlaylistSelected: (PlaylistConfig) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "iptvX",
            subtitle = state.virtualMac ?: state.deviceId,
            trailing = { PrimaryButton("Sincronizar", enabled = !state.loading, onClick = onSync) }
        )
        Spacer(Modifier.height(28.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            TvCardButton("TV ao Vivo", "Canais e categorias", Modifier.width(230.dp), onLive)
            TvCardButton("Filmes", "VOD", Modifier.width(230.dp), onVod)
            TvCardButton("Series", "Temporadas e episodios", Modifier.width(230.dp), onSeries)
            TvCardButton("EPG", "Grade de programacao", Modifier.width(230.dp), onEpg)
            TvCardButton("Favoritos", "Conteudos salvos", Modifier.width(230.dp), onFavorites)
            TvCardButton("Historico", "Continuar assistindo", Modifier.width(230.dp), onHistory)
            TvCardButton("Listas", "Adicionar manualmente", Modifier.width(230.dp), onManual)
            TvCardButton("Configuracoes", "Player e dispositivo", Modifier.width(230.dp), onSettings)
        }

        Spacer(Modifier.height(28.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            SecondaryButton("Tela de pareamento", onClick = onPairing)
            SecondaryButton("Adicionar lista manual", onClick = onManual)
        }
        Spacer(Modifier.height(24.dp))
        Text("Listas ativas", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.playlists) { playlist ->
                TvCardButton(
                    title = playlist.name,
                    subtitle = playlist.type.name,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onPlaylistSelected(playlist) }
                )
            }
            if (state.playlists.isEmpty()) {
                item {
                    Text(
                        "Nenhuma lista sincronizada.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
