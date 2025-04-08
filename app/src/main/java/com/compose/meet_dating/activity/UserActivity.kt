@file:OptIn(ExperimentalMaterial3Api::class)

package com.compose.meet_dating.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.compose.meet_dating.activity.GenderActivity
import com.compose.meet_dating.ui.theme.Nunito
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

import kotlin.math.round

class UserActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val viewModel: UserViewModel = viewModel()
            UserScreen(
                viewModel = viewModel,
                onComplete = {
                    startActivity(Intent(this, GenderActivity::class.java))
                    finish()
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
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFFFFFBFA), Color(0xFFFFFBFA))
                )
            )
    ) {
        // Top Content (Title + Input)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "💖 What's your name?",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = Nunito,
                color = Color(0xFFB00020),
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Let others know what to call you on Meet!",
                fontSize = 16.sp,
                color = Color.DarkGray,
                fontFamily = Nunito,
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(28.dp))

            OutlinedTextField(
                value = state.username,
                onValueChange = { viewModel.updateUsername(it.trim()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                placeholder = {
                    Text(
                        "Enter your name...",
                        fontSize = 16.sp,
                        color = Color.Gray,
                    )
                },
                textStyle = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black
                ),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions.Default,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFFB00020),
                    unfocusedBorderColor = Color(0xFFB00020),
                    cursorColor = Color(0xFFB00020),
                    containerColor = Color.White
                )
            )
        }

        // Bottom Button
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Button(
                onClick = { viewModel.saveUsername(onComplete) },
                enabled = state.username.isNotBlank() && !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White,
                    disabledContainerColor = Color.Black,// Always black background
                    disabledContentColor = Color.White// White text/spinner color

                )
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,            // Spinner is white
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Continue",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Your name helps others recognize you 🧡",
                fontSize = 14.sp,
                color = Color.DarkGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

    }
}


// ViewModel
open class UserViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {
    private val _state = MutableStateFlow(UserState())
    open val state: StateFlow<UserState> = _state

    fun updateUsername(username: String) {
        _state.value = _state.value.copy(username = username)
    }

    fun saveUsername(onComplete: () -> Unit) {
        if (state.value.username.isBlank()) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            try {
                val currentUser = auth.currentUser ?: return@launch
                val userData = hashMapOf(
                    "username" to state.value.username,
                    "userId" to currentUser.uid
                )
                firestore.collection("users").document(currentUser.uid).set(userData).await()
                onComplete()
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }
}

data class UserState(
    val username: String = "",
    val isLoading: Boolean = false
)

@Preview(showBackground = true)
@Composable
fun PreviewUserScreen() {
    val fakeViewModel = object : UserViewModel() {
        override val state: StateFlow<UserState> =
            MutableStateFlow(UserState(username = "TestUser"))
    }

    UserScreen(
        viewModel = fakeViewModel,
        onComplete = {}
    )
}
