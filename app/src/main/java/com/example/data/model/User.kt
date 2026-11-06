package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class User(
    @DocumentId val uid: String = "",
    val displayName: String = "Quiz Champion",
    val avatar: String = "mascot_star",
    val schoolClass: Int = 4,
    val xp: Int = 120,
    val stars: Int = 15,
    val streakDays: Int = 3,
    val lastActiveDate: String = "",
    val badges: List<String> = listOf("first_quiz"),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
