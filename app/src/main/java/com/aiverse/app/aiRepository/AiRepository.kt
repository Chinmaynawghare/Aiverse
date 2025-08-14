package com.aiverse.app.aiRepository

import com.aiverse.app.model.gemini.GeminiRequest
import com.aiverse.app.model.gemini.GeminiResponse
import com.aiverse.app.model.openai.OpenAiRequest
import com.aiverse.app.model.openai.OpenAiResponse
import com.aiverse.app.network.GeminiService
import com.aiverse.app.network.OpenAiService
import javax.inject.Inject

class AiRepository @Inject constructor(
    private val openAiService: OpenAiService,
    private val geminiService: GeminiService
) {

    suspend fun getOpenAiResponse(request: OpenAiRequest): OpenAiResponse {
        return openAiService.getChatCompletion(request)
    }

    suspend fun getGeminiResponse(apiKey: String, request: GeminiRequest): GeminiResponse {
        return geminiService.getGeminiContent(apiKey, request)
    }
}
