package com.compose.meet_dating.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class DOBActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DOBScreen()
        }
    }
}

@Composable
fun DOBScreen() {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    var day by remember { mutableStateOf(TextFieldValue()) }
    var month by remember { mutableStateOf(TextFieldValue()) }
    var year by remember { mutableStateOf(TextFieldValue()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

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
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Enter Your Date of Birth",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF000000),
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                Text(
                    text = "We need your date of birth to ensure you're at least 18 years old.",
                    fontSize = 16.sp,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                )

                Card(
                    shape = RoundedCornerShape(20.dp),
                    elevation = 8.dp,
                    backgroundColor = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CustomOutlinedTextField(
                                value = day,
                                onValueChange = { day = it },
                                label = "Day"
                            )
                            CustomOutlinedTextField(
                                value = month,
                                onValueChange = { month = it },
                                label = "Month"
                            )
                            CustomOutlinedTextField(
                                value = year,
                                onValueChange = { year = it },
                                label = "Year"
                            )
                        }

                        if (errorMessage.isNotEmpty()) {
                            Text(
                                text = errorMessage,
                                color = Color.Red,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    val dobValid = validateDOB(day.text, month.text, year.text)
                    if (dobValid) {
                        isLoading = true
                        saveDOBToFirestore(
                            userId,
                            "${day.text}/${month.text}/${year.text}",
                            firestore,
                            context
                        ) {
                            isLoading = false
                            context.startActivity(Intent(context, LocationActivity::class.java))
                        }
                    } else {
                        errorMessage = "Invalid DOB. Ensure age is 18+ and format is correct."
                    }
                },
                enabled = day.text.isNotEmpty() && month.text.isNotEmpty() && year.text.isNotEmpty() && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(60),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Black,
                    contentColor = Color.White,
                    disabledBackgroundColor = Color.Black,
                    disabledContentColor = Color.White
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(text = "Continue", fontSize = 16.sp, fontWeight = FontWeight.Normal)
                }
            }
        }
    }
}

@Composable
fun CustomOutlinedTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    label: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = {
            if (it.text.all { char -> char.isDigit() } && it.text.length <= if (label == "Year") 4 else 2) {
                onValueChange(it)
            }
        },
        label = {
            Text(
                text = label,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
        },
        textStyle = TextStyle(
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        ),
        modifier = Modifier.width(85.dp),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = Color.Black,
            unfocusedBorderColor = Color.Black,
            cursorColor = Color.Black
        ),
        keyboardOptions = KeyboardOptions( /// keyboard only show number
            keyboardType = KeyboardType.Number
        ),
        singleLine = true
    )
}

fun validateDOB(day: String, month: String, year: String): Boolean {
    return try {
        val dayInt = day.toInt()
        val monthInt = month.toInt()
        val yearInt = year.toInt()

        if (dayInt !in 1..31 || monthInt !in 1..12 || yearInt !in 1920..Calendar.getInstance().get(Calendar.YEAR)) {
            return false
        }

        val dob = Calendar.getInstance().apply { set(yearInt, monthInt - 1, dayInt) }
        val today = Calendar.getInstance()
        val age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age - 1
        } else {
            age
        } >= 18
    } catch (e: Exception) {
        false
    }
}

fun saveDOBToFirestore(
    userId: String?,
    dob: String,
    firestore: FirebaseFirestore,
    context: android.content.Context,
    onComplete: () -> Unit
) {
    if (userId == null) return

    val age = calculateAge(dob)
    val user = hashMapOf("dob" to dob, "age" to age)

    firestore.collection("users").document(userId)
        .update(user as Map<String, Any>)
        .addOnSuccessListener { onComplete() }
        .addOnFailureListener {
            Toast.makeText(context, "Failed to save Date of Birth", Toast.LENGTH_SHORT).show()
            onComplete()
        }
}

fun calculateAge(dob: String): Int {
    return try {
        val (day, month, year) = dob.split("/").map { it.toInt() }
        val dobCalendar = Calendar.getInstance().apply { set(year, month - 1, day) }
        val today = Calendar.getInstance()
        var age = today.get(Calendar.YEAR) - dobCalendar.get(Calendar.YEAR)

        if (today.get(Calendar.DAY_OF_YEAR) < dobCalendar.get(Calendar.DAY_OF_YEAR)) {
            age -= 1
        }
        age
    } catch (e: Exception) {
        0
    }
}