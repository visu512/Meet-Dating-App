package com.compose.meet_dating.navbars

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.compose.meet_dating.main.model.LikedProfilesViewModel
import com.compose.meet_dating.main.model.ProfileData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LikeScreen(likedProfilesViewModel: LikedProfilesViewModel) {
    val likedProfiles by likedProfilesViewModel.likedProfiles.collectAsState()

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            "Likes",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = Color.Black
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
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
            if (likedProfiles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("💘", fontSize = 52.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No Likes Yet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "You haven't liked anyone yet.\nStart swiping and spark a connection today!",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 4.dp)
                ) {
                    items(likedProfiles) { profile ->
                        LikedProfileCard(profile)
                        Divider(color = Color.LightGray.copy(alpha = 0.2f), thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

//// liked
@Composable
fun LikedProfileCard(profile: ProfileData) {
    val bitmap = profile.base64Image?.let { decodeBase64ToBitmap(it) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(6.dp)
        ) {
            bitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(55.dp)
                        .clip(CircleShape)
                )
            } ?: Box(
                modifier = Modifier
                    .size(55.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Text("?", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    profile.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    profile.location,
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }
    }
}

fun decodeBase64ToBitmap(base64Str: String): Bitmap? {
    return try {
        val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
