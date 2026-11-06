package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.components.ConfettiEffect
import com.example.ui.components.StatPill
import com.example.ui.theme.CoralPeach
import com.example.ui.theme.MintGreen
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.SunnyYellow
import com.example.viewmodel.GameplayState

@Composable
fun QuizGameplayScreen(
    state: GameplayState,
    onSelectOption: (Int) -> Unit,
    onCheckAnswer: () -> Unit,
    onNextQuestion: () -> Unit,
    onCloseQuiz: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (state) {
            is GameplayState.LoadingQuiz -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = PrimaryPurple, modifier = Modifier.size(50.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Generating Quiz Adventure...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPurple
                    )
                }
            }

            is GameplayState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Oops!",
                        style = MaterialTheme.typography.headlineMedium,
                        color = CoralPeach,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onCloseQuiz,
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                    ) {
                        Text("Back to Home")
                    }
                }
            }

            is GameplayState.Playing -> {
                val totalQuestions = state.quiz.questions.size
                val currentNum = state.currentQuestionIndex + 1
                val progress = currentNum.toFloat() / totalQuestions

                if (state.isChecked && state.isCorrect) {
                    ConfettiEffect()
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        // Top bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onCloseQuiz,
                                modifier = Modifier.testTag("close_gameplay_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = PrimaryText
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StatPill(
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Favorite,
                                            contentDescription = "Lives",
                                            tint = CoralPeach,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    value = "${state.lives}",
                                    backgroundColor = CoralPeach.copy(alpha = 0.15f),
                                    textColor = CoralPeach
                                )

                                StatPill(
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "XP",
                                            tint = SunnyYellow,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    value = "${state.score * 10} XP",
                                    backgroundColor = SunnyYellow.copy(alpha = 0.15f),
                                    textColor = PrimaryText
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Progress Bar & Question Counter
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Question $currentNum of $totalQuestions",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryPurple
                            )
                            Text(
                                text = "${(progress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = SecondaryText
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(CircleShape),
                            color = PrimaryPurple,
                            trackColor = SoftLavender
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Question Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = SoftLavender
                                ) {
                                    Text(
                                        text = state.quiz.subject,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PrimaryPurple,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = state.currentQuestion.question,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PrimaryText
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Options Cards
                        state.currentQuestion.options.forEachIndexed { idx, optionText ->
                            val isSelected = state.selectedOptionIndex == idx
                            val isCorrectIdx = idx == state.currentQuestion.correctIndex

                            val cardBgColor = when {
                                state.isChecked && isCorrectIdx -> MintGreen.copy(alpha = 0.25f)
                                state.isChecked && isSelected && !state.isCorrect -> CoralPeach.copy(alpha = 0.15f)
                                isSelected -> SoftLavender
                                else -> MaterialTheme.colorScheme.surface
                            }

                            val borderColor = when {
                                state.isChecked && isCorrectIdx -> Color(0xFF0D9488)
                                state.isChecked && isSelected && !state.isCorrect -> CoralPeach
                                isSelected -> PrimaryPurple
                                else -> MaterialTheme.colorScheme.outline
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clip(RoundedCornerShape(22.dp))
                                    .clickable(enabled = !state.isChecked) { onSelectOption(idx) }
                                    .border(
                                        width = if (isSelected || (state.isChecked && isCorrectIdx)) 2.dp else 1.dp,
                                        color = borderColor,
                                        shape = RoundedCornerShape(22.dp)
                                    )
                                    .testTag("option_$idx"),
                                shape = RoundedCornerShape(22.dp),
                                colors = CardDefaults.cardColors(containerColor = cardBgColor)
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
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = if (isSelected) PrimaryPurple else SoftLavender,
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "${'A' + idx}",
                                                    style = MaterialTheme.typography.labelLarge,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = if (isSelected) Color.White else PrimaryText
                                                )
                                            }
                                        }

                                        Text(
                                            text = optionText,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                            color = PrimaryText
                                        )
                                    }

                                    if (state.isChecked && isCorrectIdx) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Correct",
                                            tint = Color(0xFF0D9488)
                                        )
                                    } else if (state.isChecked && isSelected && !state.isCorrect) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Incorrect",
                                            tint = CoralPeach
                                        )
                                    }
                                }
                            }
                        }

                        // Explanation Card on check
                        AnimatedVisibility(
                            visible = state.isChecked,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                shape = RoundedCornerShape(22.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (state.isCorrect) MintGreen.copy(alpha = 0.25f) else CoralPeach.copy(alpha = 0.15f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.mascot_happy_1793977759229),
                                        contentDescription = "Mascot Feedback",
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(CircleShape)
                                    )

                                    Column {
                                        Text(
                                            text = if (state.isCorrect) "Awesome! +10 XP" else "Nice try!",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (state.isCorrect) Color(0xFF0D9488) else CoralPeach
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = state.currentQuestion.explanation,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = PrimaryText
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Action Button (Check or Next)
                    if (!state.isChecked) {
                        Button(
                            onClick = onCheckAnswer,
                            enabled = state.selectedOptionIndex != -1,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("check_answer_button"),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                        ) {
                            Text(
                                text = "Check Answer",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Button(
                            onClick = onNextQuestion,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("next_question_button"),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (state.isCorrect) Color(0xFF0D9488) else PrimaryPurple
                            )
                        ) {
                            Text(
                                text = if (currentNum >= totalQuestions || state.lives <= 0) "Finish Quiz!" else "Next Question →",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            else -> {}
        }
    }
}
