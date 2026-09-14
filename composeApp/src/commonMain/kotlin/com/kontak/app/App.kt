package com.kontak.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.kontak.app.data.ApiResult
import com.kontak.app.data.AuthRepository
import com.kontak.app.model.SessionUser
import com.kontak.app.ui.theme.KontakTheme
import kotlinx.coroutines.launch

private enum class AppScreen { LOADING, LOGIN, DASHBOARD }

@Composable
fun App() {
    KontakTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            KontakRoot()
        }
    }
}

@Composable
private fun KontakRoot() {
    val authRepository = remember { AuthRepository() }
    var screen by remember { mutableStateOf(AppScreen.LOADING) }
    var currentUser by remember { mutableStateOf<SessionUser?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        when (val result = authRepository.getSession()) {
            is ApiResult.Success -> {
                currentUser = result.data
                screen = if (result.data != null) AppScreen.DASHBOARD else AppScreen.LOGIN
            }
            is ApiResult.Error -> screen = AppScreen.LOGIN
        }
    }

    when (screen) {
        AppScreen.LOADING -> LoadingScreen()
        AppScreen.LOGIN -> LoginScreen(
            authRepository = authRepository,
            onLoginSuccess = { user ->
                currentUser = user
                screen = AppScreen.DASHBOARD
            }
        )
        AppScreen.DASHBOARD -> DashboardPlaceholder(
            user = currentUser,
            onLogout = {
                coroutineScope.launch {
                    authRepository.logout()
                    currentUser = null
                    screen = AppScreen.LOGIN
                }
            }
        )
    }
}

@Composable
private fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun LoginScreen(
    authRepository: AuthRepository,
    onLoginSuccess: (SessionUser?) -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Kontak", style = MaterialTheme.typography.headlineMedium)
        Text("Connexion", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 8.dp, bottom = 24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mot de passe") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        )

        errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }

        Button(
            enabled = !isLoading,
            onClick = {
                errorMessage = null
                isLoading = true
                coroutineScope.launch {
                    when (val result = authRepository.login(email.trim(), password)) {
                        is ApiResult.Success -> {
                            val sessionResult = authRepository.getSession()
                            isLoading = false
                            if (sessionResult is ApiResult.Success) {
                                onLoginSuccess(sessionResult.data)
                            } else {
                                onLoginSuccess(null)
                            }
                        }
                        is ApiResult.Error -> {
                            isLoading = false
                            errorMessage = result.message
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
        ) {
            Text(if (isLoading) "Connexion…" else "Se connecter")
        }
    }
}

@Composable
private fun DashboardPlaceholder(user: SessionUser?, onLogout: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Bonjour ${user?.profile?.nom ?: user?.email ?: ""}", style = MaterialTheme.typography.headlineMedium)
        Text("Crédits : ${user?.profile?.credits ?: 0}", modifier = Modifier.padding(top = 8.dp))
        Button(onClick = onLogout, modifier = Modifier.padding(top = 24.dp)) {
            Text("Déconnexion")
        }
    }
}