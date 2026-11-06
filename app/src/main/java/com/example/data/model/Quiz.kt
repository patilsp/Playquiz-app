package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class Quiz(
    @DocumentId val quizId: String = "",
    val title: String = "",
    val description: String = "",
    val schoolClass: Int = 4,
    val subject: String = "Mathematics",
    val topic: String = "Multiplication",
    val topicKey: String = "class_4_mathematics_multiplication_easy",
    val difficulty: String = "Easy",
    val questions: List<Question> = emptyList(),
    val estimatedMinutes: Int = 5,
    val createdById: String = "system",
    val isApproved: Boolean = true,
    val language: String = "English",
    val playCount: Int = 1,
    @ServerTimestamp val createdAt: Timestamp? = null
)
