package com.aiverse.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollSource.Companion.SideEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.aiverse.app.navigation.AppNavGraph
import com.aiverse.app.ui.theme.AiverseTheme
import com.aiverse.app.viewModel.AuthViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import androidx.core.view.WindowCompat
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AiverseTheme {
                val systemUiController = rememberSystemUiController()
                val useDarkIcons = true
                SideEffect {
                    systemUiController.setSystemBarsColor(
                        color = Color.Transparent,
                        darkIcons = useDarkIcons
                    )
                }
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    val authViewModel: AuthViewModel = hiltViewModel()

                    AppNavGraph(
                        navController = navController,
                        authViewModel = authViewModel)
           }
         }

        }
    }
}
