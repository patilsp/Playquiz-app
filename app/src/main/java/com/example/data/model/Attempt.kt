package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class Attempt(
    @DocumentId val attemptId: String = "",
    val userId: String = "",
    val quizId: String = "",
    val quizTitle: String = "",
    val subject: String = "General Knowledge",
    val score: Int = 0,
    val totalQuestions: Int = 5,
    val xpEarned: Int = 50,
    val starsEarned: Int = 3,
    val timeTakenSeconds: Int = 60,
    @ServerTimestamp val completedAt: Timestamp? = null
)
