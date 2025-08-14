package com.aiverse.app.ui.theme


import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


private val LightColors = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White
)

private val DarkColors = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.Black
)

@Composable
fun AiverseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // ✅ Detect system theme
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        shapes = AiverseShapes, // ✅ Now valid
        content = content
    )

}
