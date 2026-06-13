package com.iptvx.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iptvx.app.IptvUiState
import com.iptvx.app.R

private val ThemeYellow = Color(0xFFF4D21B)
private val DeepBlack = Color(0xFF050608)

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
    val usernameFocus = remember { FocusRequester() }
    val passwordFocus = remember { FocusRequester() }
    val loginFocus = remember { FocusRequester() }
    val submitLogin = {
        if (canLogin) onXtreamLogin(serverUrl.trim(), username.trim(), password)
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF16181E), Color(0xFF08090C), DeepBlack),
                    radius = 980f
                )
            )
    ) {
        val portrait = maxHeight > maxWidth
        val shortScreen = maxHeight < 470.dp
        val sidePadding = if (maxWidth < 520.dp) 18.dp else 32.dp
        val formWidth = when {
            maxWidth < 420.dp -> maxWidth - sidePadding * 2
            portrait -> minDp(maxWidth * 0.78f, 420.dp)
            else -> minDp(maxWidth * 0.34f, 430.dp)
        }
        val logoSize = when {
            shortScreen -> 48.dp
            maxWidth < 520.dp -> 58.dp
            else -> 72.dp
        }
        val titleSize = if (shortScreen) 28.sp else 36.sp
        val fieldHeight = if (shortScreen) 44.dp else 50.dp
        val gap = if (shortScreen) 7.dp else 10.dp
        val bottomPadding = if (maxHeight < 430.dp) 10.dp else 20.dp

        DecorativeEdge()

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .width(formWidth),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.brand_mark),
                contentDescription = null,
                modifier = Modifier.size(logoSize)
            )
            Spacer(Modifier.height(if (shortScreen) 6.dp else 10.dp))
            Text(
                "IPTVX",
                color = ThemeYellow,
                fontSize = titleSize,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(if (shortScreen) 14.dp else 22.dp))
            LoginField(
                value = serverUrl,
                onValueChange = { serverUrl = it },
                label = "Code",
                height = fieldHeight,
                imeAction = ImeAction.Next,
                keyboardActions = KeyboardActions(onNext = { usernameFocus.requestFocus() })
            )
            Spacer(Modifier.height(gap))
            LoginField(
                value = username,
                onValueChange = { username = it },
                label = "Username",
                height = fieldHeight,
                highlight = true,
                imeAction = ImeAction.Next,
                keyboardActions = KeyboardActions(onNext = { passwordFocus.requestFocus() }),
                modifier = Modifier.focusRequester(usernameFocus)
            )
            Spacer(Modifier.height(gap))
            LoginField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                height = fieldHeight,
                isPassword = true,
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(onDone = { submitLogin() }),
                modifier = Modifier
                    .focusRequester(passwordFocus)
                    .onPreviewKeyEvent { event ->
                        if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                        when (event.key) {
                            Key.DirectionDown -> {
                                loginFocus.requestFocus()
                                true
                            }
                            Key.Enter, Key.NumPadEnter -> {
                                submitLogin()
                                true
                            }
                            else -> false
                        }
                    }
            )
            Spacer(Modifier.height(if (shortScreen) 12.dp else 18.dp))
            Button(
                onClick = submitLogin,
                enabled = canLogin,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                    disabledContainerColor = Color.Transparent,
                    disabledContentColor = Color(0xFF7D7D7D)
                ),
                border = BorderStroke(1.dp, Color(0xFFE9E9F2)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .focusRequester(loginFocus)
                    .width(minDp(formWidth * 0.64f, 240.dp))
                    .height(if (shortScreen) 38.dp else 42.dp)
            ) {
                Text(if (state.loading) "Loading" else "Login", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
            if (!state.error.isNullOrBlank()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    state.error,
                    color = Color(0xFFFF746E),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            } else if (state.loading) {
                Spacer(Modifier.height(10.dp))
                Text(
                    "Carregando lista...",
                    color = Color(0xFFECECEC),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        CornerIdentity(
            mac = state.virtualMac ?: "...",
            id = state.displayDeviceId.ifBlank { "..." },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = sidePadding, bottom = bottomPadding),
            maxWidth = minDp(maxWidth * 0.46f, 360.dp)
        )
    }
}

@Composable
private fun DecorativeEdge() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(7.dp)
            .background(ThemeYellow)
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .width(90.dp)
                .height(3.dp)
                .background(ThemeYellow)
        )
    }
}

@Composable
private fun LoginField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    height: Dp,
    highlight: Boolean = false,
    isPassword: Boolean = false,
    imeAction: ImeAction = ImeAction.Next,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    modifier: Modifier = Modifier
) {
    val container = if (highlight) ThemeYellow else Color(0xFFF1F4FF)
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        placeholder = { Text(label, color = Color(0xFF555555), fontSize = 13.sp) },
        keyboardOptions = KeyboardOptions(
            imeAction = imeAction,
            keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Uri
        ),
        keyboardActions = keyboardActions,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = container,
            unfocusedContainerColor = container,
            disabledContainerColor = container,
            focusedTextColor = Color(0xFF111111),
            unfocusedTextColor = Color(0xFF111111),
            focusedLabelColor = Color(0xFF555555),
            unfocusedLabelColor = Color(0xFF555555),
            focusedBorderColor = ThemeYellow,
            unfocusedBorderColor = Color.Transparent,
            cursorColor = Color(0xFF111111)
        ),
        shape = RoundedCornerShape(22.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    )
}

@Composable
private fun CornerIdentity(mac: String, id: String, modifier: Modifier = Modifier, maxWidth: Dp) {
    Column(
        modifier = modifier.width(maxWidth),
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

private fun minDp(first: Dp, second: Dp): Dp = if (first < second) first else second
