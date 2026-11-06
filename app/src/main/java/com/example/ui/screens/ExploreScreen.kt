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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.User
import com.example.ui.theme.MintGreen
import com.example.ui.theme.PeachAccent
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SoftLavender

data class MathTopicItem(
    val title: String,
    val subtitle: String,
    val category: String,
    val icon: ImageVector,
    val iconBgColor: Color,
    val completed: Int,
    val total: Int
)

val MATH_TOPICS = listOf(
    MathTopicItem("Counting Fun", "Learn numbers 1 to 10", "Numbers", Icons.Default.Numbers, PeachAccent, 8, 10),
    MathTopicItem("Add & Subtract", "Learn basic addition and subtraction", "Basics", Icons.Default.SquareFoot, MintGreen, 6, 12),
    MathTopicItem("Shapes Around Us", "Explore different shapes", "Shapes", Icons.Default.Category, SkyBlue, 4, 8),
    MathTopicItem("Patterns Play", "Find and complete patterns", "Basics", Icons.Default.PieChart, SoftLavender, 3, 6)
)

@Composable
fun ExploreScreen(
    user: User,
    onSelectTopic: (subject: String, topic: String, difficulty: String) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("All") }

    val filteredList = if (selectedFilter == "All") {
        MATH_TOPICS
    } else {
        MATH_TOPICS.filter { it.category == selectedFilter }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Top Action Bar with centered Title "Math" and Trophy Pill
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "◀",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PrimaryText
                )
            }

            Text(
                text = "Math",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = PrimaryText
            )

            Surface(
                shape = CircleShape,
                color = SoftLavender,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Trophy",
                        tint = PrimaryPurple,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hero Card with 3D Purple Math Monster Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("math_hero_banner"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF6EE7B7),
                                Color(0xFF3B82F6)
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Let's Make\nMath Fun!",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }

                    Image(
                        painter = painterResource(id = R.drawable.mascot_purple_math_monster_1793979270557),
                        contentDescription = "Math Monster",
                        modifier = Modifier
                            .size(105.dp)
                            .clip(RoundedCornerShape(20.dp))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filter Pills (All, Basics, Numbers, Shapes)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val filters = listOf("All", "Basics", "Numbers", "Shapes")
            items(filters) { filter ->
                val isSelected = selectedFilter == filter
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { selectedFilter = filter }
                        .testTag("filter_$filter"),
                    color = if (isSelected) PrimaryPurple else MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(20.dp),
                    shadowElevation = if (isSelected) 4.dp else 1.dp
                ) {
                    Text(
                        text = filter,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else PrimaryText,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Topic List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredList) { topic ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectTopic("Mathematics", topic.title, "Easy") }
                        .testTag("topic_card_${topic.title}"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = topic.iconBgColor,
                                modifier = Modifier.size(54.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = topic.icon,
                                        contentDescription = topic.title,
                                        tint = PrimaryText,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = topic.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = topic.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SecondaryText
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    LinearProgressIndicator(
                                        progress = { topic.completed.toFloat() / topic.total },
                                        modifier = Modifier
                                            .width(100.dp)
                                            .height(6.dp)
                                            .clip(CircleShape),
                                        color = PrimaryPurple,
                                        trackColor = SoftLavender
                                    )

                                    Text(
                                        text = "${topic.completed}/${topic.total}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SecondaryText,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(PrimaryPurple),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Play Topic",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
