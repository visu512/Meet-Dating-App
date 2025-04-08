package com.compose.meet_dating.navbars

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.compose.meet_dating.activity.Login
import com.compose.meet_dating.activity.SettingsActivity
import com.compose.meet_dating.main.model.ProfileViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(modifier: Modifier = Modifier, viewModel: ProfileViewModel = ProfileViewModel()) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val profiles by viewModel.profiles.collectAsState()
    val profile = profiles.firstOrNull()
    var showLogoutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top Bar
        TopAppBar(
            title = { Text("Profile", fontWeight = FontWeight.Bold, fontSize = 22.sp) },
            actions = {
                IconButton(onClick = {
                    val intent = Intent(context, SettingsActivity::class.java)
                    context.startActivity(intent)
                }) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
            }
        )


        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Profile Header
            ProfileHeaderSection(
                name = profile?.name ?: "Guest",
                age = "24",
                imageUrl = profile?.base64Image,
                email = currentUser?.email ?: "No email available"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Suggested Profile Bio
            Text(
                text = "“Love good conversations, long walks, and spontaneous getaways. Let’s vibe and see where it goes 💫”",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Open to chat and verification
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = true, onCheckedChange = {}, colors = CheckboxDefaults.colors(checkedColor = Color(0xFF354494)))
                    Text("Open to chat", fontSize = 14.sp)
                }

                TextButton(onClick = { /* Handle verification */ }) {
                    Text("✔ Verified", color = Color(0xFF354494))
                }
            }

            Divider()

            // Plans Section
            Text("Plans", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(vertical = 12.dp))
            PlanItem("Your Activity")
            PlanItem("Safety")
            PlanItem("Credits")
            PlanItem("Add More")

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // Extra Section
            Text("Get Extra", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Boost your profile from ₹149", color = Color.Gray)

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("My Plan", color = Color.Gray)
                    Text("Free", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { /* Upgrade logic */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF354494))
                ) {
                    Text("Upgrade", color = Color.White)
                }
            }
        }
    }

    // Logout Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        val googleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(
                            context,
                            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                        )
                        googleSignInClient.signOut().addOnCompleteListener {
                            googleSignInClient.revokeAccess().addOnCompleteListener {
                                auth.signOut()
                                context.startActivity(Intent(context, Login::class.java))
                                (context as Activity).finish()
                            }
                        }
                    }
                }) {
                    Text("Confirm", color = Color.Red)
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
fun ProfileHeaderSection(name: String, age: String, imageUrl: String?, email: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(Color(0xFFEAEAEA)),
            contentAlignment = Alignment.Center
        ) {
            if (imageUrl != null) {
                val decodedBytes = Base64.decode(imageUrl, Base64.DEFAULT)
                val imageBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                Image(
                    bitmap = imageBitmap.asImageBitmap(),
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.clip(CircleShape)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile Icon",
                    tint = Color.Gray,
                    modifier = Modifier.size(60.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text("$name, $age", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text(email, color = Color.Gray, fontSize = 14.sp)
        }
    }
}

@Composable
fun PlanItem(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = Color(0xFF354494),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, fontSize = 15.sp)
    }
}
