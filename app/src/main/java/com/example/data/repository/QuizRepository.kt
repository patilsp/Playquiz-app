package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.Attempt
import com.example.data.model.Quiz
import com.example.data.remote.AiQuizGenerator
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class QuizRepository(context: Context) {
    private val db = FirebaseFirestore.getInstance(
        context.applicationContext.getString(R.string.firestore_database_id)
    )
    private val auth = Firebase.auth

    suspend fun getOrCreateQuiz(
        schoolClass: Int,
        subject: String,
        topic: String,
        difficulty: String = "Easy"
    ): Quiz {
        val topicKey = "class_${schoolClass}_${subject.lowercase().replace(" ", "_")}_${topic.lowercase().replace(" ", "_")}_${difficulty.lowercase()}"

        // 1. Reusable Quiz Engine: Query Firestore for existing public approved quiz
        try {
            val querySnapshot = db.collection("quizzes")
                .whereEqualTo("topicKey", topicKey)
                .whereEqualTo("isApproved", true)
                .limit(1)
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                val existingQuiz = querySnapshot.documents.first().toObject(Quiz::class.java)
                if (existingQuiz != null && existingQuiz.questions.isNotEmpty()) {
                    Log.d("QuizRepository", "Reusing existing Firestore quiz for $topicKey")
                    if (auth.currentUser != null) {
                        try {
                            db.collection("quizzes").document(existingQuiz.quizId)
                                .update("playCount", FieldValue.increment(1))
                        } catch (e: Exception) {
                            Log.w("QuizRepository", "Could not increment playCount", e)
                        }
                    }
                    return existingQuiz
                }
            }
        } catch (e: Exception) {
            Log.w("QuizRepository", "Failed to search existing quiz in Firestore, proceeding to generator", e)
        }

        // 2. Generate new quiz using Gemini AI via Firebase AI Logic
        val currentUid = auth.currentUser?.uid
        val createdById = currentUid ?: "system_guest"

        val newQuiz = AiQuizGenerator.generateQuiz(
            schoolClass = schoolClass,
            subject = subject,
            topic = topic,
            difficulty = difficulty,
            createdById = createdById
        )

        // 3. Save new quiz to Firestore ONLY if user is authenticated
        if (currentUid != null) {
            try {
                saveQuizToFirestore(newQuiz.copy(createdById = currentUid))
            } catch (e: Exception) {
                Log.e("QuizRepository", "Failed to cache AI quiz to Firestore", e)
            }
        }

        return newQuiz
    }

    private suspend fun saveQuizToFirestore(quiz: Quiz) {
        val questionsPayload = quiz.questions.map { q ->
            mapOf(
                "id" to q.id,
                "question" to q.question,
                "options" to q.options,
                "correctIndex" to q.correctIndex,
                "explanation" to q.explanation,
                "illustration" to q.illustration
            )
        }

        val docRef = db.collection("quizzes").document(quiz.quizId)
        docRef.set(
            mapOf(
                "quizId" to quiz.quizId,
                "title" to quiz.title,
                "description" to quiz.description,
                "schoolClass" to quiz.schoolClass,
                "subject" to quiz.subject,
                "topic" to quiz.topic,
                "topicKey" to quiz.topicKey,
                "difficulty" to quiz.difficulty,
                "questions" to questionsPayload,
                "estimatedMinutes" to quiz.estimatedMinutes,
                "createdById" to quiz.createdById,
                "isApproved" to quiz.isApproved,
                "language" to quiz.language,
                "playCount" to quiz.playCount,
                "createdAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    suspend fun saveQuizAttempt(attempt: Attempt) {
        val uid = auth.currentUser?.uid ?: return // Skip Firestore write for unauthenticated guest sessions
        val attemptsRef = db.collection("users").document(uid).collection("attempts").document()
        attemptsRef.set(
            mapOf(
                "attemptId" to attemptsRef.id,
                "userId" to uid,
                "quizId" to attempt.quizId,
                "quizTitle" to attempt.quizTitle,
                "subject" to attempt.subject,
                "score" to attempt.score,
                "totalQuestions" to attempt.totalQuestions,
                "xpEarned" to attempt.xpEarned,
                "starsEarned" to attempt.starsEarned,
                "timeTakenSeconds" to attempt.timeTakenSeconds,
                "completedAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    fun observeUserAttempts(uid: String): Flow<List<Attempt>> {
        if (auth.currentUser == null) return flowOf(emptyList())
        return db.collection("users").document(uid).collection("attempts")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Attempt::class.java)?.copy(attemptId = doc.id)
                }
            }
            .catch { e ->
                Log.e("QuizRepository", "Error observing attempts", e)
                emit(emptyList())
            }
    }
}
