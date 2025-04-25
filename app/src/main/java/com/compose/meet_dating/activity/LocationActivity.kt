package com.compose.meet_dating.activity

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.compose.meet_dating.main.UserImageActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LocationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LocationScreen()
        }
    }
}

@Composable
fun LocationScreen() {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    var location by remember { mutableStateOf(TextFieldValue()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val mainColor = Color(0xFF000000) /// input box color

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFFBFA), Color(0xFFFFFBFA))
                )
            )
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Set Your Location",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = mainColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                textAlign = TextAlign.Start
            )

            Text(
                text = "This helps us find better matches near you. People near you are waiting to match!",
                fontSize = 16.sp,
                color = Color.DarkGray,
                modifier = Modifier
                    .fillMaxWidth(),
                textAlign = TextAlign.Start
            )
             Spacer(modifier = Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(24.dp),
                backgroundColor = Color.White.copy(alpha = 0.95f),
                elevation = 10.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = location,
                        onValueChange = {
                            location = it
                            errorMessage = ""
                        },
                        label = {
                            Text(
                                "Eg: Mumbai, Delhi, New York",
                                fontWeight = FontWeight.Normal,
                                color = mainColor
                            )
                        },
                        placeholder = {
//                            Text(
//                                "Eg: Mumbai, Delhi, New York",
//                                color = Color.Gray
//                            )
                        },
                        textStyle = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal,
                            color = mainColor
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = mainColor,
                            unfocusedBorderColor = Color.Gray,
                            cursorColor = mainColor,
                            textColor = mainColor
                        )
                    )

                    if (errorMessage.isNotEmpty()) {
                        Text(
                            text = errorMessage,
                            color = Color.Red,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    if (location.text.isNotEmpty() && userId != null) {
                        isLoading = true
                        saveLocationToFirestore(userId, location.text, firestore, context) {
                            isLoading = false
                            context.startActivity(Intent(context, UserImageActivity::class.java))
                        }
                    } else {
                        errorMessage = "Please enter a valid location."
                    }
                },
                enabled = !isLoading && location.text.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(30.dp),
                elevation = ButtonDefaults.elevation(8.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Black,
                    contentColor = Color.White,
                    disabledBackgroundColor = Color.Black,
                    disabledContentColor = Color.White
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Continue",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = 1.2.sp
                    )
                }
            }
        }
    }
}


fun saveLocationToFirestore(
    userId: String?,
    location: String,
    firestore: FirebaseFirestore,
    context: android.content.Context,
    onComplete: () -> Unit
) {
    if (userId == null) return

    val userLocation = hashMapOf("location" to location)

    firestore.collection("users").document(userId)
        .update(userLocation as Map<String, Any>)
        .addOnSuccessListener {
            onComplete()
        }
        .addOnFailureListener {
            Toast.makeText(context, "Failed to save location", Toast.LENGTH_SHORT).show()
            onComplete()
        }
}
