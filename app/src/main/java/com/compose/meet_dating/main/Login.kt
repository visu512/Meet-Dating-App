package com.compose.meet_dating.main

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.compose.meet_dating.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class Login : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        auth = Firebase.auth

        setContent {
            LoginScreen(
                onGoogleSignInClick = {
                    signInWithGoogle()
                }
            )
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleGoogleSignInResult(task)
        }
    }

    private fun handleGoogleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            account?.let { firebaseAuthWithGoogle(it.idToken!!) }
        } catch (e: ApiException) {
            // Handle sign-in failure
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, navigate to UserScreen
                    startActivity(Intent(this, UserActivity::class.java))
                    finish()
                } else {
                    // Handle failure
                }
            }
    }
}

@Composable
fun LoginScreen(
    onGoogleSignInClick: () -> Unit = {}
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.white)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo
        Text(
            text = "MEET",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
        )

        Spacer(modifier = Modifier.height(30.dp))

        // Subtitle
        Text(
            text = "Here's to dating with confidence",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "You last signed in with Google",
            fontSize = 14.sp,
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(15.dp))

        // Google Login Button
        SocialLoginButton(
            text = "Continue With Google",
            icon = R.drawable.google,
            backgroundColor = Color.Black,
            textColor = Color.LightGray
        ) {
            onGoogleSignInClick()
        }

        Spacer(modifier = Modifier.height(15.dp))

        // Email Login Button
        SocialLoginButton(
            text = "Continue with email",
            icon = R.drawable.email,
            backgroundColor = Color.LightGray,
            textColor = Color.Black,
        ) {
            // Navigate to RegisterScreen
            val intent = Intent(context, RegisterScreen::class.java)
            (context as Activity).startActivity(intent)
        }

        Spacer(modifier = Modifier.height(15.dp))

        // Facebook Login Button
        SocialLoginButton(
            text = "Continue With Facebook",
            icon = R.drawable.facebook,
            backgroundColor = Color(0xFF1877F2),
            textColor = Color.White
        ) {
            // TODO: Implement Facebook Sign-in Logic
        }

        Spacer(modifier = Modifier.height(35.dp))

        Text(
            text = "We'll never share anything without your permission",
            fontSize = 12.sp,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(15.dp))

        Row {
            Text(
                text = "By signing up, you agree to our ",
                fontSize = 12.sp,
                color = Color.Black
            )
            Text(
                text = "Terms and Conditions",
                fontSize = 12.sp,
                color = Color.Blue,
                modifier = Modifier.clickable {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://yourtermslink.com")
                        )
                    )
                }
            )
        }

        Row {
            Text(
                text = "Learn how we use your data in our ",
                fontSize = 12.sp,
                color = Color.Black
            )
            Text(
                text = "Privacy Policy",
                fontSize = 12.sp,
                color = Color.Blue,
                modifier = Modifier.clickable {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://yourprivacylink.com")
                        )
                    )
                }
            )
        }
    }
}

@Composable
fun SocialLoginButton(
    text: String,
    icon: Int,
    backgroundColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .height(55.dp),
        shape = RoundedCornerShape(50.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = "Icon",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(15.dp))
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLoginScreen() {
    LoginScreen()
}
