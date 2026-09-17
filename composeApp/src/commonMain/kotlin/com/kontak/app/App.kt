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
import com.kontak.app.model.Profile
import com.kontak.app.model.SessionUser
import com.kontak.app.ui.screens.AbonnementScreen
import com.kontak.app.ui.screens.CompleteProfileScreen
import com.kontak.app.ui.screens.DashboardScreen
import com.kontak.app.ui.theme.KontakTheme
import kotlinx.coroutines.launch

private enum class AppScreen { LOADING, LOGIN, COMPLETE_PROFILE, DASHBOARD, ABONNEMENT }

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

    suspend fun refreshSessionAndRoute() {
        when (val result = authRepository.getSession()) {
            is ApiResult.Success -> {
                currentUser = result.data
                screen = when {
                    result.data == null -> AppScreen.LOGIN
                    result.data.profile == null -> AppScreen.COMPLETE_PROFILE
                    else -> AppScreen.DASHBOARD
                }
            }
            is ApiResult.Error -> screen = AppScreen.LOGIN
        }
    }

    LaunchedEffect(Unit) {
        refreshSessionAndRoute()
    }

    when (screen) {
        AppScreen.LOADING -> LoadingScreen()

        AppScreen.LOGIN -> LoginScreen(
            authRepository = authRepository,
            onLoginSuccess = {
                coroutineScope.launch { refreshSessionAndRoute() }
            }
        )

        AppScreen.COMPLETE_PROFILE -> CompleteProfileScreen(
            onProfileCreated = { profile ->
                currentUser = currentUser?.copy(profile = profile)
                screen = AppScreen.DASHBOARD
            }
        )

        AppScreen.DASHBOARD -> {
            val profile = currentUser?.profile
            if (profile != null) {
                DashboardScreen(
                    profile = profile,
                    onLogout = {
                        coroutineScope.launch {
                            authRepository.logout()
                            currentUser = null
                            screen = AppScreen.LOGIN
                        }
                    },
                    onBuyCredits = { screen = AppScreen.ABONNEMENT },
                )
            } else {
                // Sécurité : ne devrait pas arriver, mais on repasse par la
                // vérification de session plutôt que d'afficher un écran vide.
                LaunchedEffect(Unit) { refreshSessionAndRoute() }
                LoadingScreen()
            }
        }

        AppScreen.ABONNEMENT -> AbonnementScreen(
            onCreditsUpdated = {
                coroutineScope.launch { refreshSessionAndRoute() }
            },
            onBack = { screen = AppScreen.DASHBOARD },
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
    onLoginSuccess: () -> Unit,
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
                            isLoading = false
                            onLoginSuccess()
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
