package com.iptvx.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.iptvx.app.IptvUiState
import com.iptvx.app.R
import com.iptvx.app.data.local.ChannelEntity
import kotlinx.coroutines.delay

@Composable
fun CatalogScreen(
    title: String,
    icon: Int,
    state: IptvUiState,
    emptyText: String,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onCategory: (String?) -> Unit,
    onSearch: (String) -> Unit,
    onOpen: ((ChannelEntity) -> Unit)?
) {
    val firstItemId = state.channels.firstOrNull()?.id
    val firstItemFocus = remember { FocusRequester() }

    LaunchedEffect(firstItemId, onOpen != null) {
        if (firstItemId != null && onOpen != null) {
            delay(150)
            firstItemFocus.requestFocus()
        }
    }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = title,
            subtitle = state.selectedPlaylist?.name ?: "Selecione uma lista na Home",
            trailing = {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SecondaryButton("Atualizar", enabled = !state.loading, onClick = onRefresh)
                    SecondaryButton("Voltar", onClick = onBack)
                }
            }
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = onSearch,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Buscar $title") }
        )
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LazyColumn(
                modifier = Modifier
                    .width(260.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.categories) { category ->
                    CatalogCategoryRow(
                        name = category ?: "Sem categoria",
                        selected = category == state.selectedCategory,
                        onClick = { onCategory(category) }
                    )
                }
            }
            if (state.channels.isEmpty()) {
                EmptyCatalogState(
                    title = title,
                    icon = icon,
                    text = if (state.loading) "Carregando catalogo..." else emptyText,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 156.dp),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(state.channels, key = { it.id }) { item ->
                        CatalogCard(
                            item = item,
                            icon = icon,
                            modifier = if (item.id == firstItemId && onOpen != null) Modifier.focusRequester(firstItemFocus) else Modifier,
                            onClick = onOpen
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CatalogCategoryRow(name: String, selected: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = if (selected) Color(0xFF263B6E) else Color(0xFF101821)),
        border = BorderStroke(1.dp, if (selected) Color(0xFF58A6FF) else Color(0xFF314154)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(name, color = Color.White, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun CatalogCard(
    item: ChannelEntity,
    icon: Int,
    modifier: Modifier = Modifier,
    onClick: ((ChannelEntity) -> Unit)?
) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    Card(
        onClick = { onClick?.invoke(item) },
        enabled = onClick != null,
        interactionSource = interaction,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101821)),
        border = BorderStroke(if (focused) 3.dp else 1.dp, if (focused) Color.White else Color(0xFF314154)),
        modifier = modifier.focusable(interactionSource = interaction)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(178.dp)
                    .background(Brush.linearGradient(listOf(Color(0xFF1E2A44), Color(0xFF151922)))),
                contentAlignment = Alignment.Center
            ) {
                if (item.logoUrl.isNullOrBlank()) {
                    Image(painter = painterResource(icon), contentDescription = null, modifier = Modifier.size(58.dp))
                } else {
                    AsyncImage(
                        model = item.logoUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                    )
                }
            }
            Column(Modifier.padding(12.dp)) {
                Text(item.name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(item.category.orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun EmptyCatalogState(title: String, icon: Int, text: String, modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Image(painter = painterResource(icon), contentDescription = null, modifier = Modifier.size(64.dp))
            Text(title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 24.sp)
            Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
