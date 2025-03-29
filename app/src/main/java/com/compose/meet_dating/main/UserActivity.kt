package com.compose.meet_dating.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val viewModel: UserViewModel = viewModel()
            UserScreen(
                viewModel = viewModel,
                onComplete = {
                    // Navigate to the next screen after completion
                    startActivity(Intent(this, UserImageActivity::class.java))
                }
            )
        }
    }
}

@Composable
fun UserScreen(
    viewModel: UserViewModel,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Got it. Next, what's your username?",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "This is so you can verify your account",
            color = Color.Gray,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = state.username,
            onValueChange = { viewModel.updateUsername(it) },
            label = { Text("Your username") },
            placeholder = { Text("e.g., awesome_user123") },
            singleLine = true,
            isError = state.errorMessage.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(8.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            supportingText = {
                if (state.errorMessage.isNotEmpty()) {
                    Text(
                        text = state.errorMessage,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Sign Up With Cell Number Instead",
            color = Color.Blue,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* Navigate to phone sign-up */ }
                .padding(bottom = 24.dp)
        )

        Button(
            onClick = { viewModel.saveUsername(onComplete) },
            enabled = !state.isLoading && state.username.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    text = "Continue",
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Register Using Facebook",
            color = Color.Blue,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* Facebook sign-up action */ }
                .padding(bottom = 16.dp)
        )
    }
}

class UserViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {
    private val _state = MutableStateFlow(UserState())
    val state: StateFlow<UserState> = _state

    fun updateUsername(username: String) {
        _state.value = _state.value.copy(
            username = username,
            errorMessage = ""
        )
    }

    fun validateUsername(): Boolean {
        return when {
            state.value.username.isEmpty() -> {
                _state.value = _state.value.copy(
                    errorMessage = "Username cannot be empty"
                )
                false
            }
            state.value.username.length < 3 -> {
                _state.value = _state.value.copy(
                    errorMessage = "Username too short (min 3 chars)"
                )
                false
            }
            state.value.username.length > 20 -> {
                _state.value = _state.value.copy(
                    errorMessage = "Username too long (max 20 chars)"
                )
                false
            }
            !state.value.username.matches(Regex("^[A-zA-Z0-9_]+$")) -> {
                _state.value = _state.value.copy(
                    errorMessage = "Only letters, numbers, and underscores allowed"
                )
                false
            }
            else -> true
        }
    }

    fun saveUsername(onComplete: () -> Unit) {
        if (!validateUsername()) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    // Check if username exists (case-insensitive)
                    val snapshot = firestore.collection("Users")
                        .whereEqualTo("username_lowercase", state.value.username.lowercase())
                        .get()
                        .await()

                    if (!snapshot.isEmpty) {
                        _state.value = _state.value.copy(
                            errorMessage = "Username already taken"
                        )
                        return@launch
                    }

                    // Save user data
                    val userData = hashMapOf(
                        "username" to state.value.username,
                        "username_lowercase" to state.value.username.lowercase(),
                        "userId" to currentUser.uid,
                        "email" to currentUser.email,
                        "createdAt" to System.currentTimeMillis()
                    )

                    firestore.collection("users")
                        .document(currentUser.uid)
                        .set(userData)
                        .await()

                    onComplete()
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    errorMessage = "Error saving username: ${e.message}"
                )
            } finally {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }
}

data class UserState(
    val username: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String = ""
)
