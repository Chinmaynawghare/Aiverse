package com.aiverse.app.userInterface

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.aiverse.app.navigation.AppScreens

import com.aiverse.app.viewModel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    var expanded by remember { mutableStateOf(false) }
    var messageText by remember { mutableStateOf("") }
    var isAiLoopMode by remember { mutableStateOf(false) }
    var currentSource by remember { mutableStateOf("openai") }
    var preferredSource by remember { mutableStateOf<String?>(null) }

    val openAiResponse = "Recursion is a method where the solution depends on solutions to smaller instances."
    val geminiResponse = "Recursion is a process in which a function calls itself."
    val currentResponse = if (currentSource == "openai") openAiResponse else geminiResponse

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Aiverse Chat") },
                actions = {
                    IconButton(onClick = {
                        // 🔥 TODO: Navigate to History Screen
                    }) {
                        Icon(Icons.Outlined.History, contentDescription = "History")
                    }

                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sign Out") },
                            onClick = {
                                authViewModel.signOut()
                                navController.navigate(AppScreens.LOGIN) {
                                    popUpTo(AppScreens.CHAT) { inclusive = true }
                                }
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // Mode Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isAiLoopMode) "AI ↔ AI Mode" else "Manual Mode",
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = isAiLoopMode,
                    onCheckedChange = { isAiLoopMode = it }
                )
            }

            // AI Response Box
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .weight(1f),
                border = BorderStroke(
                    2.dp,
                    if (preferredSource == currentSource) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "🤖 ${currentSource.uppercase()}",
                            style = MaterialTheme.typography.titleSmall
                        )
                        TextButton(
                            onClick = {
                                currentSource = if (currentSource == "openai") "gemini" else "openai"
                            }
                        ) {
                            Text("Switch to ${if (currentSource == "openai") "Gemini" else "OpenAI"}")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = currentResponse)

                    Spacer(modifier = Modifier.height(12.dp))
                    IconButton(onClick = {
                        preferredSource = currentSource
                    }) {
                        Icon(
                            imageVector = Icons.Default.ThumbUp,
                            contentDescription = "Mark Preferred",
                            tint = if (preferredSource == currentSource)
                                MaterialTheme.colorScheme.primary
                            else LocalContentColor.current
                        )
                    }
                }
            }

            // Chat Input Bar
            ChatInputBar(
                messageText = messageText,
                onTextChange = { messageText = it },
                onSendClick = {
                    // TODO: send message to OpenAI & Gemini
                }
            )
        }
    }
}

@Composable
fun ChatInputBar(
    messageText: String,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = messageText,
            onValueChange = onTextChange,
            placeholder = { Text("Type your message...") },
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            maxLines = 5
        )
        IconButton(
            onClick = onSendClick,
            enabled = messageText.isNotBlank()
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Send"
            )
        }
    }
}
