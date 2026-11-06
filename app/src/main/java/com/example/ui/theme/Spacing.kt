package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * PlayQuiz 4px/4dp-based spacing system.
 * All spacing values strictly follow the 4dp grid.
 */
data class PlayQuizSpacing(
    val xxs: Dp = 4.dp,   // 4px
    val xs: Dp = 8.dp,    // 8px
    val sm: Dp = 12.dp,   // 12px
    val md: Dp = 16.dp,   // 16px
    val lg: Dp = 20.dp,   // 20px
    val xl: Dp = 24.dp,   // 24px
    val xxl: Dp = 28.dp,  // 28px
    val xxxl: Dp = 32.dp, // 32px
    val huge: Dp = 48.dp  // 48px
)

val LocalSpacing = staticCompositionLocalOf { PlayQuizSpacing() }

val MaterialTheme.spacing: PlayQuizSpacing
    @Composable
    @ReadOnlyComposable
    get() = LocalSpacing.current
