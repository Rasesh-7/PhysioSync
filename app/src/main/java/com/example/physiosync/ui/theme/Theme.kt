package com.example.physiosync.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = TealContainer,
    onPrimary = TealOnContainer,
    primaryContainer = TealPrimary,
    onPrimaryContainer = TealOnPrimary,
    secondary = CyanContainer,
    onSecondary = CyanOnContainer,
    background = NeutralDarkBackground,
    surface = NeutralDarkSurface,
    surfaceVariant = NeutralDarkSurfaceVariant,
    error = StatusErrorContainer,
    onError = StatusError
)

private val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = TealOnPrimary,
    primaryContainer = TealContainer,
    onPrimaryContainer = TealOnContainer,
    secondary = CyanSecondary,
    onSecondary = CyanOnSecondary,
    secondaryContainer = CyanContainer,
    onSecondaryContainer = CyanOnContainer,
    background = NeutralLightBackground,
    surface = NeutralLightSurface,
    surfaceVariant = NeutralLightSurfaceVariant,
    error = StatusError,
    onError = Color.White
)

@Composable
fun PhysioSyncTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}