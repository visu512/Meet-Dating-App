package com.compose.meet_dating.activity

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
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
import com.compose.meet_dating.main.ChatViewModel
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
                    ChatScreen(
                        viewModel = chatViewModel,
                        navController = NavController(LocalContext.current)
                    )
                }

                2 -> LikeScreen(likedProfilesViewModel)
                3 -> ProfileScreen(viewModel = profileViewModel)
            }
        }
    }
}

@Composable
fun FilterBottomSheetContent(
    onClose: () -> Unit,
    onApply: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Filter", style = MaterialTheme.typography.titleLarge)
            IconButton(onClick = onClose) {
                Icon(
                    Icons.Default.Close,
                    tint = Color(0xFF616161),
                    contentDescription = "Close"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Example filters
        FilterOption("Nearby")
        FilterOption("Show only men")
        FilterOption("Show only women")
        FilterOption("Show everyone")

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                onApply()
                onClose()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Apply")
        }
    }
}

@Composable
fun FilterOption(option: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Gray)
        Spacer(modifier = Modifier.width(12.dp))
        Text(option, style = MaterialTheme.typography.bodyLarge)
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

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Matches",
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
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
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

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            containerColor = Color.White,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            FilterBottomSheetContent(
                onClose = { showSheet = false },
                onApply = {
                    // Handle filter application here
                }
            )
        }
    }
}

@Composable
fun ProfileCard(profile: ProfileData, likedProfilesViewModel: LikedProfilesViewModel) {
    val bitmap = profile.base64Image?.let { decodeBase64ToBitmap(it) }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 50.dp)
            .aspectRatio(2.4f / 4f)/// height, width
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
                .padding(start = 10.dp, top = 10.dp)
                .align(Alignment.TopStart),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "${profile.name}, ${profile.age}",/// name + age
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium
            )

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
                            putExtra("senderId", currentUserId)
                            putExtra("receiverId", profile.userId)
                        }
                        context.startActivity(intent)
                    }
            ) {
                Text(
                    text = "Open to chat",
                    color = Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 1.5.dp)
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
                    painter = painterResource(id = R.drawable.lovess),
                    contentDescription = "Like",
                    tint = Color.Black,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

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

@Composable
fun BottomNavigationBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    Box(modifier = Modifier.height(58.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Divider(
                color = Color.LightGray,
                thickness = 0.245.dp,
                modifier = Modifier.fillMaxWidth()
            )

            NavigationBar(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.White,
                contentColor = Color.Black,
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { onTabSelected(0) },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.cardswap),
                            contentDescription = "Meet",
                            tint = if (selectedTab == 0) Color.Black else Color(0xFF797878),
                            modifier = Modifier.size(23.dp)
                        )
                    },
                    label = {
                        Text(
                            "Matches",
                            fontWeight = FontWeight.Normal,
                            color = if (selectedTab == 1) Color.Black else Color(0xFF616161)
                        )
                    },
                    alwaysShowLabel = true,
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { onTabSelected(1) },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.typing
                        ),
                            contentDescription = "Chats",
                            tint = if (selectedTab == 1) Color.Black else Color(0xFF616161),
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            "Chats",
                            fontWeight = FontWeight.Normal,
                            color = if (selectedTab == 1) Color.Black else Color(0xFF616161)
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
                            painter = painterResource(id = R.drawable.lovess),
                            contentDescription = "Likes",
                            tint = if (selectedTab == 2) Color.Black else Color(0xFF616161),
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            "Likes",
                            fontWeight = FontWeight.Normal,
                            color = if (selectedTab == 2) Color.Black else Color(0xFF616161)
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
                            tint = if (selectedTab == 3) Color.Black else Color(0xFF616161),
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            "Profile",
                            fontWeight = FontWeight.Normal,
                            color = if (selectedTab == 3) Color.Black else Color(0xFF616161)
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