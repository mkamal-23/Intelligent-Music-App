package com.example.intelligentmusicapp.ui.theme

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

// OpenAiService.kt
interface OpenAiService {

    @POST("v1/chat/completions")
    suspend fun getChatResponse(
        @Header("Authorization") authHeader: String,
        @Body request: OpenAiRequest
    ): OpenAiResponse
}

data class OpenAiRequest(
    val model: String = "gpt-4o-mini",
    val messages: List<Map<String, String>>,
    val max_tokens: Int = 500,
    val temperature: Double = 0.7
)

data class OpenAiResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: Map<String, String>
)