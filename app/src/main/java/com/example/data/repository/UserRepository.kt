package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.User
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserRepository(context: Context) {
    private val db = FirebaseFirestore.getInstance(
        context.applicationContext.getString(R.string.firestore_database_id)
    )
    private val auth = Firebase.auth

    fun observeUser(uid: String): Flow<User?> {
        if (auth.currentUser == null) return flowOf(null)
        return db.collection("users").document(uid)
            .snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(User::class.java)?.copy(uid = snapshot.id)
                } else {
                    null
                }
            }
            .catch { e ->
                Log.e("UserRepository", "Error observing user profile", e)
                emit(null)
            }
    }

    suspend fun ensureUserExists(uid: String, displayName: String, schoolClass: Int = 4): User {
        if (auth.currentUser == null) {
            return User(uid = uid, displayName = displayName, schoolClass = schoolClass)
        }

        val docRef = db.collection("users").document(uid)
        val snapshot = docRef.get().await()
        if (snapshot.exists()) {
            return snapshot.toObject(User::class.java)?.copy(uid = uid) ?: User(uid = uid)
        } else {
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val initialUser = User(
                uid = uid,
                displayName = displayName.ifBlank { "Quiz Champion" },
                schoolClass = schoolClass,
                xp = 100,
                stars = 10,
                streakDays = 1,
                lastActiveDate = todayStr,
                badges = listOf("first_quiz")
            )
            docRef.set(
                mapOf(
                    "uid" to uid,
                    "displayName" to initialUser.displayName,
                    "avatar" to initialUser.avatar,
                    "schoolClass" to initialUser.schoolClass,
                    "xp" to initialUser.xp,
                    "stars" to initialUser.stars,
                    "streakDays" to initialUser.streakDays,
                    "lastActiveDate" to initialUser.lastActiveDate,
                    "badges" to initialUser.badges,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            return initialUser
        }
    }

    suspend fun updateUserProfile(schoolClass: Int, displayName: String, avatar: String) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).update(
            mapOf(
                "schoolClass" to schoolClass,
                "displayName" to displayName,
                "avatar" to avatar,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    suspend fun addXpAndCheckStreak(xpGained: Int, starsGained: Int) {
        val uid = auth.currentUser?.uid ?: return
        val docRef = db.collection("users").document(uid)
        val snapshot = docRef.get().await()

        val currentUser = snapshot.toObject(User::class.java) ?: return
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        var newStreak = currentUser.streakDays
        if (currentUser.lastActiveDate != todayStr) {
            newStreak = if (currentUser.lastActiveDate.isNotBlank()) {
                currentUser.streakDays + 1
            } else {
                1
            }
        }

        val updatedBadges = currentUser.badges.toMutableSet()
        val newXp = currentUser.xp + xpGained

        if (newXp >= 500) updatedBadges.add("quiz_champion")
        if (newStreak >= 7) updatedBadges.add("7_day_streak")

        docRef.update(
            mapOf(
                "xp" to FieldValue.increment(xpGained.toLong()),
                "stars" to FieldValue.increment(starsGained.toLong()),
                "streakDays" to newStreak,
                "lastActiveDate" to todayStr,
                "badges" to updatedBadges.toList(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }
}
