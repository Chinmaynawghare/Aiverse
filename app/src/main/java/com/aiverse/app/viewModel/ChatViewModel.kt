package com.aiverse.app.viewModel

import android.content.Context
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiverse.app.aiRepository.AiRepository
import com.aiverse.app.aiRepository.ChatRepository
import com.aiverse.app.model.gemini.Content
import com.aiverse.app.model.gemini.GeminiRequest
import com.aiverse.app.model.gemini.Part
import com.aiverse.app.model.openai.Message
import com.aiverse.app.model.openai.OpenAiRequest
import com.aiverse.app.BuildConfig
import com.aiverse.app.model.ChatSession
import com.aiverse.app.util.isNetworkAvailable
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val aiRepository: AiRepository,
    private val chatRepository: ChatRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private var lastUserInput: String? = null

    var openAiResponse by mutableStateOf<String?>(null)
        private set

    var geminiResponse by mutableStateOf<String?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var currentSessionId by mutableStateOf<String?>(null)
        private set

    private var isFirstMessage = true

    var chatSessions by mutableStateOf<List<ChatSession>>(emptyList())
        private set

    var preferredSource by mutableStateOf<String?>(null)
        private set

    var isLoopRunning by mutableStateOf(false)
    private var loopStartTime = 0L

    fun startAiLoop(initialPrompt: String) {
        if (isLoopRunning) return
        if (!isNetworkAvailable(context)) {
            errorMessage = "Cannot start AI loop: No internet connection."
            return
        }

        isLoopRunning = true
        loopStartTime = System.currentTimeMillis()

        viewModelScope.launch {
            var currentMessage = initialPrompt

            while (isLoopRunning && System.currentTimeMillis() - loopStartTime <= 30_000) {
                try {
                    val openAiResponse = aiRepository.getOpenAiResponse(
                        OpenAiRequest(messages = listOf(Message(content = currentMessage)))
                    ).choices.firstOrNull()?.message?.content ?: "❓ No OpenAI reply."

                    delay(1000)

                    val geminiResponse = aiRepository.getGeminiResponse(
                        BuildConfig.GEMINI_API_KEY,
                        GeminiRequest(contents = listOf(Content(parts = listOf(Part(text = openAiResponse)))))
                    ).candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "❓ No Gemini reply."

                    this@ChatViewModel.openAiResponse = openAiResponse
                    this@ChatViewModel.geminiResponse = geminiResponse

                    currentMessage = geminiResponse
                    delay(1000)
                } catch (e: Exception) {
                    errorMessage = when {
                        e.message?.contains("429") == true -> "AI loop stopped: Rate limit reached."
                        e.message?.contains("timeout") == true -> "AI loop stopped: Timeout occurred."
                        else -> "❌ AI loop failed: ${e.localizedMessage ?: "Unknown error"}"
                    }
                    break
                }
            }

            isLoopRunning = false
        }
    }

    fun stopAiLoop() {
        isLoopRunning = false
    }

    fun updatePreferredSource(source: String) {
        preferredSource = source
    }

    fun loadChatSessions() {
        viewModelScope.launch {
            try {
                chatSessions = chatRepository.getChatSessions()
            } catch (e: Exception) {
                errorMessage = "❌ Failed to load history: ${e.localizedMessage ?: "Unknown error"}"
            }
        }
    }

    fun loadSession(sessionId: String) {
        viewModelScope.launch {
            try {
                currentSessionId = sessionId
                isFirstMessage = false

                val messages = chatRepository.getMessagesForSession(sessionId)
                val last = messages.lastOrNull() ?: return@launch

                lastUserInput = last["question"] as? String
                val responses = last["responses"] as? List<Map<String, Any>> ?: return@launch

                responses.forEach { res ->
                    val source = res["source"] as? String
                    val text = res["text"] as? String
                    val isPreferred = res["isPreferred"] as? Boolean ?: false

                    when (source) {
                        "openai" -> {
                            openAiResponse = text
                            if (isPreferred) preferredSource = "openai"
                        }
                        "gemini" -> {
                            geminiResponse = text
                            if (isPreferred) preferredSource = "gemini"
                        }
                    }
                }

            } catch (e: Exception) {
                errorMessage = "❌ Failed to load session: ${e.localizedMessage ?: "Unknown error"}"
            }
        }
    }

    fun startNewChat() {
        viewModelScope.launch {
            try {
                currentSessionId = chatRepository.startNewSession()
                isFirstMessage = true
                resetResponses()
            } catch (e: Exception) {
                errorMessage = "❌ Failed to start new session: ${e.localizedMessage ?: "Unknown error"}"
            }
        }
    }

    fun sendMessageToBothAIs(userMessage: String) {
        if (!isNetworkAvailable(context)) {
            errorMessage = "No internet connection. Please try again."
            return
        }

        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            try {
                // OpenAI call with separate try/catch
                val openAiText = try {
                    println("Calling OpenAI API...")
                    val resp = aiRepository.getOpenAiResponse(
                        OpenAiRequest(messages = listOf(Message(content = userMessage)))
                    )
                    println("OpenAI API success")
                    resp.choices.firstOrNull()?.message?.content ?: "❓ No OpenAI reply."
                } catch (e: Exception) {
                    println("OpenAI API failed: ${e.message}")
                    errorMessage = "OpenAI failed: ${e.message}"
                    null
                }

                // Gemini call with separate try/catch
                val geminiText = try {
                    println("Calling Gemini API...")
                    val resp = aiRepository.getGeminiResponse(
                        BuildConfig.GEMINI_API_KEY,
                        GeminiRequest(contents = listOf(Content(parts = listOf(Part(text = userMessage)))))
                    )
                    println("Gemini API success")
                    resp.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "❓ No Gemini reply."
                } catch (e: Exception) {
                    println("Gemini API failed: ${e.message}")
                    errorMessage = "Gemini failed: ${e.message}"
                    null
                }

                openAiResponse = openAiText
                geminiResponse = geminiText

                if (isFirstMessage) {
                    currentSessionId?.let {
                        chatRepository.updateSessionTitle(it, userMessage.take(40))
                    }
                    isFirstMessage = false
                }

                lastUserInput = userMessage

            } finally {
                isLoading = false
            }
        }
    }


    fun saveChatToFirestore(preferred: String) {
        val question = lastUserInput ?: return
        val openAiText = openAiResponse ?: return
        val geminiText = geminiResponse ?: return
        val sessionId = currentSessionId ?: return

        viewModelScope.launch {
            try {
                chatRepository.saveMessageToSession(
                    sessionId,
                    question,
                    openAiText,
                    geminiText,
                    preferred
                )
            } catch (e: Exception) {
                errorMessage = "❌ Firestore save failed: ${e.localizedMessage ?: "Unknown error"}"
            }
        }
    }

    fun resetResponses() {
        openAiResponse = null
        geminiResponse = null
        errorMessage = null
    }
}
