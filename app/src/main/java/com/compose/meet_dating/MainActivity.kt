package com.compose.meet_dating

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.compose.meet_dating.main.model.ProfileData
import com.compose.meet_dating.main.model.ProfileViewModel
import com.compose.meet_dating.navbars.ProfileScreen
import com.compose.meet_dating.ui.theme.MeetDatingAppTheme
import com.google.accompanist.pager.*
import java.lang.Exception

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MeetDatingAppTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(selectedTab) { index ->
                selectedTab = index
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> EncounterScreen(viewModel = ProfileViewModel())
                1 -> Text("Chats")
                2 -> Text("Likes")
                3 -> ProfileScreen(viewModel = ProfileViewModel())
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EncounterScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel
) {
    val profiles by viewModel.profiles.collectAsState()
    val pagerState = rememberPagerState()

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Match", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            IconButton(onClick = { /* Open Filter */ }) {
                Icon(painter = painterResource(id = R.drawable.setting), contentDescription = "Filter")
            }
        }

        when {
            profiles.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            else -> {
                HorizontalPager(
                    state = pagerState,
                    count = profiles.size,
                    modifier = Modifier.weight(1f)
                ) { page ->
                    ProfileCard(profile = profiles[page], viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun ProfileCard(profile: ProfileData, viewModel: ProfileViewModel) {
    val bitmap = profile.base64Image?.let { decodeBase64ToBitmap(it) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .aspectRatio(2.6f / 4f)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.LightGray)
    ) {
        // Show image if available, otherwise use a placeholder
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } ?: run {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                Text("No Image", color = Color.White)
            }
        }

        // Gradient Overlay (Top & Bottom)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .align(Alignment.TopCenter)
                .background(Brush.verticalGradient(listOf(Color.Black.copy(0.6f), Color.Transparent)))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.6f))))
        )

        // User Details (Top Left Corner)
        Column(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                profile.name,
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                profile.location,
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyMedium
            )
            Button(
                onClick = { /* Open Chat */ },
                modifier = Modifier.height(35.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.9f))
            ) {
                Text("Open to chat", color = Color.Black,
                    fontSize = 12.sp)
            }
        }

        // Like & Dislike Buttons (Bottom Center)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(
                onClick = { /* Dislike Action */ },
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.close),
                    contentDescription = "Dislike",
                    tint = Color.Black,
                    modifier = Modifier.size(30.dp)
                )
            }

            IconButton(
                onClick = { /* Like Action */ },
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.like),
                    contentDescription = "Like",
                    tint = Color.Black,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}



// Function to Convert Base64 String to Bitmap
fun decodeBase64ToBitmap(base64Str: String): Bitmap? {
    return try {
        val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
fun BottomNavigationBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar {
        NavigationBarItem(selected = selectedTab == 0, onClick = { onTabSelected(0) },
            icon = { Icon(painter = painterResource(id = R.drawable.card), contentDescription = "Encounters", modifier = Modifier.size(20.dp)) },
            label = { Text("Match") })

        NavigationBarItem(selected = selectedTab == 1, onClick = { onTabSelected(1) },
            icon = { Icon(painter = painterResource(id = R.drawable.chat), contentDescription = "Chats", modifier = Modifier.size(20.dp)) },
            label = { Text("Chats") })

        NavigationBarItem(selected = selectedTab == 2, onClick = { onTabSelected(2) },
            icon = { Icon(painter = painterResource(id = R.drawable.like), contentDescription = "Likes", modifier = Modifier.size(20.dp)) },
            label = { Text("Likes") })

        NavigationBarItem(selected = selectedTab == 3, onClick = { onTabSelected(3) },
            icon = { Icon(painter = painterResource(id = R.drawable.profile), contentDescription = "Profile", modifier = Modifier.size(20.dp)) },
            label = { Text("Profile") })
    }
}

data class ProfileData(
    val name: String,
    val location: String,
    val base64Image: String? = null
)

@Preview(showBackground = true)
@Composable
fun PreviewEncounterScreen() {
    MeetDatingAppTheme {
        EncounterScreen(viewModel = ProfileViewModel())
    }
}
