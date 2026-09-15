package com.kontak.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

private val KontakColorScheme = lightColorScheme(
    primary = KontakGreen,
    onPrimary = Color.White,
    secondary = KontakGreenDark,
    background = KontakBg,
    surface = KontakBg,
    surfaceVariant = KontakBgSoft,
    onBackground = KontakInk,
    onSurface = KontakInk,
    error = KontakErr,
)

// Sora (UI/texte courant) est la police du site — en attendant l'intégration
// des polices custom (Fraunces/Sora/Space Mono) via des fichiers .ttf dans
// composeResources, on utilise la police système par défaut ici. Migration
// des polices prévue comme prochaine étape.
private val KontakTypography = Typography(
    headlineMedium = TextStyle(fontSize = 24.sp),
    titleLarge = TextStyle(fontSize = 19.sp),
    bodyLarge = TextStyle(fontSize = 15.sp),
    labelSmall = TextStyle(fontSize = 11.sp),
)

@Composable
fun KontakTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = KontakColorScheme,
        typography = KontakTypography,
        content = content,
    )
}