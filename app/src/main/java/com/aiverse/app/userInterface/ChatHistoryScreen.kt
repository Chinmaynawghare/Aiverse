package com.aiverse.app.userInterface

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.aiverse.app.viewModel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.Date

// Reuse same theme colors from ChatScreen1
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatHistoryScreen(
    navController: NavHostController,
    chatViewModel: ChatViewModel = hiltViewModel()
) {
    val isDark = isSystemInDarkTheme()

    MaterialTheme(
        colorScheme = if (isDark) DarkColors else LightColors
    ) {
        LaunchedEffect(Unit) {
            chatViewModel.loadChatSessions()
        }

        val sessions = chatViewModel.chatSessions

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Chat History", color = MaterialTheme.colorScheme.onPrimary) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
            ) {
                sessions.forEach { session ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                chatViewModel.loadSession(session.id)
                                navController.navigate("chat/${session.id}")
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                session.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                "🕒 ${SimpleDateFormat("dd MMM yyyy, hh:mm a").format(Date(session.timestamp))}",
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }
        }
    }
}
