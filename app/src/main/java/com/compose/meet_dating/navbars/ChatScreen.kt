package com.compose.meet_dating.navbars

import ChatViewModel
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.compose.meet_dating.main.model.ProfileData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel, navController: NavController.Companion) {
    val chatProfiles by viewModel.chatProfiles.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            "Chats",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = Color.Black
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White,
                        titleContentColor = Color.Black
                    )
                )
                Divider(color = Color.LightGray.copy(alpha = 0.4f), thickness = 0.5.dp)
            }
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            if (chatProfiles.isEmpty()) {
                EmptyChatsView()
            } else {
                ChatProfilesList(profiles = chatProfiles) { profile ->
//                    navController.navigate("chatDetail/${profile.name}")
                }
            }
        }
    }
}

@Composable
private fun EmptyChatsView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("💬", fontSize = 52.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "No Chats Yet",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Start a conversation to see your chats here.\nYour messages will appear once sent!",
                color = Color.Gray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}

@Composable
private fun ChatProfilesList(
    profiles: List<ProfileData>,
    onItemClick: (ProfileData) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
    ) {
        items(profiles) { profile ->
            ChatProfileCard(profile = profile, onClick = { onItemClick(profile) })
            Divider(color = Color.LightGray.copy(alpha = 0.2f), thickness = 0.5.dp)
        }
    }
}

@Composable
fun ChatProfileCard(profile: ProfileData, onClick: () -> Unit) {
    val bitmap = remember(profile.base64Image) {
        profile.base64Image?.let { decodeBase64ToBitmap(it) }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            ProfileImage(bitmap = bitmap)
            Spacer(modifier = Modifier.width(12.dp))
            ProfileInfo(profile = profile)
        }
    }
}

@Composable
private fun ProfileImage(bitmap: Bitmap?) {
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Profile Picture",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
        )
    } else {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Default profile",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ProfileInfo(profile: ProfileData) {
    Column {
        Text(
            profile.name,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Tap to view chat",
            color = Color.Gray,
            fontSize = 14.sp
        )
    }
}
//
//private fun decodeBase64ToBitmap(base64Str: String): Bitmap? {
//    return try {
//        val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
//        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
//    } catch (e: Exception) {
//        null
//    }
//}
