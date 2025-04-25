package com.compose.meet_dating.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.compose.meet_dating.R
import com.compose.meet_dating.main.UserActivity
import com.compose.meet_dating.utils.AuthUtils
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

class LoginActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleGoogleSignInResult(task)
        }
    }

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
                LoginAppScreen()
            }
        }
    }

    private fun handleGoogleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            account?.let { firebaseAuthWithGoogle(it.idToken!!) }
        } catch (e: ApiException) {
            // Handle error
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    AuthUtils.setUserLoggedIn(this, true, auth.currentUser?.uid)
                    startActivity(Intent(this, UserActivity::class.java))
                    finish()
                }
            }
    }

    @Composable
    fun MeetDatingTheme(content: @Composable () -> Unit) {
        MaterialTheme(
            colorScheme = lightColorScheme(
                primary = Color(0xFFE94057), // Romantic red
                secondary = Color(0xFF8A2387), // Deep purple
                tertiary = Color(0xFFF27121), // Orange
                background = Color(0xFFFFFBFA), // Soft white
                surface = Color.White,
                error = Color(0xFFE53935),
                onPrimary = Color.White,
                onSecondary = Color.White,
                onBackground = Color(0xFF333333),
                onSurface = Color(0xFF333333),
                onError = Color.White
            ),
            content = content
        )
    }

    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    fun LoginAppScreen() {
        var isLoading by remember { mutableStateOf(false) }
        var showSuccess by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        if (showSuccess) {
            SuccessAnimationScreen {
                startActivity(Intent(this@LoginActivity, UserActivity::class.java))
                finish()
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFFFF0F0),
                                Color(0xFFFFFBFA)
                            )
                        )
                    )
            ) {
                // Floating hearts background
                FloatingHeartsAnimation()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(60.dp))

                    // Animated logo with pulse effect
                    PulsatingLogo()

                    Spacer(modifier = Modifier.height(16.dp))

                    // Romantic taglines that change
                    RomanticTaglines()

                    Spacer(modifier = Modifier.height(40.dp))

                    // Error message if any
                    errorMessage?.let {
                        AnimatedErrorMessage(it) {
                            errorMessage = null
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Login buttons
                    DatingLoginOptions(
                        isLoading = isLoading,
                        onGoogleLogin = {
                            isLoading = true
                            errorMessage = null
                            val signInIntent = googleSignInClient.signInIntent
                            googleSignInLauncher.launch(signInIntent)
                        },
                        onEmailLogin = {
                            // Handle email login
                        }
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Romantic divider
                    RomanticDivider()

                    Spacer(modifier = Modifier.height(24.dp))

                    // Guest option
                    GuestOption()

                    Spacer(modifier = Modifier.weight(1f))

                    // Terms and privacy
                    DatingTermsText()

                    Spacer(modifier = Modifier.height(40.dp))
                }

                if (isLoading) {
                    RomanticLoadingOverlay()
                }
            }
        }
    }

    @Composable
    fun PulsatingLogo() {
        val infiniteTransition = rememberInfiniteTransition()
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )

        Box(
            modifier = Modifier
                .size(140.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = CircleShape
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.lov),
                contentDescription = "Dating App Logo",
                modifier = Modifier
                    .size(100.dp)
                    .scale(pulseScale),
                contentScale = ContentScale.Fit
            )
        }
    }

    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    fun RomanticTaglines() {
        val taglines = listOf(
            "Find your perfect match",
            "Love is just a swipe away",
            "Meet someone special today",
            "Your love story starts here"
        )

        var currentTagline by remember { mutableStateOf(0) }

        LaunchedEffect(Unit) {
            while (true) {
                delay(3000)
                currentTagline = (currentTagline + 1) % taglines.size
            }
        }

        AnimatedContent(
            targetState = currentTagline,
            transitionSpec = {
                slideInVertically { height -> height } + fadeIn() with
                        slideOutVertically { height -> -height } + fadeOut()
            }
        ) { index ->
            Text(
                text = taglines[index],
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    @Composable
    fun AnimatedErrorMessage(message: String, onDismiss: () -> Unit) {
        var visible by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            delay(5000)
            visible = false
            onDismiss()
        }

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDismiss() },
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.lovess),
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    @Composable
    fun DatingLoginOptions(
        isLoading: Boolean,
        onGoogleLogin: () -> Unit,
        onEmailLogin: () -> Unit
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Google button
            RomanticAuthButton(
                text = "Continue with Google",
                icon = R.drawable.google,
                backgroundColor = Color(0xFF000000),
                textColor = Color.White,
                isLoading = isLoading && AuthUtils.isGoogleLoginInProgress(),
                onClick = onGoogleLogin

            )

            // Facebook button
            RomanticAuthButton(
                text = "Continue with Facebook",
                icon = R.drawable.facebook,
                backgroundColor = Color(0xFF1877F2),
                textColor = Color.White,
                isLoading = false,
                onClick = { /* Implement Facebook login */ }
            )

            // Email button with heart icon
            RomanticAuthButton(
                text = "Continue with Email",
                icon = R.drawable.email,
                backgroundColor = MaterialTheme.colorScheme.primary,
                textColor = Color.White,
                isLoading = false,
                onClick = onEmailLogin
            )
        }
    }

    @Composable
    fun RomanticAuthButton(
        text: String,
        icon: Int,
        backgroundColor: Color,
        textColor: Color,
        isLoading: Boolean,
        onClick: () -> Unit
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.97f else 1f,
            animationSpec = spring(dampingRatio = 0.6f, stiffness = 800f)
        )

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = backgroundColor,
            shadowElevation = if (isPressed) 4.dp else 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .scale(scale)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = textColor,
                        strokeWidth = 2.dp
                    )
                } else {
                    Image(
                        painter = painterResource(id = icon),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = text,
                    color = textColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    @Composable
    fun RomanticDivider() {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Divider(
                color = Color.LightGray.copy(alpha = 0.3f),
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .size(24.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Heart",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(12.dp)
                )
            }
            Divider(
                color = Color.LightGray.copy(alpha = 0.3f),
                modifier = Modifier.weight(1f)
            )
        }
    }

    @Composable
    fun GuestOption() {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.95f else 1f,
            animationSpec = spring(dampingRatio = 0.6f, stiffness = 800f)
        )

        TextButton(
            onClick = { /* Handle guest login */ },
            modifier = Modifier.scale(scale),
            interactionSource = interactionSource
        ) {
            Text(
                text = "Continue as guest",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                painter = painterResource(id = R.drawable.baseline_arrow),
                contentDescription = "Heart",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
    }

    @Composable
    fun DatingTermsText() {
        val context = LocalContext.current
        val annotatedString = buildAnnotatedString {
            append("By continuing, you agree to our ")

            pushStringAnnotation(tag = "terms", annotation = "https://example.com/terms")
            withStyle(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline
                )
            ) {
                append("Terms of Love")
            }
            pop()

            append(" and ")

            pushStringAnnotation(tag = "privacy", annotation = "https://example.com/privacy")
            withStyle(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline
                )
            ) {
                append("Privacy Promise")
            }
            pop()
        }

        ClickableText(
            text = annotatedString,
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color.Gray,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.fillMaxWidth(),
            onClick = { offset ->
                annotatedString.getStringAnnotations(tag = "terms", start = offset, end = offset)
                    .firstOrNull()?.let {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it.item)))
                    }

                annotatedString.getStringAnnotations(tag = "privacy", start = offset, end = offset)
                    .firstOrNull()?.let {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it.item)))
                    }
            }
        )
    }

    @Composable
    fun RomanticLoadingOverlay() {
        Surface(
            color = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Beating heart animation
                BeatingHeart()

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Login...",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Good things take time",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )
            }
        }
    }

    @Composable
    fun BeatingHeart() {
        val infiniteTransition = rememberInfiniteTransition()
        val heartScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.3f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 1000
                    1.0f at 0
                    1.3f at 500
                    1.0f at 1000
                }
            )
        )

        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = "Beating heart",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(48.dp)
                .scale(heartScale)
        )
    }

    @Composable
    fun FloatingHeartsAnimation() {
        Box(modifier = Modifier.fillMaxSize()) {
            // Heart 1
            FloatingHeart(
                startX = 0.1f,
                startY = 0.2f,
                duration = 8000,
                size = 24.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )

            // Heart 2
            FloatingHeart(
                startX = 0.8f,
                startY = 0.3f,
                duration = 10000,
                size = 32.dp,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
            )

            // Heart 3
            FloatingHeart(
                startX = 0.3f,
                startY = 0.7f,
                duration = 12000,
                size = 20.dp,
                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
            )
        }
    }

    @Composable
    fun FloatingHeart(
        startX: Float,
        startY: Float,
        duration: Int,
        size: Dp,
        color: Color
    ) {
        val infiniteTransition = rememberInfiniteTransition()
        val offsetX by infiniteTransition.animateFloat(
            initialValue = startX,
            targetValue = startX + 0.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(duration, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ), label = ""
        )

        val offsetY by infiniteTransition.animateFloat(
            initialValue = startY,
            targetValue = startY - 0.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(duration, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )

        val rotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 15f,
            animationSpec = infiniteRepeatable(
                animation = tween(duration / 2, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(
                    x = (offsetX * LocalContext.current.resources.displayMetrics.widthPixels).dp,
                    y = (offsetY * LocalContext.current.resources.displayMetrics.heightPixels).dp
                )
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = color,
                modifier = Modifier
                    .size(size)
                    .rotate(rotation)
            )
        }
    }

    @Composable
    fun SuccessAnimationScreen(onFinish: () -> Unit) {
        var showContent by remember { mutableStateOf(false) }
        var scale by remember { mutableStateOf(0f) }

        LaunchedEffect(Unit) {
            delay(100)
            showContent = true
            scale = 1f
            delay(2000)
            onFinish()
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Success",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(60.dp)
                                .scale(scale)
                        )
                    }

                    Text(
                        text = "Welcome to MEET!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Your journey to love begins now",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}