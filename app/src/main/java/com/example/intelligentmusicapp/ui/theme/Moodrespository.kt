package com.example.intelligentmusicapp.ui.theme

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.gson.Gson
import kotlinx.coroutines.tasks.await

// MoodRepository.kt
class MoodRepository(
    private val service: OpenAiService = NetworkModule.openAiService,
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val apiKey = "YOUR_OPENAI_API_KEY" // .env ya BuildConfig se lena

    // Mood detect karo
    suspend fun detectMoodFromChat(
        chatHistory: List<Chat>,
        userId: String
    ): Result<MoodEntry> {
        return try {
            val systemPrompt = """
                You are a mood analysis assistant. Analyze the user's messages and detect their emotional mood.
                
                Respond ONLY in this exact JSON format, nothing else:
                {
                    "mood": "happy|sad|anxious|calm|angry|excited|neutral|frustrated",
                    "emoji": "😊",
                    "intensity": 7,
                    "summary": "User seems excited about their upcoming trip"
                }
                
                Base your analysis on tone, word choice, and context.
            """.trimIndent()

            // Sirf user ke messages bhejo (last 10)
            val userMessages = chatHistory.takeLast(10).map { msg ->
                mapOf("role" to msg.role, "content" to msg.content)
            }

            val allMessages = listOf(
                mapOf("role" to "system", "content" to systemPrompt)
            ) + userMessages + listOf(
                mapOf("role" to "user", "content" to "Analyze my mood from this conversation.")
            )

            val response = service.getChatResponse(
                authHeader = "Bearer $apiKey",
                request = OpenAiRequest(messages = allMessages)
            )

            val jsonText = response.choices.first().message["content"] ?: ""
            val gson = Gson()
            val moodData = gson.fromJson(jsonText, MoodAnalysisResult::class.java)

            val moodEntry = MoodEntry(
                userId = userId,
                mood = moodData.mood,
                moodEmoji = moodData.emoji,
                intensity = moodData.intensity,
                summary = moodData.summary,
                chatContext = chatHistory.takeLast(5)
                    .joinToString("\n") { "${it.role}: ${it.content}" }
            )

            // Firebase mein save karo
            saveMoodToFirebase(moodEntry)
            Result.success(moodEntry)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun saveMoodToFirebase(moodEntry: MoodEntry) {
        db.collection("moods")
            .add(moodEntry)
            .await()
    }

    // User ki mood history fetch karo
    suspend fun getMoodHistory(userId: String): List<MoodEntry> {
        return db.collection("moods")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(20)
            .get()
            .await()
            .toObjects(MoodEntry::class.java)
    }
}

data class MoodAnalysisResult(
    val mood: String,
    val emoji: String,
    val intensity: Int,
    val summary: String
)