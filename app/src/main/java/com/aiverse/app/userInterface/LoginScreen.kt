package com.aiverse.app.userInterface

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.aiverse.app.R
import com.aiverse.app.navigation.AppScreens
import com.aiverse.app.viewModel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun LoginScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val darkTheme = isSystemInDarkTheme()

    // 🎨 Color scheme
    val backgroundColor = if (darkTheme) Color(0xFF0D1117) else Color(0xFFFFFFFF)
    val primaryColor = if (darkTheme) Color(0xFF0D1117) else Color(0xFFA3CEF1)
    val secondaryColor = if (darkTheme) Color(0xFF161B22) else Color(0xFFF0F8FF)
    val accentColor = if (darkTheme) Color(0xFF58A6FF) else Color(0xFFFF6B6B)
    val textColor = if (darkTheme) Color(0xFFC9D1D9) else Color(0xFF1F2D3D)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            authViewModel.signInWithCredential(
                credential,
                onSuccess = {
                    val user = authViewModel.user.value
                    if (user != null) {
                        val db = Firebase.firestore
                        val userRef = db.collection("users").document(user.uid)
                        userRef.get().addOnSuccessListener { doc ->
                            if (!doc.exists()) {
                                userRef.set(
                                    mapOf(
                                        "uid" to user.uid,
                                        "name" to user.displayName,
                                        "email" to user.email,
                                        "photoUrl" to user.photoUrl?.toString(),
                                        "joinedAt" to System.currentTimeMillis()
                                    )
                                )
                            }
                        }
                    }
                    navController.navigate(AppScreens.CHAT) {
                        popUpTo(AppScreens.LOGIN) { inclusive = true }
                    }
                },
                onError = {
                    Toast.makeText(context, "Login Failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            )
        } catch (e: Exception) {
            Toast.makeText(context, "Google Sign-In Failed", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top placeholder (60% height)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.6f)
                    .background(primaryColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Image Placeholder",
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
            }

            // Outer wrapper matches top section color so corners blend
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = primaryColor,
                        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                    )
            ) {
                // Inner box holds actual content background
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = secondaryColor,
                            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                        )
                        .padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Welcome Back!",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Sign in to continue to your account",
                                fontSize = 14.sp,
                                color = textColor.copy(alpha = 0.7f)
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    launcher.launch(getSignInIntent(context, forceChoose = false))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text("Sign In", color = Color.White, fontSize = 16.sp)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    launcher.launch(getSignInIntent(context, forceChoose = true))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    "Sign Up with New Account",
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getSignInIntent(context: Context, forceChoose: Boolean): Intent {
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()

    val client = GoogleSignIn.getClient(context, gso)

    if (forceChoose) {
        client.signOut()
    }

    return client.signInIntent
}
