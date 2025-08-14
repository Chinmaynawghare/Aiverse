package com.aiverse.app.network

import com.aiverse.app.model.gemini.GeminiRequest
import com.aiverse.app.model.gemini.GeminiResponse
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface GeminiService {
    @POST("models/gemini-1.5-pro:generateContent") // ✅ CORRECT ENDPOINT
    suspend fun getGeminiContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest

    ): GeminiResponse
}
