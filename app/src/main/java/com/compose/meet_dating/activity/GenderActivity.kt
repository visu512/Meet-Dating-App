package com.compose.meet_dating.activity

import android.content.Intent
import android.os.Bundle
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.compose.meet_dating.main.UserImageActivity
import com.compose.meet_dating.ui.theme.Nunito
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GenderActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GenderSelectionScreen {
                val intent = Intent(this, GenderSelectionActivity::class.java)
                startActivity(intent)
                finishAffinity()
            }
        }
    }
}

@Composable
fun GenderSelectionScreen(onSuccess: () -> Unit) {
    val options = listOf("I’m a Man", "I’m a Woman", "Another Gender")
    var selectedOption by remember { mutableStateOf<String?>(null) }
    val firestore = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFFFFBFA), Color(0xFFFFFBFA))
                )
            )
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Tell us about yourself!",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = Nunito,
                color = Color(0xFFB00020),
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "To help you find your perfect match, let’s start with your gender.",
                fontSize = 15.sp,
                color = Color.DarkGray,
                fontFamily = Nunito,
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(36.dp))

            options.forEach { option ->
                Button(
                    onClick = { selectedOption = option },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (selectedOption == option) Color.Black else Color.White,
                        contentColor = if (selectedOption == option) Color.White else Color.Black
                    ),
                    elevation = ButtonDefaults.elevation(6.dp)
                ) {
                    Text(
                        text = option,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Nunito,
//                        color = Color.White
                    )
                }
            }
        }

        // Bottom Continue Button
        Button(
            onClick = {
                if (selectedOption != null && userId != null) {
                    isLoading = true
                    firestore.collection("users").document(userId)
                        .update("gender", selectedOption)
                        .addOnSuccessListener {
                            isLoading = false
                            onSuccess()
                        }
                        .addOnFailureListener {
                            isLoading = false
                        }
                }
            },
            enabled = selectedOption != null && !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .align(Alignment.BottomCenter),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.Black,
                contentColor = Color.White,
                disabledBackgroundColor = Color.Black,
                disabledContentColor = Color.White
            ),
            shape = RoundedCornerShape(50)
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
                    fontWeight = FontWeight.Normal,
                )
            }
        }
    }
}
