package com.example.ai

import com.example.BuildConfig
import android.util.Log

class GeminiManager {
    suspend fun generateResponse(command: String, systemInstruction: String, customApiKey: String? = null): String? {
        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = command)))),
            systemInstruction = Content(parts = listOf(Part(text = systemInstruction))),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json"
            )
        )

        return try {
            val key = if (!customApiKey.isNullOrBlank()) {
                customApiKey
            } else if (BuildConfig.GEMINI_API_KEY.isNotEmpty() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY") {
                BuildConfig.GEMINI_API_KEY
            } else {
                null
            }

            if (key.isNullOrBlank()) {
                Log.e("GeminiManager", "API Key is missing")
                return null
            }
            val response = RetrofitClient.service.generateContent(key, request)
            response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
        } catch (e: Exception) {
            Log.e("GeminiManager", "Error calling Gemini", e)
            null
        }
    }
}
