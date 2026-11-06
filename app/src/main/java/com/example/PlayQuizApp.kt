package com.example.PlayQuizApp

import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.User
import com.example.viewmodel.AuthUiState
import com.example.viewmodel.AuthViewModel
import com.example.ui.components.NavigationTab
import com.example.ui.components.PlayQuizBottomNav
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PlayScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.QuizGameplayScreen
import com.example.ui.screens.QuizResultsScreen
import com.example.ui.screens.RewardsScreen
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.GameplayState
import com.example.viewmodel.UserViewModel

enum class AppScreen {
    AUTH,
    ONBOARDING,
    MAIN_TABS,
    GAMEPLAY,
    RESULTS
}

@Composable
fun PlayQuizApp(
    authViewModel: AuthViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel(),
    gameViewModel: GameViewModel = viewModel()
) {
    val context = LocalContext.current
    val authState by authViewModel.uiState.collectAsState()
    val currentUserProfile by userViewModel.currentUser.collectAsState()
    val gameState by gameViewModel.uiState.collectAsState()

    var currentScreen by remember { mutableStateOf(AppScreen.AUTH) }
    var currentTab by remember { mutableStateOf(NavigationTab.HOME) }
    var isGuestMode by remember { mutableStateOf(false) }

    // Fallback user profile for Guest mode or offline startup
    var guestUser by remember {
        mutableStateOf(
            User(
                uid = "guest_123",
                displayName = "Quiz Champion",
                schoolClass = 4,
                xp = 120,
                stars = 15,
                streakDays = 3,
                badges = listOf("first_quiz", "math_genius")
            )
        )
    }

    val activeUser = currentUserProfile ?: guestUser

    // React to Auth state transitions
    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthUiState.Authenticated -> {
                isGuestMode = false
                userViewModel.loadOrCreateUser(state.user.uid, state.user.displayName ?: "Quiz Champion")
                if (currentScreen == AppScreen.AUTH) {
                    currentScreen = AppScreen.MAIN_TABS
                }
            }
            is AuthUiState.Unauthenticated -> {
                if (!isGuestMode) {
                    currentScreen = AppScreen.AUTH
                }
            }
            else -> {}
        }
    }

    // Auto-check sign-in state on launch
    LaunchedEffect(Unit) {
        authViewModel.checkAutoSignIn(context)
    }

    // React to Gameplay state completed
    LaunchedEffect(gameState) {
        if (gameState is GameplayState.Completed) {
            currentScreen = AppScreen.RESULTS
        }
    }

    when (currentScreen) {
        AppScreen.AUTH -> {
            AuthScreen(
                isLoading = authState is AuthUiState.Loading,
                onGoogleSignInClick = { activity ->
                    authViewModel.signInWithGoogle(activity) { err -> }
                },
                onGuestContinue = {
                    isGuestMode = true
                    currentScreen = AppScreen.ONBOARDING
                }
            )
        }

        AppScreen.ONBOARDING -> {
            OnboardingScreen(
                initialClass = activeUser.schoolClass,
                initialName = activeUser.displayName,
                onComplete = { schoolClass, name ->
                    if (isGuestMode) {
                        guestUser = guestUser.copy(schoolClass = schoolClass, displayName = name)
                    } else {
                        userViewModel.updateClass(schoolClass)
                    }
                    currentScreen = AppScreen.MAIN_TABS
                }
            )
        }

        AppScreen.MAIN_TABS -> {
            Scaffold(
                bottomBar = {
                    PlayQuizBottomNav(
                        currentTab = currentTab,
                        onTabSelected = { currentTab = it }
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (currentTab) {
                        NavigationTab.HOME -> {
                            HomeScreen(
                                user = activeUser,
                                onStartQuiz = { subject, topic ->
                                    gameViewModel.startQuiz(activeUser.schoolClass, subject, topic)
                                    currentScreen = AppScreen.GAMEPLAY
                                },
                                onPlayCustomTopic = {
                                    currentTab = NavigationTab.PLAY
                                },
                                onProfileClick = {
                                    currentTab = NavigationTab.PROFILE
                                }
                            )
                        }

                        NavigationTab.EXPLORE -> {
                            ExploreScreen(
                                user = activeUser,
                                onSelectTopic = { subject, topic, difficulty ->
                                    gameViewModel.startQuiz(activeUser.schoolClass, subject, topic, difficulty)
                                    currentScreen = AppScreen.GAMEPLAY
                                }
                            )
                        }

                        NavigationTab.PLAY -> {
                            PlayScreen(
                                user = activeUser,
                                onGenerateQuiz = { subject, topic, difficulty ->
                                    gameViewModel.startQuiz(activeUser.schoolClass, subject, topic, difficulty)
                                    currentScreen = AppScreen.GAMEPLAY
                                }
                            )
                        }

                        NavigationTab.REWARDS -> {
                            RewardsScreen(user = activeUser)
                        }

                        NavigationTab.PROFILE -> {
                            ProfileScreen(
                                user = activeUser,
                                onUpdateClass = { newClass ->
                                    if (isGuestMode) {
                                        guestUser = guestUser.copy(schoolClass = newClass)
                                    } else {
                                        userViewModel.updateClass(newClass)
                                    }
                                },
                                onSignOut = {
                                    isGuestMode = false
                                    authViewModel.signOut(context)
                                    currentScreen = AppScreen.AUTH
                                }
                            )
                        }
                    }
                }
            }
        }

        AppScreen.GAMEPLAY -> {
            QuizGameplayScreen(
                state = gameState,
                onSelectOption = { gameViewModel.selectOption(it) },
                onCheckAnswer = { gameViewModel.checkAnswer() },
                onNextQuestion = { gameViewModel.nextQuestion() },
                onCloseQuiz = {
                    gameViewModel.resetGame()
                    currentScreen = AppScreen.MAIN_TABS
                }
            )
        }

        AppScreen.RESULTS -> {
            val completedState = gameState as? GameplayState.Completed
            if (completedState != null) {
                QuizResultsScreen(
                    state = completedState,
                    onPlayAgain = {
                        gameViewModel.resetGame()
                        currentScreen = AppScreen.MAIN_TABS
                    },
                    onHomeClick = {
                        gameViewModel.resetGame()
                        currentTab = NavigationTab.HOME
                        currentScreen = AppScreen.MAIN_TABS
                    }
                )
            } else {
                currentScreen = AppScreen.MAIN_TABS
            }
        }
    }
}
