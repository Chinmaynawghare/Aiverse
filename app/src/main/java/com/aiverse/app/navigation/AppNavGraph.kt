package com.aiverse.app.navigation


import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.aiverse.app.viewModel.AuthViewModel // ✅ Check this path
import com.aiverse.app.userInterface.LoginScreen // ✅ Check this path

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import com.aiverse.app.userInterface.ChatScreen
import com.aiverse.app.userInterface.ChatScreen1
import androidx.navigation.NavType

import androidx.navigation.navArgument
import com.aiverse.app.userInterface.ChatHistoryScreen


@Composable
fun AppNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val user = authViewModel.user.collectAsState()

    LaunchedEffect(user.value) {
        if (user.value != null) {
            navController.navigate(AppScreens.CHAT) {
                popUpTo(AppScreens.LOGIN) { inclusive = true }
            }
        }
    }


    NavHost(
        navController = navController,
        startDestination = AppScreens.LOGIN
    ) {
        composable(AppScreens.LOGIN) {
            LoginScreen(navController, authViewModel)
        }
        composable(AppScreens.CHAT) {
            ChatScreen1(navController, authViewModel)
        }
        composable(AppScreens.HISTORY) {
            ChatHistoryScreen(navController)
        }
        composable(
            route = AppScreens.CHAT_WITH_ID,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            // Pass the sessionId to ChatScreen1 to load previous chat
            ChatScreen1(navController, authViewModel, sessionId = sessionId)
        }
    }
}
