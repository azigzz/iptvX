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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.iptvx.app.data.model.SeriesEpisode
import kotlinx.coroutines.delay

@Composable
fun SeriesDetailScreen(
    state: IptvUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onPlay: (SeriesEpisode) -> Unit
) {
    val series = state.selectedSeries
    val episodesBySeason = remember(state.seriesEpisodes) {
        state.seriesEpisodes.groupBy { it.season.coerceAtLeast(1) }.toSortedMap()
    }
    val seasons = episodesBySeason.keys.toList()
    var selectedSeason by remember { mutableStateOf<Int?>(null) }
    val selectedEpisodes = selectedSeason?.let { episodesBySeason[it] }.orEmpty()
    val firstEpisodeId = selectedEpisodes.firstOrNull()?.id
    val firstEpisodeFocus = remember { FocusRequester() }

    LaunchedEffect(seasons) {
        if (selectedSeason == null || selectedSeason !in seasons) {
            selectedSeason = seasons.firstOrNull()
        }
    }

    LaunchedEffect(firstEpisodeId) {
        if (firstEpisodeId != null) {
            delay(150)
            firstEpisodeFocus.requestFocus()
        }
    }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = series?.name ?: "Series",
            subtitle = if (state.seriesLoading) "Carregando episodios..." else countLabel(state.seriesEpisodes.size, "episodio", "episodios"),
            trailing = {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SecondaryButton("Atualizar", enabled = !state.seriesLoading, onClick = onRefresh)
                    SecondaryButton("Voltar", onClick = onBack)
                }
            }
        )
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            SeriesPosterPanel(state = state, seasonCount = seasons.size, modifier = Modifier.width(286.dp).fillMaxHeight())
            if (state.seriesEpisodes.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Image(painter = painterResource(R.drawable.ic_series), contentDescription = null, modifier = Modifier.size(64.dp))
                        Text(
                            if (state.seriesLoading) "Buscando episodios..." else "Nenhum episodio encontrado.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 18.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .width(214.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(seasons) { season ->
                        SeasonRow(
                            season = season,
                            count = episodesBySeason[season].orEmpty().size,
                            selected = selectedSeason == season,
                            onClick = { selectedSeason = season }
                        )
                    }
                }
                Column(Modifier.weight(1f).fillMaxHeight()) {
                    Text(
                        selectedSeason?.let { "Temporada $it" } ?: "Temporadas",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(Modifier.height(12.dp))
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 244.dp),
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(selectedEpisodes, key = { it.id }) { episode ->
                            EpisodeCard(
                                episode = episode,
                                modifier = if (episode.id == firstEpisodeId) Modifier.focusRequester(firstEpisodeFocus) else Modifier,
                                onClick = { onPlay(episode) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SeriesPosterPanel(state: IptvUiState, seasonCount: Int, modifier: Modifier = Modifier) {
    val series = state.selectedSeries
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101821)),
        border = BorderStroke(1.dp, Color(0xFF314154))
    ) {
        Column(Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Brush.linearGradient(listOf(Color(0xFF24316A), Color(0xFF101821)))),
                contentAlignment = Alignment.Center
            ) {
                if (series?.logoUrl.isNullOrBlank()) {
                    Image(painter = painterResource(R.drawable.ic_series), contentDescription = null, modifier = Modifier.size(74.dp))
                } else {
                    AsyncImage(
                        model = series?.logoUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(series?.name.orEmpty(), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(series?.category.orEmpty(), color = Color(0xFF9BC8FF), fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "${countLabel(seasonCount, "temporada", "temporadas")}  |  ${countLabel(state.seriesEpisodes.size, "episodio", "episodios")}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun SeasonRow(season: Int, count: Int, selected: Boolean, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    Card(
        onClick = onClick,
        interactionSource = interaction,
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .focusable(interactionSource = interaction),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = if (selected) Color(0xFF263B6E) else Color(0xFF101821)),
        border = BorderStroke(
            if (focused || selected) 2.dp else 1.dp,
            when {
                focused -> Color.White
                selected -> Color(0xFF58A6FF)
                else -> Color(0xFF314154)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("Temporada $season", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(countLabel(count, "episodio", "episodios"), color = Color(0xFFC9D6E2), fontSize = 12.sp)
        }
    }
}

@Composable
private fun EpisodeCard(episode: SeriesEpisode, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    Card(
        onClick = onClick,
        interactionSource = interaction,
        modifier = modifier.focusable(interactionSource = interaction),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101821)),
        border = BorderStroke(if (focused) 3.dp else 1.dp, if (focused) Color.White else Color(0xFF314154))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(132.dp)
                    .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF172230), Color(0xFF263B6E)))),
                contentAlignment = Alignment.Center
            ) {
                if (episode.imageUrl.isNullOrBlank()) {
                    Image(painter = painterResource(R.drawable.ic_series), contentDescription = null, modifier = Modifier.size(48.dp))
                } else {
                    AsyncImage(
                        model = episode.imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    episode.title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "T${episode.season} E${episode.episode.coerceAtLeast(1)}",
                    color = Color(0xFF9BC8FF),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (!episode.plot.isNullOrBlank()) {
                    Text(
                        episode.plot,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

private fun countLabel(count: Int, singular: String, plural: String): String {
    return "$count ${if (count == 1) singular else plural}"
}
