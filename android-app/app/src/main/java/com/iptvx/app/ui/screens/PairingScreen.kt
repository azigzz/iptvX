package com.iptvx.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iptvx.app.IptvUiState

@Composable
fun PairingScreen(
    state: IptvUiState,
    onSync: () -> Unit,
    onRefreshCode: () -> Unit,
    onXtreamLogin: (serverUrl: String, username: String, password: String) -> Unit,
    onManual: () -> Unit,
    onSettings: () -> Unit
) {
    var serverUrl by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val canLogin = serverUrl.startsWith("http") && username.isNotBlank() && password.isNotBlank() && !state.loading

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF06111F), Color(0xFF0A2745), Color(0xFF07101A))
                )
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(34.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .width(470.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.width(82.dp).height(82.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("X", color = MaterialTheme.colorScheme.onPrimary, fontSize = 38.sp, fontWeight = FontWeight.Black)
                    }
                }
                Spacer(Modifier.height(18.dp))
                Text("IPTVX PLAYER", fontSize = 16.sp, color = Color(0xFF9FD8FF), fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(14.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xDD08131F)),
                    border = BorderStroke(1.dp, Color(0xFF2C6A94)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Entrar com Xtream", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = serverUrl,
                            onValueChange = { serverUrl = it.trim() },
                            label = { Text("Servidor / Portal URL") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Usuario") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Senha") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = { onXtreamLogin(serverUrl, username, password) },
                            enabled = canLogin,
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0D7FCC),
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(58.dp)
                        ) {
                            Text(if (state.loading) "ENTRANDO..." else "ENTRAR", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.width(390.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PairingCard(state = state, onSync = onSync, onRefreshCode = onRefreshCode)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    SecondaryButton("M3U manual", enabled = !state.loading, onClick = onManual)
                    SecondaryButton("Ajustes", enabled = !state.loading, onClick = onSettings)
                }
            }
        }
    }
}

@Composable
private fun PairingCard(
    state: IptvUiState,
    onSync: () -> Unit,
    onRefreshCode: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xE6101821)),
        border = BorderStroke(1.dp, Color(0xFF27D6A5)),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Parear pelo site", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(
                "Digite estes dados no painel para adicionar listas sem usar o controle.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )

            InfoLine("Painel", state.panelUrl)
            InfoLine("MAC/ID", state.virtualMac ?: "registrando...")

            Column {
                Text("Codigo", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                Text(
                    state.pairingCode ?: "------",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp
                )
            }

            if (!state.error.isNullOrBlank()) {
                Text(
                    state.error,
                    color = MaterialTheme.colorScheme.error,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                PrimaryButton("Sincronizar", enabled = !state.loading, onClick = onSync)
                SecondaryButton("Novo codigo", enabled = !state.loading, onClick = onRefreshCode)
            }
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Column {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        Text(
            value,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Start
        )
    }
}
