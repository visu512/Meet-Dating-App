package com.compose.meet_dating.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlin.random.Random

class RegisterScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RegisterScreenContent()
        }
    }
}

@Composable
fun RegisterScreenContent() {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var generatedOtp by remember { mutableStateOf("") }
    var showOtpField by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val auth = Firebase.auth

    // Generate and send OTP (simulated)
    fun sendOtp() {
        isLoading = true
        errorMessage = ""

        // Generate random 6-digit OTP
        generatedOtp = Random.nextInt(100000, 999999).toString()

        // In a real app, you would send this OTP to the user's email
        // For demo purposes, we'll just log it and show the OTP field
        println("OTP sent to $email: $generatedOtp")

        // Simulate network delay
        kotlinx.coroutines.MainScope().launch {
            kotlinx.coroutines.delay(2000)
            showOtpField = true
            isLoading = false
        }
    }

    fun verifyOtpAndRegister() {
        if (otp == generatedOtp) {
            isLoading = true
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Navigate to main activity
                        context.startActivity(Intent(context, MainActivity::class.java))
                        (context as ComponentActivity).finish()
                    } else {
                        errorMessage = task.exception?.message ?: "Registration failed"
                    }
                    isLoading = false
                }
        } else {
            errorMessage = "Invalid OTP"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Register with OTP",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (!showOtpField) {
            // Email and password fields
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                placeholder = { Text("example@domain.com") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { sendOtp() },
                enabled = email.isNotEmpty() && password.length >= 6 && !isLoading,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White)
                } else {
                    Text("Send OTP")
                }
            }
        } else {
            // OTP verification fields
            Text(
                text = "OTP sent to $email",
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = otp,
                onValueChange = { otp = it },
                label = { Text("Enter OTP") },
                placeholder = { Text("123456") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { verifyOtpAndRegister() },
                enabled = otp.length == 6 && !isLoading,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White)
                } else {
                    Text("Verify OTP and Register")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = {
                    showOtpField = false
                    otp = ""
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Resend OTP")
            }
        }

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = errorMessage,
                color = Color.Red,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewRegisterScreen() {
    RegisterScreenContent()
}