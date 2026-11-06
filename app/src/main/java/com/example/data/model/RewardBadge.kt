package com.example.data.model

data class RewardBadge(
    val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val colorHex: Long,
    val isUnlocked: Boolean = false
) {
    companion object {
        fun getAllBadges(unlockedIds: List<String>): List<RewardBadge> {
            val all = listOf(
                RewardBadge("first_quiz", "First Quiz", "Completed your very first quiz!", "🎯", 0xFF8B5CF6),
                RewardBadge("math_genius", "Math Genius", "Scored 100% in a Mathematics quiz!", "🧮", 0xFF3B82F6),
                RewardBadge("science_star", "Science Star", "Mastered 3 Science topics!", "🔬", 0xFF10B981),
                RewardBadge("quiz_champion", "Quiz Champion", "Earned 500+ XP in PlayQuiz!", "🏆", 0xFFF59E0B),
                RewardBadge("7_day_streak", "7-Day Streak", "Played quizzes 7 days in a row!", "🔥", 0xFFFF6B6B),
                RewardBadge("perfect_score", "Perfect Score", "Got every question correct!", "🌟", 0xFFEC4899)
            )
            return all.map { badge ->
                badge.copy(isUnlocked = unlockedIds.contains(badge.id))
            }
        }
    }
}
