package com.example.intelligentmusicapp.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.intelligentmusicapp.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ChatViewModel.kt
class ChatViewModel : ViewModel() {

    private val repository = MoodRepository()

    private val _messages = MutableStateFlow<List<Chat>>(emptyList())
    val messages: StateFlow<List<Chat>> = _messages


    // Har 5 messages ke baad mood analyze ho
    private var messageCountSinceLastAnalysis = 0

    fun sendMessage(content: String, userId: String) {
        val userMsg = Chat(role = "user", content = content)
        _messages.value = _messages.value + userMsg

        viewModelScope.launch {
            // AI se reply lo (aapka existing OpenAI call)
            val aiReply = getAiReply(_messages.value)
            val assistantMsg = Chat(role = "assistant", content = aiReply)
            _messages.value = _messages.value + assistantMsg

            messageCountSinceLastAnalysis++

            // Har 5 messages pe mood analyze karo
            if (messageCountSinceLastAnalysis >= 5) {
                launch { repository.detectMoodFromChat(_messages.value, userId) }
                messageCountSinceLastAnalysis = 0
            }
        }
    }

    private suspend fun getAiReply(history: List<Chat>): String {
        return try {
            val messages = history.map {
                mapOf("role" to it.role, "content" to it.content)
            }
            val response = NetworkModule.openAiService.getChatResponse(
                authHeader = "Bearer ${BuildConfig.OPENAI_API_KEY}",
                request = OpenAiRequest(messages = messages)
            )
            response.choices.first().message["content"] ?: "Sorry, kuch problem hui."
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}