package com.compose.meet_dating.activity

import ChatViewModel
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.compose.meet_dating.R
import com.compose.meet_dating.chats.ChatActivity
import com.compose.meet_dating.main.model.LikedProfilesViewModel
import com.compose.meet_dating.main.model.ProfileData
import com.compose.meet_dating.main.model.ProfileViewModel
import com.compose.meet_dating.navbars.ChatScreen
import com.compose.meet_dating.navbars.LikeScreen
import com.compose.meet_dating.navbars.ProfileScreen
import com.compose.meet_dating.ui.theme.MeetDatingAppTheme
import com.google.accompanist.pager.*
import java.io.File
import java.io.FileOutputStream

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
    val likedProfilesViewModel = remember { LikedProfilesViewModel() }
    val profileViewModel = remember { ProfileViewModel() }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(selectedTab) { index -> selectedTab = index }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> EncounterScreen(
                    viewModel = profileViewModel,
                    likedProfilesViewModel = likedProfilesViewModel
                )

                1 -> {
                    val chatViewModel: ChatViewModel = viewModel()
                    ChatScreen(viewModel = chatViewModel, navController = NavController)
                }

                2 -> LikeScreen(likedProfilesViewModel)
                3 -> ProfileScreen(viewModel = profileViewModel)
            }
        }
    }
}


@Composable
fun FilterBottomSheetContent(
    onOptionSelected: (String) -> Unit
) {
    val options = listOf("Nearby", "Show only men", "Show only women", "Show everyone")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Filter Options",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        options.forEach { option ->
            Button(
                onClick = { onOptionSelected(option) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFf0f0f0),
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(option, fontSize = 16.sp)
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EncounterScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel,
    likedProfilesViewModel: LikedProfilesViewModel
) {
    val profiles by viewModel.profiles.collectAsState()
    val pagerState = rememberPagerState()

    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            containerColor = Color.White
        ) {
            FilterBottomSheetContent(
                onOptionSelected = { selected ->
                    showSheet = false
                    // Apply filter logic here
                }
            )
        }
    }

    ///top bar
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 45.dp,),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Meet",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp
            )
            IconButton(onClick = { showSheet = true }) {
                Icon(
                    painter = painterResource(id = R.drawable.settings),
                    contentDescription = "Filter",
                    modifier = Modifier.padding(6.dp)
                )
            }
        }

        if (profiles.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                HorizontalPager(
                    state = pagerState,
                    count = profiles.size,
                    modifier = Modifier.fillMaxWidth()
                ) { page ->
                    ProfileCard(
                        profile = profiles[page],
                        likedProfilesViewModel = likedProfilesViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileCard(profile: ProfileData, likedProfilesViewModel: LikedProfilesViewModel) {
    val bitmap = profile.base64Image?.let { decodeBase64ToBitmap(it) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .aspectRatio(2.45f / 4f) // height ,width
            .border(
                width = 1.dp,
                color = Color.LightGray,
                shape = RoundedCornerShape(20.dp)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(Color.LightGray)
    ) {
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } ?: run {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                Text("No Image", color = Color.White)
            }
        }

        // Top gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(125.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(0.5f), Color.Transparent)
                    )
                )
        )

        // Bottom gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(0.2f))
                    )
                )
        )

        // User details
        Column(
            modifier = Modifier
                .padding(start = 8.dp, top = 8.dp)
                .align(Alignment.TopStart),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "${profile.name}, ${profile.age}",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium
            )

            val context = LocalContext.current
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                modifier = Modifier
                    .padding(vertical = 2.dp)
                    .clickable {
                        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                        val currentUserId = prefs.getString("currentUserId", "") ?: ""

                        val imageUri =
                            profile.base64Image?.let { saveBase64ToImageUri(context, it) }

                        val intent = Intent(context, ChatActivity::class.java).apply {
                            putExtra("name", profile.name)
                            putExtra("profileImageUri", imageUri?.toString())
                            putExtra("senderId", currentUserId) // Current user (YOU)
                            putExtra(
                                "receiverId",
                                profile.userId
                            ) // The other person you're chatting with
                        }
                        context.startActivity(intent)
                    }


            ) {
                Text(
                    text = "Open to chat",
                    color = Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.Black.copy(alpha = 0.3f),
                modifier = Modifier.height(30.dp)
            ) {
                Text(
                    text = "~ ${profile.location}",
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                )
            }
        }

        // Like & Dislike buttons
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
                    .size(65.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.dislike),
                    contentDescription = "Dislike",
                    tint = Color.Black,
                    modifier = Modifier.size(30.dp)
                )
            }

            IconButton(
                onClick = { likedProfilesViewModel.addLikedProfile(profile) },
                modifier = Modifier
                    .size(65.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.heart),
                    contentDescription = "Like",
                    tint = Color.Black,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}


/// helper function for resolve image sending to chat activity problem
fun saveBase64ToImageUri(context: Context, base64: String): Uri? {
    return try {
        val bytes = Base64.decode(base64, Base64.DEFAULT)
        val file = File(context.cacheDir, "profile_temp_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { it.write(bytes) }

        FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    } catch (e: Exception) {
        e.printStackTrace()
        null
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

////// bottom nav bar
@Composable
fun BottomNavigationBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    Box(modifier = Modifier.height(58.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top gray divider line
            Divider(
                color = Color.LightGray,
                thickness = 0.245.dp,
                modifier = Modifier.fillMaxWidth()
            )

            // BottomNavigationBar with transparent selection background
            NavigationBar(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.White,
                contentColor = Color.Black,
                tonalElevation = 0.dp // Remove elevation effect
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { onTabSelected(0) },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.cardswap),
                            contentDescription = "Meet",
                            tint = if (selectedTab == 0) Color.Black else Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            "Meet",
                            fontWeight = FontWeight.Normal,
                            color = if (selectedTab == 0) Color.Black else Color.Gray
                        )
                    },
                    alwaysShowLabel = true,
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent // Removes the purple background
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { onTabSelected(1) },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.chat),
                            contentDescription = "Chats",
                            tint = if (selectedTab == 1) Color.Black else Color.Gray,
                            modifier = Modifier.size(23.dp)
                        )
                    },
                    label = {
                        Text(
                            "Chats",
                            fontWeight = FontWeight.Normal,
                            color = if (selectedTab == 1) Color.Black else Color.Gray
                        )
                    },
                    alwaysShowLabel = true,
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { onTabSelected(2) },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.heart),
                            contentDescription = "Likes",
                            tint = if (selectedTab == 2) Color.Black else Color.Gray,
                            modifier = Modifier.size(23.dp)
                        )
                    },
                    label = {
                        Text(
                            "Likes",
                            fontWeight = FontWeight.Normal,
                            color = if (selectedTab == 2) Color.Black else Color.Gray
                        )
                    },
                    alwaysShowLabel = true,
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { onTabSelected(3) },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.user),
                            contentDescription = "Profile",
                            tint = if (selectedTab == 3) Color.Black else Color.Gray,
                            modifier = Modifier.size(23.dp)
                        )
                    },
                    label = {
                        Text(
                            "Profile",
                            fontWeight = FontWeight.Normal,
                            color = if (selectedTab == 3) Color.Black else Color.Gray
                        )
                    },
                    alwaysShowLabel = true,
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewEncounterScreen() {
    MeetDatingAppTheme {
        val profileViewModel = ProfileViewModel()
        val likedProfilesViewModel = LikedProfilesViewModel()
        EncounterScreen(
            viewModel = profileViewModel,
            likedProfilesViewModel = likedProfilesViewModel
        )
    }
}
