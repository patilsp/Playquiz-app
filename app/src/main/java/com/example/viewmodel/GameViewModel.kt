package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Attempt
import com.example.data.model.Question
import com.example.data.model.Quiz
import com.example.data.repository.QuizRepository
import com.example.data.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface GameplayState {
    data object Idle : GameplayState
    data object LoadingQuiz : GameplayState
    data class Playing(
        val quiz: Quiz,
        val currentQuestionIndex: Int,
        val currentQuestion: Question,
        val selectedOptionIndex: Int = -1,
        val isChecked: Boolean = false,
        val isCorrect: Boolean = false,
        val lives: Int = 3,
        val score: Int = 0,
        val timeSeconds: Int = 0
    ) : GameplayState
    data class Completed(
        val quizTitle: String,
        val subject: String,
        val score: Int,
        val totalQuestions: Int,
        val percentage: Int,
        val xpEarned: Int,
        val starsEarned: Int,
        val timeTakenSeconds: Int,
        val unlockedBadges: List<String>
    ) : GameplayState
    data class Error(val message: String) : GameplayState
}

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val quizRepository = QuizRepository(application)
    private val userRepository = UserRepository(application)

    private val _uiState = MutableStateFlow<GameplayState>(GameplayState.Idle)
    val uiState: StateFlow<GameplayState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    fun startQuiz(schoolClass: Int, subject: String, topic: String, difficulty: String = "Easy") {
        viewModelScope.launch {
            _uiState.value = GameplayState.LoadingQuiz
            try {
                val quiz = quizRepository.getOrCreateQuiz(schoolClass, subject, topic, difficulty)
                if (quiz.questions.isEmpty()) {
                    _uiState.value = GameplayState.Error("No questions available for this topic.")
                    return@launch
                }

                _uiState.value = GameplayState.Playing(
                    quiz = quiz,
                    currentQuestionIndex = 0,
                    currentQuestion = quiz.questions[0],
                    selectedOptionIndex = -1,
                    isChecked = false,
                    isCorrect = false,
                    lives = 3,
                    score = 0,
                    timeSeconds = 0
                )

                startTimer()
            } catch (e: Exception) {
                _uiState.value = GameplayState.Error(e.localizedMessage ?: "Failed to load quiz")
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val current = _uiState.value
                if (current is GameplayState.Playing) {
                    _uiState.value = current.copy(timeSeconds = current.timeSeconds + 1)
                } else {
                    break
                }
            }
        }
    }

    fun selectOption(optionIndex: Int) {
        val current = _uiState.value
        if (current is GameplayState.Playing && !current.isChecked) {
            _uiState.value = current.copy(selectedOptionIndex = optionIndex)
        }
    }

    fun checkAnswer() {
        val current = _uiState.value
        if (current is GameplayState.Playing && current.selectedOptionIndex != -1 && !current.isChecked) {
            val isCorrect = current.selectedOptionIndex == current.currentQuestion.correctIndex
            val newScore = if (isCorrect) current.score + 1 else current.score
            val newLives = if (!isCorrect) (current.lives - 1).coerceAtLeast(0) else current.lives

            _uiState.value = current.copy(
                isChecked = true,
                isCorrect = isCorrect,
                score = newScore,
                lives = newLives
            )
        }
    }

    fun nextQuestion() {
        val current = _uiState.value
        if (current is GameplayState.Playing) {
            if (current.lives <= 0 || current.currentQuestionIndex + 1 >= current.quiz.questions.size) {
                finishQuiz(current)
            } else {
                val nextIdx = current.currentQuestionIndex + 1
                _uiState.value = current.copy(
                    currentQuestionIndex = nextIdx,
                    currentQuestion = current.quiz.questions[nextIdx],
                    selectedOptionIndex = -1,
                    isChecked = false,
                    isCorrect = false
                )
            }
        }
    }

    private fun finishQuiz(state: GameplayState.Playing) {
        timerJob?.cancel()
        val total = state.quiz.questions.size
        val percentage = ((state.score.toFloat() / total) * 100).toInt()
        val xpEarned = state.score * 15 + if (percentage >= 80) 50 else 20
        val starsEarned = when {
            percentage == 100 -> 3
            percentage >= 60 -> 2
            percentage >= 40 -> 1
            else -> 0
        }

        viewModelScope.launch {
            userRepository.addXpAndCheckStreak(xpEarned, starsEarned)

            val attempt = Attempt(
                quizId = state.quiz.quizId,
                quizTitle = state.quiz.title,
                subject = state.quiz.subject,
                score = state.score,
                totalQuestions = total,
                xpEarned = xpEarned,
                starsEarned = starsEarned,
                timeTakenSeconds = state.timeSeconds
            )
            quizRepository.saveQuizAttempt(attempt)

            val unlocked = mutableListOf<String>()
            if (percentage == 100) unlocked.add("perfect_score")
            if (state.quiz.subject == "Mathematics" && percentage == 100) unlocked.add("math_genius")

            _uiState.value = GameplayState.Completed(
                quizTitle = state.quiz.title,
                subject = state.quiz.subject,
                score = state.score,
                totalQuestions = total,
                percentage = percentage,
                xpEarned = xpEarned,
                starsEarned = starsEarned,
                timeTakenSeconds = state.timeSeconds,
                unlockedBadges = unlocked
            )
        }
    }

    fun resetGame() {
        timerJob?.cancel()
        _uiState.value = GameplayState.Idle
    }
}
