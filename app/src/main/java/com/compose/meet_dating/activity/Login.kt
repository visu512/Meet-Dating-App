package com.compose.meet_dating.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.compose.meet_dating.R
import com.compose.meet_dating.main.UserActivity
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
import kotlinx.coroutines.delay

class Login : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private var onGoogleAuthSuccess: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        auth = Firebase.auth

        setContent {
            MeetDatingTheme {
                var isLoading by remember { mutableStateOf(false) }
                var showSuccessLoading by remember { mutableStateOf(false) }

                if (showSuccessLoading) {
                    FullscreenLoadingScreen {
                        // Redirect after loading screen
                        startActivity(Intent(this@Login, UserActivity::class.java))
                        finish()
                    }
                } else {
                    LoginScreen(
                        isLoading = isLoading,
                        onGoogleSignInClick = {
                            isLoading = true
                            signInWithGoogle {
                                // Save login state and UID in SharedPreferences
                                val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                                prefs.edit()
                                    .putBoolean("isLoggedIn", true)
                                    .putString("currentUserId", auth.currentUser?.uid ?: "") // receiver id
                                    .apply()

                                isLoading = false
                                showSuccessLoading = true
                            }
                        }

                    )
                }
            }
        }
    }

    private fun signInWithGoogle(onComplete: () -> Unit) {
        onGoogleAuthSuccess = onComplete

        // Sign out first to show account chooser every time
        googleSignInClient.signOut().addOnCompleteListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }
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
                    onGoogleAuthSuccess?.invoke()
                }
            }
    }
}

@Composable
fun MeetDatingTheme(content: @Composable () -> Unit) {
    val colorScheme = lightColorScheme(
        primary = Color(0xFFFE3C72),
        secondary = Color(0xFFFF8E8E),
        tertiary = Color(0xFFFFD3D3),
        background = Color(0xFFFFFBFA),
        surface = Color.White,
        error = Color(0xFFE53935),
        onPrimary = Color.White,
        onSecondary = Color.Black,
        onBackground = Color.Black,
        onSurface = Color.Black,
        onError = Color.White
    )
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

@Composable
fun LoginScreen(
    isLoading: Boolean,
    onGoogleSignInClick: () -> Unit = {}
) {
    val context = LocalContext.current
    var buttonScale by remember { mutableStateOf(1f) }
    val animatedScale by animateFloatAsState(targetValue = buttonScale, label = "")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.app_logo),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(100.dp)
                .padding(bottom = 16.dp)
        )

        Text(
            buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color(0xFFFE3C72))) {
                    append("ME")
                }
                append("ET")
            },
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Find your perfect match",
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 40.dp)
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SocialLoginButton(
                text = "Continue with Google",
                icon = R.drawable.google,
                backgroundColor = Color.Black,
                textColor = Color.White,
                borderColor = Color(0xFF7E7D7D),
                isLoading = isLoading,
                modifier = Modifier.scale(animatedScale)
            ) {
                buttonScale = 0.95f
                onGoogleSignInClick()
                buttonScale = 1f
            }


            SocialLoginButton(
                text = "Continue with Facebook",
                icon = R.drawable.facebook,
                backgroundColor = Color(0xFF1877F2),
                textColor = Color.White,
                modifier = Modifier.scale(animatedScale)
            ) {
                buttonScale = 0.95f
                buttonScale = 1f
            }

            SocialLoginButton(
                text = "Continue with email",
                icon = R.drawable.email,
                backgroundColor = MaterialTheme.colorScheme.primary,
                textColor = Color.White,
                modifier = Modifier.scale(animatedScale)
            ) {
                buttonScale = 0.95f
                val intent = Intent(context, RegisterScreen::class.java)
                (context as Activity).startActivity(intent)
                buttonScale = 1f
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Divider(
                color = Color.LightGray.copy(alpha = 0.5f),
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "or",
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Divider(
                color = Color.LightGray.copy(alpha = 0.5f),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = {},
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Continue as guest",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Icon(
                painter = painterResource(id = R.drawable.baseline_arrow),
                contentDescription = "Arrow",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
        val context = LocalContext.current

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "By continuing, you agree to our",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Row {
                ClickableText(
                    text = buildAnnotatedString {
                        append("Terms of Service")
                    },
                    style = androidx.compose.ui.text.TextStyle(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    ),
                    onClick = {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("https://yourtermslink.com"))
                        )
                    }
                )

                Text(
                    text = " and ",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                ClickableText(
                    text = buildAnnotatedString {
                        append("Privacy Policy")
                    },
                    style = androidx.compose.ui.text.TextStyle(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    ),
                    onClick = {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("https://yourprivacylink.com"))
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun SocialLoginButton(
    text: String,
    icon: Int,
    backgroundColor: Color,
    textColor: Color,
    borderColor: Color = backgroundColor,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .clickable(enabled = !isLoading) { onClick() }
            .padding(horizontal = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
        ) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp,

                    )
            } else {
                Text(
                    text = text,
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun FullscreenLoadingScreen(
    onFinish: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(2000) // Show loading screen for 3 seconds
        onFinish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp
            )
            Spacer(modifier = Modifier.height(16.dp)) // Adds space between the progress indicator and text
            Text(
                text = "Logging in...",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }

}
