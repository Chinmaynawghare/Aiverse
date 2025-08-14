package com.aiverse.app.aiRepository

import com.aiverse.app.model.ChatSession
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ChatRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    private fun getUserId(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
    }

    suspend fun startNewSession(): String {
        val uid = getUserId()
        val sessionData = mapOf(
            "title" to "Untitled",
            "timestamp" to System.currentTimeMillis()
        )

        val sessionRef = firestore.collection("users")
            .document(uid)
            .collection("chat_sessions")
            .add(sessionData)
            .await()

        return sessionRef.id
    }

    suspend fun saveMessageToSession(
        sessionId: String,
        question: String,
        openAiText: String,
        geminiText: String,
        preferred: String
    ) {
        val uid = getUserId()
        val messageData = mapOf(
            "question" to question,
            "timestamp" to System.currentTimeMillis(),
            "responses" to listOf(
                mapOf("source" to "openai", "text" to openAiText, "isPreferred" to (preferred == "openai")),
                mapOf("source" to "gemini", "text" to geminiText, "isPreferred" to (preferred == "gemini"))
            )
        )

        firestore.collection("users")
            .document(uid)
            .collection("chat_sessions")
            .document(sessionId)
            .collection("messages")
            .add(messageData)
            .await()
    }

    suspend fun getMessagesForSession(sessionId: String): List<Map<String, Any>> {
        val uid = getUserId()
        val snapshot = firestore.collection("users")
            .document(uid)
            .collection("chat_sessions")
            .document(sessionId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .get()
            .await()

        return snapshot.documents.mapNotNull { it.data }
    }

    suspend fun updateSessionTitle(sessionId: String, newTitle: String) {
        val uid = getUserId()
        firestore.collection("users")
            .document(uid)
            .collection("chat_sessions")
            .document(sessionId)
            .update("title", newTitle)
            .await()
    }

    suspend fun getChatSessions(): List<ChatSession> {
        val uid = getUserId()
        val snapshot = firestore.collection("users")
            .document(uid)
            .collection("chat_sessions")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            val title = doc.getString("title") ?: return@mapNotNull null
            val timestamp = doc.getLong("timestamp") ?: return@mapNotNull null
            ChatSession(id = doc.id, title = title, timestamp = timestamp)
        }
    }
}
