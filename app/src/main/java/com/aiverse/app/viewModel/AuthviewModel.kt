package com.aiverse.app.viewModel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject


@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _user = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val user: StateFlow<FirebaseUser?> = _user.asStateFlow()

    fun signInWithCredential(credential: AuthCredential, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                _user.value = auth.currentUser
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }

    fun signOut() {
        auth.signOut()
        _user.value = null
    }
}
