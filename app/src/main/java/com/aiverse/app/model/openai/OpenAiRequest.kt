package com.aiverse.app.model.openai

data class OpenAiRequest(
    val model: String = "gpt-3.5-turbo",
    val messages: List<Message>
)

data class Message(
    val role: String = "user",
    val content: String
)
