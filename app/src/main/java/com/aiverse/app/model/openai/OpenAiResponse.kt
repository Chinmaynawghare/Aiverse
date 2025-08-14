package com.aiverse.app.model.openai

data class OpenAiResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: MessageContent
)

data class MessageContent(
    val role: String,
    val content: String
)
