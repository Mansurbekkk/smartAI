package com.example.myapplication.ui.theme

import androidx.compose.ui.graphics.Color

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val KidsColorScheme = lightColorScheme(
    primary = KidsPrimary,
    secondary = KidsSecondary,
    tertiary = KidsTertiary,
    background = Color(0xFFF0F4F8),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color(0xFF2C3E50)
)

private val TeensColorScheme = darkColorScheme(
    primary = TeensPrimary,
    secondary = TeensSecondary,
    tertiary = TeensTertiary,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFFECEFF1)
)

private val DarkColorScheme = darkColorScheme(
    primary = TeensPrimary,
    secondary = TeensSecondary,
    tertiary = TeensTertiary
)

private val LightColorScheme = lightColorScheme(
    primary = KidsPrimary,
    secondary = KidsSecondary,
    tertiary = KidsTertiary
)

@Composable
fun MyApplicationTheme(
    isTeens: Boolean = false,
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Set to false to prioritize our custom palettes
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isTeens -> TeensColorScheme
        else -> KidsColorScheme
    }

    MaterialTheme(
      colorScheme = colorScheme,
      typography = Typography,
      content = content
    )
}