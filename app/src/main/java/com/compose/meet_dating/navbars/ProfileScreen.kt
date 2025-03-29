package com.compose.meet_dating.navbars

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.compose.meet_dating.main.Login
import com.compose.meet_dating.main.model.ProfileViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(modifier: Modifier = Modifier, viewModel: ProfileViewModel) {
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showLogoutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Picture Section
        ProfileImageSection()

        Spacer(modifier = Modifier.height(24.dp))

        // User Info Section
        UserInfoSection(email = auth.currentUser?.email)

        Spacer(modifier = Modifier.height(32.dp))

        // Settings Options
        ProfileMenuOptions()

        Spacer(modifier = Modifier.weight(1f))

        // Logout Button
        LogoutButton {
            showLogoutDialog = true
        }
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        coroutineScope.launch {
                            val googleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(
                                context,
                                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                            )

                            googleSignInClient.signOut().addOnCompleteListener {
                                googleSignInClient.revokeAccess().addOnCompleteListener {
                                    auth.signOut() // Firebase sign-out
                                    context.startActivity(Intent(context, Login::class.java))
                                    (context as Activity).finish()
                                }
                            }
                        }
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ProfileImageSection() {
    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Profile",
            modifier = Modifier.size(60.dp)
        )
    }
}

@Composable
private fun UserInfoSection(email: String?) {
    Text(
        text = email ?: "Guest",
        style = MaterialTheme.typography.headlineSmall
    )
}

@Composable
private fun ProfileMenuOptions() {
    Column(modifier = Modifier.fillMaxWidth()) {
        ProfileMenuItem(icon = Icons.Default.Settings, text = "Settings", onClick = { /* Navigate to settings */ })
        ProfileMenuItem(icon = Icons.Default.Face, text = "Help & Support", onClick = { /* Navigate to help */ })
        ProfileMenuItem(icon = Icons.Default.Info, text = "About", onClick = { /* Navigate to about */ })
    }
}

@Composable
private fun LogoutButton(onLogoutClicked: () -> Unit) {
    Button(
        onClick = onLogoutClicked,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
    ) {
        Text("Logout")
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = "Navigate",
            modifier = Modifier.size(16.dp)
        )
    }
}
