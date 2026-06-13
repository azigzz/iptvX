package com.iptvx.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.iptvx.app.IptvUiState
import com.iptvx.app.data.local.ChannelEntity

@Composable
fun LiveTvScreen(
    state: IptvUiState,
    onBack: () -> Unit,
    onCategory: (String?) -> Unit,
    onSearch: (String) -> Unit,
    onPlay: (ChannelEntity) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "TV ao Vivo",
            subtitle = state.selectedPlaylist?.name ?: "Selecione uma lista na Home",
            trailing = { SecondaryButton("Voltar", onClick = onBack) }
        )
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = onSearch,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Busca global") }
        )
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LazyColumn(
                modifier = Modifier
                    .width(260.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.categories) { category ->
                    val selected = category == state.selectedCategory
                    CategoryRow(
                        name = category ?: "Sem categoria",
                        selected = selected,
                        onClick = { onCategory(category) }
                    )
                }
            }
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.channels, key = { it.id }) { channel ->
                    ChannelRow(channel = channel, onClick = { onPlay(channel) })
                }
                if (state.channels.isEmpty()) {
                    item {
                        Text("Nenhum canal encontrado.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryRow(name: String, selected: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = if (selected) Color(0xFF17382F) else Color(0xFF101821)),
        border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary else Color(0xFF314154)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            name,
            modifier = Modifier.fillMaxWidth().height(56.dp).width(240.dp),
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ChannelRow(channel: ChannelEntity, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101821)),
        border = BorderStroke(1.dp, Color(0xFF314154)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(Modifier.fillMaxWidth().height(74.dp)) {
            Text(
                channel.name,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                channel.category ?: "",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
