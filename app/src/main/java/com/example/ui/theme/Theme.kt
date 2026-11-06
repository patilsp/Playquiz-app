package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

/**
 * PlayQuiz Light Color Scheme strictly using:
 * Primary: #7655E8
 * Background: #F8F7FC
 */
private val PlayQuizLightColorScheme = lightColorScheme(
    primary = PrimaryPurple,
    onPrimary = Color.White,
    primaryContainer = SecondaryLavender,
    onPrimaryContainer = PrimaryText,
    secondary = MintGreen,
    onSecondary = PrimaryText,
    secondaryContainer = MintGreen,
    onSecondaryContainer = PrimaryText,
    tertiary = PeachAccent,
    onTertiary = PrimaryText,
    tertiaryContainer = SunnyYellow,
    background = AppBackground,
    onBackground = PrimaryText,
    surface = PrimarySurface,
    onSurface = PrimaryText,
    surfaceVariant = CardBorderColor,
    outline = CardBorderColor
)

@Composable
fun PlayQuizTheme(
    darkTheme: Boolean = false, // Always enforce PlayQuiz light theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalSpacing provides PlayQuizSpacing()
    ) {
        MaterialTheme(
            colorScheme = PlayQuizLightColorScheme,
            typography = Typography,
            content = content
        )
    }
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    PlayQuizTheme(darkTheme = false, dynamicColor = false, content = content)
}
