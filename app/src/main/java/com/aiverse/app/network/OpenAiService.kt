package com.aiverse.app.network

import com.aiverse.app.model.openai.OpenAiRequest
import com.aiverse.app.model.openai.OpenAiResponse
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface OpenAiService {

    @Headers(
        "Content-Type: application/json"
    )
    @POST("v1/chat/completions")
    suspend fun getChatCompletion(
        @Body request: OpenAiRequest


    ): OpenAiResponse
}
