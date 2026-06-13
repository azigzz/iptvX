package com.iptvx.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF101318), Color(0xFF08090C), Color(0xFF050608))
                )
            )
            .padding(horizontal = 30.dp, vertical = 24.dp)
    ) {
        val compact = maxWidth < 560.dp
        val cardHeight = if (compact) 132.dp else 176.dp
        val cardWidth = if (compact) maxWidth - 60.dp else (maxWidth - 108.dp) / 3

        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Image(
                        painter = painterResource(R.drawable.brand_mark),
                        contentDescription = null,
                        modifier = Modifier.size(46.dp)
                    )
                    Column {
                        Text("IPTVX", color = HomeYellow, fontSize = 27.sp, fontWeight = FontWeight.Black)
                        Text(
                            state.selectedPlaylist?.name ?: state.playlists.firstOrNull()?.name ?: "Pronto para assistir",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    IconPillButton(icon = R.drawable.ic_sync, text = if (state.loading) "Sync..." else "Atualizar", enabled = !state.loading, onClick = onRefresh)
                    IconPillButton(icon = R.drawable.ic_settings, text = "Ajustes", onClick = onSettings)
                }
            }

            Spacer(Modifier.height(if (compact) 26.dp else 42.dp))

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
