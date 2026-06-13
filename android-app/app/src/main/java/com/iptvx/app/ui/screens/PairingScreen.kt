package com.iptvx.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
    val canLogin = serverUrl.isNotBlank() && username.isNotBlank() && password.isNotBlank() && !state.loading

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF11151C), Color(0xFF050608)),
                    radius = 900f
                )
            )
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .height(8.dp)
                .background(Color(0xFFF2D21C))
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 18.dp, end = 24.dp)
                .width(128.dp)
                .height(44.dp)
                .background(Color(0x55F2D21C), RoundedCornerShape(4.dp))
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .width(390.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LogoMark()
            Spacer(Modifier.height(12.dp))
            Text(
                "IPTVX",
                color = Color(0xFFF2D21C),
                fontSize = 34.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(24.dp))
            LoginField(value = serverUrl, onValueChange = { serverUrl = it }, label = "Code")
            Spacer(Modifier.height(10.dp))
            LoginField(value = username, onValueChange = { username = it }, label = "Username", highlight = true)
            Spacer(Modifier.height(10.dp))
            LoginField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                isPassword = true
            )
            Spacer(Modifier.height(18.dp))
            Button(
                onClick = { onXtreamLogin(serverUrl, username, password) },
                enabled = canLogin,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                    disabledContainerColor = Color.Transparent,
                    disabledContentColor = Color(0xFF808080)
                ),
                border = BorderStroke(1.dp, Color(0xFFE6E9F2)),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .width(220.dp)
                    .height(42.dp)
            ) {
                Text(if (state.loading) "Loading" else "Login", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }

        CornerIdentity(
            mac = state.virtualMac ?: "...",
            id = state.deviceId.ifBlank { "..." },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 22.dp)
        )
    }
}

@Composable
private fun LogoMark() {
    Box(
        modifier = Modifier
            .size(54.dp)
            .border(0.dp, Color.Transparent, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = CircleShape,
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF2D21C)),
            modifier = Modifier.size(46.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text("X", color = Color(0xFF101010), fontWeight = FontWeight.Black, fontSize = 25.sp)
            }
        }
    }
}

@Composable
private fun LoginField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    highlight: Boolean = false,
    isPassword: Boolean = false
) {
    val container = if (highlight) Color(0xFFF2E733) else Color(0xFFEFF2FF)
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        label = { Text(label, fontSize = 11.sp) },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = container,
            unfocusedContainerColor = container,
            disabledContainerColor = container,
            focusedTextColor = Color(0xFF101010),
            unfocusedTextColor = Color(0xFF101010),
            focusedLabelColor = Color(0xFF505050),
            unfocusedLabelColor = Color(0xFF505050),
            focusedBorderColor = Color(0xFFF2D21C),
            unfocusedBorderColor = Color.Transparent,
            cursorColor = Color(0xFF101010)
        ),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    )
}

@Composable
private fun CornerIdentity(mac: String, id: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.width(330.dp),
        horizontalAlignment = Alignment.End
    ) {
        Text(
            "mac: $mac",
            color = Color(0xFFECECEC),
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.End
        )
        Text(
            "id: $id",
            color = Color(0xFFECECEC),
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.End
        )
    }
}
