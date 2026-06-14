package com.iptvx.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iptvx.app.IptvUiState
import com.iptvx.app.R

private val HomeYellow = Color(0xFFF4D21B)

@Composable
fun HomeScreen(
    state: IptvUiState,
    onRefresh: () -> Unit,
    onLive: () -> Unit,
    onVod: () -> Unit,
    onSeries: () -> Unit,
    onSettings: () -> Unit
) {
    val tvFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        tvFocusRequester.requestFocus()
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 30.dp, vertical = 24.dp)
    ) {
        val compact = maxWidth < 560.dp
        val cardHeight = if (compact) 132.dp else 176.dp
        val cardWidth = if (compact) maxWidth - 60.dp else (maxWidth - 108.dp) / 3

        Column(Modifier.fillMaxSize()) {
            HomeHero(
                state = state,
                compact = compact,
                onRefresh = onRefresh,
                onSettings = onSettings
            )

            Spacer(Modifier.height(if (compact) 18.dp else 30.dp))

            if (compact) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
                    MainFeatureCard("TV", "Canais ao vivo", R.drawable.ic_live_tv, tvGradient(), Modifier.width(cardWidth).height(cardHeight).focusRequester(tvFocusRequester), onLive)
                    MainFeatureCard("Filmes", "Biblioteca VOD", R.drawable.ic_movies, movieGradient(), Modifier.width(cardWidth).height(cardHeight), onVod)
                    MainFeatureCard("Series", "Temporadas e episodios", R.drawable.ic_series, seriesGradient(), Modifier.width(cardWidth).height(cardHeight), onSeries)
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp), modifier = Modifier.fillMaxWidth()) {
                    MainFeatureCard("TV", "Canais ao vivo", R.drawable.ic_live_tv, tvGradient(), Modifier.width(cardWidth).height(cardHeight).focusRequester(tvFocusRequester), onLive)
                    MainFeatureCard("Filmes", "Biblioteca VOD", R.drawable.ic_movies, movieGradient(), Modifier.width(cardWidth).height(cardHeight), onVod)
                    MainFeatureCard("Series", "Temporadas e episodios", R.drawable.ic_series, seriesGradient(), Modifier.width(cardWidth).height(cardHeight), onSeries)
                }
            }
        }
    }
}

@Composable
private fun HomeHero(
    state: IptvUiState,
    compact: Boolean,
    onRefresh: () -> Unit,
    onSettings: () -> Unit
) {
    val playlistName = state.selectedPlaylist?.name ?: state.playlists.firstOrNull()?.name ?: "Sem lista"
    val heroHeight = if (compact) 122.dp else 138.dp
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(heroHeight)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF123C4B),
                        Color(0xFF123034),
                        Color(0xFF1C2248),
                        Color(0xFF3E2458)
                    )
                )
            )
            .border(1.dp, Color(0x6645D4FF), RoundedCornerShape(16.dp))
            .padding(horizontal = if (compact) 18.dp else 26.dp, vertical = if (compact) 14.dp else 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(if (compact) 14.dp else 18.dp),
                modifier = Modifier.weight(1f)
            ) {
                Image(
                    painter = painterResource(R.drawable.brand_mark),
                    contentDescription = null,
                    modifier = Modifier.size(if (compact) 58.dp else 72.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("IPTVX", color = HomeYellow, fontSize = if (compact) 34.sp else 42.sp, fontWeight = FontWeight.Black)
                    Text(
                        "Pronto para assistir",
                        color = Color(0xFFEAF4FF),
                        fontSize = if (compact) 15.sp else 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Lista ativa: $playlistName",
                        color = Color(0xFFD8E6F2),
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                IconPillButton(icon = R.drawable.ic_sync, text = if (state.loading) "Atualizando" else "Atualizar", enabled = !state.loading, onClick = onRefresh)
                IconPillButton(icon = R.drawable.ic_settings, text = "Ajustes", onClick = onSettings)
            }
        }
    }
}

@Composable
private fun MainFeatureCard(
    title: String,
    subtitle: String,
    icon: Int,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    Card(
        onClick = onClick,
        interactionSource = interaction,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(if (focused) 3.dp else 1.dp, if (focused) Color.White else Color(0xFF2F3440)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = modifier.focusable(interactionSource = interaction)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(colors))
                .padding(22.dp)
        ) {
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .size(54.dp)
            )
            Column(Modifier.align(Alignment.BottomStart)) {
                Text(title, fontSize = 31.sp, fontWeight = FontWeight.Black, color = Color.White)
                Text(subtitle, fontSize = 14.sp, color = Color(0xFFE8EDF7))
            }
        }
    }
}

private fun tvGradient() = listOf(Color(0xFF10E0A0), Color(0xFF198BEE), Color(0xFF5D2BFF))
private fun movieGradient() = listOf(Color(0xFFF21A62), Color(0xFFFF7A2F), Color(0xFFFFD447))
private fun seriesGradient() = listOf(Color(0xFF9B2BFF), Color(0xFF4F7DFF), Color(0xFF45D4FF))

@Composable
private fun IconPillButton(icon: Int, text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF11151C)),
        border = BorderStroke(1.dp, Color(0xFF2F3440))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(painter = painterResource(icon), contentDescription = null, modifier = Modifier.size(22.dp))
            Text(text, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
