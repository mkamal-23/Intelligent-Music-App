package com.example.intelligentmusicapp.ui.theme

data class Song(
    val title: String = "",
    val artist: String = "",
    val url: String = "",
    val imageUrl: String= "",
    val songid: String=""
)

data class MoodEntry(
    val userId: String = "",
    val mood: String = "",          // "happy", "sad", "anxious", "calm", "angry", "neutral"
    val moodEmoji: String = "",     // "😊", "😢", etc.
    val intensity: Int = 0,         // 1-10
    val summary: String = "",       // AI ka short summary
    val timestamp: Long = System.currentTimeMillis(),
    val chatContext: String = ""    // Last few messages ka context
)

data class Chat(
    val role: String,    // "user" or "assistant"
    val content: String
)