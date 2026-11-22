package org.discoursey.android.ui.screens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.discoursey.android.R
import org.discoursey.android.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: MainViewModel,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var url by remember { mutableStateOf("https://meta.discourse.org") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isApiKeyMode by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var isConnecting by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Branding Header
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(Color(0xFF0284C7), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Forum,
                contentDescription = "Discoursey",
                tint = Color.White,
                modifier = Modifier.size(46.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Discoursey",
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 28.sp),
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Connect to any Discourse community",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
        )

        // Error Banner
        if (errorText != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF7F1D1D)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = errorText ?: "",
                    color = Color(0xFFFECACA),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }

        // URL Input
        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            label = { Text("Discourse Instance URL") },
            placeholder = { Text("https://meta.discourse.org") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Auth Mode Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = !isApiKeyMode,
                onClick = { isApiKeyMode = false },
                label = { Text("Password (Standard)") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = isApiKeyMode,
                onClick = { isApiKeyMode = true },
                label = { Text("API Key") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Username Input
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text(if (isApiKeyMode) "API Username (Optional for User API Key)" else "Username or Email") },
            placeholder = { Text("your_user") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Password or API Key Input
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                if (it.length == 64 && !isApiKeyMode) {
                    isApiKeyMode = true
                }
            },
            label = { Text(if (isApiKeyMode) "User API Key / Admin API Key" else "Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            visualTransformation = if (isApiKeyMode) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = if (isApiKeyMode) KeyboardType.Ascii else KeyboardType.Password),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Connect Button
        Button(
            onClick = {
                if (url.isBlank()) {
                    errorText = "Please enter your Discourse forum URL."
                    return@Button
                }
                isConnecting = true
                errorText = null
                val useApiKey = isApiKeyMode || (password.length == 64)
                viewModel.login(url, username, password, isApiKey = useApiKey) { success, msg ->
                    isConnecting = false
                    if (success) {
                        onLoginSuccess()
                    } else {
                        errorText = msg ?: "Could not connect to forum instance."
                    }
                }
            },
            enabled = !isConnecting,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            if (isConnecting) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Connect to Forum", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Demo / Public Instance Button
        OutlinedButton(
            onClick = {
                isConnecting = true
                viewModel.useDemoInstance {
                    isConnecting = false
                    onLoginSuccess()
                }
            },
            enabled = !isConnecting,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Try Demo / Public Instance", fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Discoursey connects directly to your forum's standard API.\nOpen Source GPL-3.0 License.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
