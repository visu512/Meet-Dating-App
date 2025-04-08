package com.compose.meet_dating.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GenderSelectionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GenderSelectionScreen(context = this)
        }
    }
}

@Composable
fun GenderSelectionScreen(context: Context) {
    val options = listOf("Men", "Women", "Everyone")
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val firestore = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFFBFA), Color(0xFFFFFBFA))
                )
            )
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "I’m interested in...",
                color = Color(0xFFB00020),
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Tell us who you’d like to connect with. This helps us show you better matches!",
                textAlign = TextAlign.Start,
                color = Color.DarkGray,
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(30.dp))

            options.forEach { option ->
                Button(
                    onClick = { selectedOption = option },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (selectedOption == option) Color(0xFF000000) else Color.White.copy(
                            alpha = 1f
                    ),
                    contentColor = if (selectedOption == option) Color.White else Color.Black
                ),
                elevation = ButtonDefaults.elevation(defaultElevation = 3.dp)
                ) {
                Text(
                    text = option,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    selectedOption?.let {
                        isLoading = true
                        saveSelectionToFirestore(it, firestore, userId, context) {
                            isLoading = false
                            context.startActivity(Intent(context, DOBActivity::class.java))

                        }
                    }
                },
                enabled = selectedOption != null,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Black,
                    contentColor = Color.White,
                    disabledBackgroundColor = Color.Black,
                    disabledContentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(30.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
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
        }
    }
}

fun saveSelectionToFirestore(
    selection: String,
    firestore: FirebaseFirestore,
    userId: String?,
    context: Context,
    onComplete: () -> Unit
) {
    if (userId != null) {
        val user = hashMapOf("genderPreference" to selection)
        firestore.collection("users").document(userId)
            .update(user as Map<String, Any>)
            .addOnSuccessListener {
//                Toast.makeText(context, "Selection saved!", Toast.LENGTH_SHORT).show()
                onComplete()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to save selection", Toast.LENGTH_SHORT).show()
                onComplete()
            }
    }
}
