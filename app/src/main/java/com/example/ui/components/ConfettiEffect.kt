package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.CoralPeach
import com.example.ui.theme.LavenderPrimary
import com.example.ui.theme.MintGreen
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.StarGold
import kotlin.random.Random

private data class Particle(
    val xRatio: Float,
    val initialYRatio: Float,
    val speed: Float,
    val sizePx: Float,
    val color: Color
)

@Composable
fun ConfettiEffect(modifier: Modifier = Modifier) {
    val particles = remember {
        val colors = listOf(LavenderPrimary, MintGreen, CoralPeach, StarGold, SkyBlue)
        List(40) {
            Particle(
                xRatio = Random.nextFloat(),
                initialYRatio = -Random.nextFloat() * 0.5f,
                speed = 0.3f + Random.nextFloat() * 0.7f,
                sizePx = 8f + Random.nextFloat() * 12f,
                color = colors.random()
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val progress = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confetti_progress"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        particles.forEach { p ->
            val yOffset = ((p.initialYRatio + progress.value * p.speed) % 1.2f) * height
            val xPos = p.xRatio * width

            drawRect(
                color = p.color,
                topLeft = Offset(xPos, yOffset),
                size = Size(p.sizePx, p.sizePx * 1.5f)
            )
        }
    }
}
