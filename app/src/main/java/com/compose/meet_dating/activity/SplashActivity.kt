package com.compose.meet_dating.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.compose.meet_dating.R
import com.compose.meet_dating.utils.AuthUtils
import kotlinx.coroutines.delay

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if the user is already logged in
        if (AuthUtils.isUserLoggedIn(this)) {
            navigateToMain()
        } else {
            setContent {
                SplashScreen {
                    // Navigate based on user login status
                    if (AuthUtils.isUserLoggedIn(this@SplashActivity)) {
                        navigateToMain()
                    } else {
                        navigateToLogin()
                    }
                }
            }
        }
    }

    private fun navigateToMain() {
        // Navigate to MainActivity if logged in
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()  // Close the SplashActivity after navigating
    }

    private fun navigateToLogin() {
        // Navigate to LoginActivity if not logged in
        val intent = Intent(this, Login::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()  // Close the SplashActivity after navigating
    }
}

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    // Define the scale animation for the splash screen logo and text
    val scale = remember { Animatable(0f) }

    LaunchedEffect(true) {
        // Animate the scale of the logo and text
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = EaseOutBack)
        )
        delay(1500)  // Delay for 1.5 seconds before navigating
        onFinished()  // Call the onFinished callback
    }

    // Splash screen layout
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFE94057), Color(0xFF8A2387))
                )
            ),
        contentAlignment = Alignment.Center

    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Splash logo with animation
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(100.dp)
                    .graphicsLayer {
                        scaleX = scale.value
                        scaleY = scale.value
                    }
            )
            // App name with animation
            Text(
                text = "MEET",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                }
            )
        }
    }
}
