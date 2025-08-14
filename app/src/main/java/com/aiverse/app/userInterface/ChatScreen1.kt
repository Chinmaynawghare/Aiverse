package com.aiverse.app.userInterface

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.aiverse.app.navigation.AppScreens
import com.aiverse.app.viewModel.AuthViewModel
import com.aiverse.app.viewModel.ChatViewModel

// ===== Custom Colors =====
private val LightColors = lightColorScheme(
    background = Color(0xFFFFFFFF),
    primary = Color(0xFFA3CEF1),
    secondary = Color(0xFFF0F8FF),
    tertiary = Color(0xFFFF6B6B),
    onBackground = Color(0xFF1F2D3D),
    onPrimary = Color(0xFF1F2D3D)
)

private val DarkColors = darkColorScheme(
    background = Color(0xFF0D1117),
    primary = Color(0xFF0D1117),
    secondary = Color(0xFF161B22),
    tertiary = Color(0xFF58A6FF),
    onBackground = Color(0xFFC9D1D9),
    onPrimary = Color(0xFFC9D1D9)
)

@Composable
fun ChatScreen1(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    sessionId: String? = null
) {
    val chatViewModel: ChatViewModel = hiltViewModel()
    val isDark = isSystemInDarkTheme()

    MaterialTheme(
        colorScheme = if (isDark) DarkColors else LightColors
    ) {
        ChatScreenContent(navController, authViewModel, chatViewModel, sessionId)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreenContent(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    chatViewModel: ChatViewModel,
    sessionId: String?
) {
    LaunchedEffect(sessionId) {
        sessionId?.let { chatViewModel.loadSession(it) }
    }

    var expanded by remember { mutableStateOf(false) }
    var messageText by remember { mutableStateOf("") }
    var isAiLoopMode by remember { mutableStateOf(false) }

    val openAiResponse by remember { derivedStateOf { chatViewModel.openAiResponse } }
    val geminiResponse by remember { derivedStateOf { chatViewModel.geminiResponse } }
    val isLoading by remember { derivedStateOf { chatViewModel.isLoading } }
    val errorMessage by remember { derivedStateOf { chatViewModel.errorMessage } }
    val preferredSource by remember { derivedStateOf { chatViewModel.preferredSource } }

    val viewOptions = listOf("Both", "OpenAI", "Gemini")
    var currentViewIndex by remember { mutableIntStateOf(0) }
    val selectedView = viewOptions[currentViewIndex]

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Aiverse Chat", color = MaterialTheme.colorScheme.onBackground) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    IconButton(onClick = {
                        navController.navigate(AppScreens.HISTORY)
                    }) {
                        Icon(Icons.Outlined.History, contentDescription = "History", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onPrimary)
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
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val isLargeScreen = maxWidth > 600.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isAiLoopMode) "AI ↔ AI Mode" else "Manual Mode",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = isAiLoopMode,
                        onCheckedChange = {
                            isAiLoopMode = it
                            if (it) {
                                chatViewModel.startAiLoop(
                                    initialPrompt = messageText.ifBlank { "Let's start a discussion." }
                                )
                            } else {
                                chatViewModel.stopAiLoop()
                            }
                        }
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                ) {
                    when {
                        isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.tertiary)
                        }

                        errorMessage != null -> Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("❌ $errorMessage", color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = {
                                chatViewModel.sendMessageToBothAIs(messageText)
                            }) {
                                Text("Retry")
                            }
                        }

                        else -> when (selectedView) {
                            "OpenAI" -> FullWidthAiCard(
                                source = "OpenAI",
                                text = openAiResponse ?: "",
                                isPreferred = preferredSource == "openai",
                                onSelect = {
                                    chatViewModel.updatePreferredSource("openai")
                                    chatViewModel.saveChatToFirestore("openai")
                                }
                            )

                            "Gemini" -> FullWidthAiCard(
                                source = "Gemini",
                                text = geminiResponse ?: "",
                                isPreferred = preferredSource == "gemini",
                                onSelect = {
                                    chatViewModel.updatePreferredSource("gemini")
                                    chatViewModel.saveChatToFirestore("gemini")
                                }
                            )

                            else -> Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                AiResponseCard(
                                    source = "OpenAI",
                                    text = openAiResponse ?: "",
                                    isPreferred = preferredSource == "openai",
                                    onSelect = {
                                        chatViewModel.updatePreferredSource("openai")
                                        chatViewModel.saveChatToFirestore("openai")
                                    }
                                )
                                AiResponseCard(
                                    source = "Gemini",
                                    text = geminiResponse ?: "",
                                    isPreferred = preferredSource == "gemini",
                                    onSelect = {
                                        chatViewModel.updatePreferredSource("gemini")
                                        chatViewModel.saveChatToFirestore("gemini")
                                    }
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 12.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = {
                        currentViewIndex = (currentViewIndex + 1) % viewOptions.size
                    }) {
                        Text("View: $selectedView")
                    }
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = {
                                chatViewModel.startNewChat()
                                messageText = ""
                            },
                            enabled = !isLoading
                        ) {
                            Text("New Chat")
                        }
                    }

                    ChatInputBar1(
                        messageText = messageText,
                        onTextChange = { messageText = it },
                        onSendClick = {
                            chatViewModel.sendMessageToBothAIs(messageText)
                            messageText = ""
                        },
                        enabled = !isLoading
                    )
                }
            }
        }
    }
}

@Composable
fun ChatInputBar1(
    messageText: String,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    enabled: Boolean
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
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send",
                tint = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
fun AiResponseCard(
    source: String,
    text: String,
    isPreferred: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .heightIn(min = 160.dp),
        border = BorderStroke(2.dp, if (isPreferred) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "🤖 $source", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = text, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(12.dp))
            IconButton(onClick = onSelect) {
                Icon(
                    imageVector = Icons.Default.ThumbUp,
                    contentDescription = "Mark Preferred",
                    tint = if (isPreferred) MaterialTheme.colorScheme.tertiary else LocalContentColor.current
                )
            }
        }
    }
}

@Composable
fun FullWidthAiCard(
    source: String,
    text: String,
    isPreferred: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        border = BorderStroke(2.dp, if (isPreferred) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "🤖 $source", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = text, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(12.dp))
            IconButton(onClick = onSelect) {
                Icon(
                    imageVector = Icons.Default.ThumbUp,
                    contentDescription = "Mark Preferred",
                    tint = if (isPreferred) MaterialTheme.colorScheme.tertiary else LocalContentColor.current
                )
            }
        }
    }
}
