package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.User
import com.example.ui.components.DailyChallengeCard
import com.example.ui.components.FloatingMascot
import com.example.ui.components.PlayQuizTopBar
import com.example.ui.theme.MintGreen
import com.example.ui.theme.PeachAccent
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.SunnyYellow

import com.example.ui.theme.spacing

data class CategoryItem(
    val name: String,
    val drawableRes: Int,
    val bgColor: Color,
    val testTag: String
)

val CATEGORIES = listOf(
    CategoryItem("Stories", R.drawable.subject_3d_stories_1793980405722, SoftLavender, "cat_stories"),
    CategoryItem("Math", R.drawable.subject_3d_math_1793980405678, PeachAccent, "cat_math"),
    CategoryItem("Science", R.drawable.subject_3d_science_1793980405636, MintGreen, "cat_science"),
    CategoryItem("Arts", R.drawable.subject_3d_arts_1793980405653, SkyBlue, "cat_arts")
)

@Composable
fun HomeScreen(
    user: User,
    onStartQuiz: (subject: String, topic: String) -> Unit,
    onPlayCustomTopic: () -> Unit,
    onProfileClick: () -> Unit
) {
    val spacing = MaterialTheme.spacing

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // 1. Header with animated floating waving mascot
        PlayQuizTopBar(
            displayName = user.displayName,
            streakDays = user.streakDays,
            stars = user.stars,
            xp = user.xp
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.xl)
        ) {
            // 2. Category Quick Row with 3D Artwork Tiles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CATEGORIES.forEach { category ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { onStartQuiz(category.name, "${category.name} Basics") }
                            .testTag(category.testTag)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = category.bgColor,
                            modifier = Modifier.size(72.dp),
                            shadowElevation = 2.dp
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(spacing.xxs)
                            ) {
                                Image(
                                    painter = painterResource(id = category.drawableRes),
                                    contentDescription = category.name,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(spacing.xs))

                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // 3. Daily Challenge Card with Progress Bar, 3D Star Mascot, and XP Reward
            DailyChallengeCard(
                currentProgress = 3,
                totalTarget = 5,
                xpReward = 50,
                onStartChallenge = { onStartQuiz("General Knowledge", "Daily Challenge") }
            )

            // 4. Continue Learning Card
            Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Continue Learning",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "See all",
                        style = MaterialTheme.typography.labelLarge,
                        color = PrimaryPurple,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onPlayCustomTopic() }
                    )
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onStartQuiz("Science", "The Jungle Adventure") }
                        .testTag("continue_learning_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                            modifier = Modifier.weight(1f)
                        ) {
                            FloatingMascot(
                                drawableRes = R.drawable.mascot_reading_1793977759480,
                                contentDescription = "Jungle Adventure Mascot",
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                                floatDistanceDp = 6f,
                                durationMillis = 1800
                            )

                            Column {
                                Text(
                                    text = "The Jungle Adventure",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Story • Class ${user.schoolClass}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SecondaryText
                                )
                                Spacer(modifier = Modifier.height(spacing.xxs))
                                LinearProgressIndicator(
                                    progress = { 0.6f },
                                    modifier = Modifier
                                        .fillMaxWidth(0.85f)
                                        .height(8.dp)
                                        .clip(CircleShape),
                                    color = PrimaryPurple,
                                    trackColor = SoftLavender
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(PrimaryPurple),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Resume",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            // 5. Recommended For You Cards
            Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recommended for You",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "See all",
                        style = MaterialTheme.typography.labelLarge,
                        color = PrimaryPurple,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onPlayCustomTopic() }
                    )
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm)
                ) {
                    item {
                        Card(
                            modifier = Modifier
                                .width(168.dp)
                                .clickable { onStartQuiz("Science", "Space Explorer") },
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(spacing.sm)) {
                                FloatingMascot(
                                    drawableRes = R.drawable.mascot_reading_1793977759480,
                                    contentDescription = "Space Explorer",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(104.dp)
                                        .clip(RoundedCornerShape(16.dp)),
                                    floatDistanceDp = 8f,
                                    durationMillis = 1500
                                )
                                Spacer(modifier = Modifier.height(spacing.xs))
                                Text(
                                    text = "Space Explorer",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Science",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SecondaryText
                                )
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier
                                .width(168.dp)
                                .clickable { onStartQuiz("General Knowledge", "Dino World") },
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(spacing.sm)) {
                                FloatingMascot(
                                    drawableRes = R.drawable.mascot_green_monster_1793979257265,
                                    contentDescription = "Dino World",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(104.dp)
                                        .clip(RoundedCornerShape(16.dp)),
                                    floatDistanceDp = 8f,
                                    durationMillis = 1700
                                )
                                Spacer(modifier = Modifier.height(spacing.xs))
                                Text(
                                    text = "Dino World",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Story",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SecondaryText
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(spacing.lg))
        }
    }
}


