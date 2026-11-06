package com.example.data.remote

import android.util.Log
import com.example.data.model.Question
import com.example.data.model.Quiz
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException

object AiQuizGenerator {
    private const val TAG = "AiQuizGenerator"

    // 1. Define JSON Schema for Gemini structured output
    private val quizSchema = Schema.obj(
        properties = mapOf(
            "title" to Schema.string(description = "Short engaging quiz title for kids"),
            "description" to Schema.string(description = "1 sentence fun description of what kids will learn"),
            "estimatedMinutes" to Schema.integer(description = "Estimated minutes to finish (3 to 10)"),
            "questions" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "question" to Schema.string(description = "Clear age-appropriate question text"),
                        "options" to Schema.array(
                            items = Schema.string(),
                            description = "Exactly 4 distinct answer choices"
                        ),
                        "correctIndex" to Schema.integer(description = "Zero-based index (0, 1, 2, or 3) of the correct answer option"),
                        "explanation" to Schema.string(description = "Short, encouraging 1-2 sentence educational explanation of the answer")
                    )
                ),
                description = "Array of 5 multiple choice questions"
            )
        )
    )

    private val model by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-2.5-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = quizSchema
            }
        )
    }

    suspend fun generateQuiz(
        schoolClass: Int,
        subject: String,
        topic: String,
        difficulty: String,
        createdById: String
    ): Quiz {
        val topicKey = "class_${schoolClass}_${subject.lowercase().replace(" ", "_")}_${topic.lowercase().replace(" ", "_")}_${difficulty.lowercase()}"
        
        val prompt = """
            You are an expert elementary and middle school teacher designing an engaging quiz game for school children in India.
            Class level: Class $schoolClass (Ages approximately ${schoolClass + 5}-${schoolClass + 6}).
            Subject: $subject
            Topic: $topic
            Difficulty Level: $difficulty

            Generate exactly 5 fun, educational, age-appropriate multiple-choice questions.
            Ensure options are clear, exactly 4 choices per question, with exactly one correct option.
            Include a simple, positive explanation for each answer.
        """.trimIndent()

        return try {
            val responseText = withTimeout(12_000L) {
                val result = model.generateContent(prompt)
                result.text
            } ?: throw IllegalStateException("Model returned empty text.")

            Log.d(TAG, "Generated AI Quiz response: $responseText")
            parseQuizJson(responseText, schoolClass, subject, topic, topicKey, difficulty, createdById)
        } catch (e: TimeoutCancellationException) {
            Log.w(TAG, "AI Quiz Generation timed out after 12s, using rich fallback quiz")
            getFallbackQuiz(schoolClass, subject, topic, topicKey, difficulty, createdById)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "AI Quiz Generation failed (${e.message}), using fallback quiz")
            getFallbackQuiz(schoolClass, subject, topic, topicKey, difficulty, createdById)
        }
    }

    private fun parseQuizJson(
        jsonString: String,
        schoolClass: Int,
        subject: String,
        topic: String,
        topicKey: String,
        difficulty: String,
        createdById: String
    ): Quiz {
        val root = JSONObject(jsonString)
        val title = root.optString("title", "$topic Challenge")
        val description = root.optString("description", "Test your knowledge on $topic!")
        val estMin = root.optInt("estimatedMinutes", 5)

        val questionsArray = root.getJSONArray("questions")
        val questionsList = mutableListOf<Question>()

        for (i in 0 until questionsArray.length()) {
            val qObj = questionsArray.getJSONObject(i)
            val qText = qObj.getString("question")
            val optionsArr = qObj.getJSONArray("options")
            val options = mutableListOf<String>()
            for (j in 0 until optionsArr.length()) {
                options.add(optionsArr.getString(j))
            }
            // Ensure 4 options
            while (options.size < 4) {
                options.add("Option ${options.size + 1}")
            }
            val correctIdx = qObj.optInt("correctIndex", 0).coerceIn(0, options.size - 1)
            val explanation = qObj.optString("explanation", "Great job testing your knowledge!")

            questionsList.add(
                Question(
                    id = "q_${i + 1}_${UUID.randomUUID().toString().take(6)}",
                    question = qText,
                    options = options.take(4),
                    correctIndex = correctIdx,
                    explanation = explanation
                )
            )
        }

        return Quiz(
            quizId = UUID.randomUUID().toString(),
            title = title,
            description = description,
            schoolClass = schoolClass,
            subject = subject,
            topic = topic,
            topicKey = topicKey,
            difficulty = difficulty,
            questions = questionsList,
            estimatedMinutes = estMin,
            createdById = createdById,
            isApproved = true,
            playCount = 1
        )
    }

    fun getFallbackQuiz(
        schoolClass: Int,
        subject: String,
        topic: String,
        topicKey: String,
        difficulty: String,
        createdById: String
    ): Quiz {
        val questions = when (subject.lowercase()) {
            "mathematics" -> listOf(
                Question("m1", "What is 12 × 8?", listOf("86", "96", "108", "94"), 1, "12 multiplied by 8 equals 96!"),
                Question("m2", "What is the square root of 64?", listOf("6", "7", "8", "9"), 2, "8 × 8 = 64, so the square root is 8!"),
                Question("m3", "How many sides does a hexagon have?", listOf("5", "6", "7", "8"), 1, "A hexagon has 6 straight sides!"),
                Question("m4", "What is 1/2 + 1/4?", listOf("2/4", "3/4", "1/6", "2/6"), 1, "1/2 is equal to 2/4. 2/4 + 1/4 = 3/4!"),
                Question("m5", "What is 100 - 37?", listOf("63", "67", "53", "73"), 0, "100 minus 37 equals 63!")
            )
            "science" -> listOf(
                Question("s1", "Which planet is known as the Red Planet?", listOf("Venus", "Mars", "Jupiter", "Saturn"), 1, "Mars appears red due to iron oxide on its surface!"),
                Question("s2", "What gas do plants absorb during photosynthesis?", listOf("Oxygen", "Nitrogen", "Carbon Dioxide", "Hydrogen"), 2, "Plants absorb carbon dioxide and release oxygen!"),
                Question("s3", "What is the hardest natural substance on Earth?", listOf("Gold", "Iron", "Diamond", "Quartz"), 2, "Diamond is the hardest known natural mineral!"),
                Question("s4", "How many bones are in the adult human body?", listOf("206", "180", "300", "210"), 0, "An adult human skeleton has 206 bones!"),
                Question("s5", "Which organ pumps blood throughout the human body?", listOf("Brain", "Lungs", "Heart", "Liver"), 2, "The heart pumps blood to supply oxygen and nutrients everywhere!")
            )
            "english" -> listOf(
                Question("e1", "Which of the following is a noun?", listOf("Quickly", "Happiness", "Run", "Beautiful"), 1, "Happiness is an abstract noun!"),
                Question("e2", "What is the past tense of 'sing'?", listOf("Singed", "Sang", "Sung", "Song"), 1, "The simple past tense of sing is sang!"),
                Question("e3", "Which word is an antonym of 'Bravery'?", listOf("Courage", "Fear", "Strength", "Heroism"), 1, "Fear is the opposite of bravery!"),
                Question("e4", "Find the correct spelling:", listOf("Recieve", "Receive", "Receeve", "Receiv"), 1, "Remember: 'i' before 'e' except after 'c'!"),
                Question("e5", "What type of word is 'quickly'?", listOf("Noun", "Verb", "Adjective", "Adverb"), 3, "Words ending in -ly that describe actions are adverbs!")
            )
            else -> listOf(
                Question("g1", "What is the capital city of India?", listOf("Mumbai", "New Delhi", "Kolkata", "Bengaluru"), 1, "New Delhi is the official capital of India!"),
                Question("g2", "Which is the largest continent in the world?", listOf("Africa", "Asia", "Europe", "North America"), 1, "Asia is the largest continent by land area and population!"),
                Question("g3", "Who is known as the Father of the Indian Constitution?", listOf("Mahatma Gandhi", "Dr. B. R. Ambedkar", "Jawaharlal Nehru", "Sardar Patel"), 1, "Dr. B. R. Ambedkar chaired the drafting committee!"),
                Question("g4", "Which animal is known as the Ship of the Desert?", listOf("Elephant", "Camel", "Horse", "Llama"), 1, "Camels adapt perfectly to travel long distances in desert sand!"),
                Question("g5", "How many colors are in a rainbow?", listOf("5", "6", "7", "8"), 2, "VIBGYOR gives us the 7 vibrant colors of the rainbow!")
            )
        }

        return Quiz(
            quizId = "fallback_${UUID.randomUUID().toString().take(8)}",
            title = "$topic Master Quiz",
            description = "Explore $topic with this fun Class $schoolClass quiz!",
            schoolClass = schoolClass,
            subject = subject,
            topic = topic,
            topicKey = topicKey,
            difficulty = difficulty,
            questions = questions,
            estimatedMinutes = 5,
            createdById = createdById,
            isApproved = true,
            playCount = 10
        )
    }
}
