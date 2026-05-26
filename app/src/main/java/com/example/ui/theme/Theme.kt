package com.example.ui.theme

import android.os.Build
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SleekDarkAccent,
    onPrimary = SleekDarkBg,
    primaryContainer = SleekDarkContainer,
    onPrimaryContainer = SleekDarkOnContainer,
    secondary = SleekDarkContainer,
    onSecondary = SleekDarkOnContainer,
    tertiary = GoldPulse,
    onTertiary = SleekDarkBg,
    background = SleekDarkBg,
    onBackground = SleekDarkTextPrimary,
    surface = SleekDarkSurface,
    onSurface = SleekDarkTextPrimary,
    surfaceVariant = SleekDarkContainer,
    onSurfaceVariant = SleekDarkTextSecondary,
    error = ErrorPastel,
    onError = SleekDarkBg
)

private val LightColorScheme = lightColorScheme(
    primary = SleekLightAccent,
    onPrimary = Color.White,
    primaryContainer = SleekLightContainer,
    onPrimaryContainer = SleekLightOnContainer,
    secondary = SleekLightSurface,
    onSecondary = SleekLightOnContainer,
    tertiary = GoldPulse,
    onTertiary = SleekLightTextPrimary,
    background = SleekLightBg,
    onBackground = SleekLightTextPrimary,
    surface = Color.White,
    onSurface = SleekLightTextPrimary,
    surfaceVariant = SleekLightSurface,
    onSurfaceVariant = SleekLightTextSecondary,
    error = ErrorPastel,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disable dynamic colors to enforce our custom premium design
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
