package com.compose.meet_dating.main

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compose.meet_dating.R
import com.compose.meet_dating.activity.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.*

class UserImageActivity : ComponentActivity() {
    private val viewModel: UserImageViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    private val selectImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { viewModel.setImageUri(it, contentResolver) }
        }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            UserImageScreen(
                viewModel = viewModel,
                onSelectImage = { selectImageLauncher.launch("image/*") },
                onComplete = {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            )
        }
    }
}

@Composable
fun UserImageScreen(
    viewModel: UserImageViewModel,
    onSelectImage: () -> Unit,
    onComplete: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var name by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFFBFA),
                        Color(0xFFFFFBFA)
                    )
                )
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 72.dp), // space for bottom button
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Let's make a stunning first impression!",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFB00020),
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "choose your best photo, your profile will be visible to all members for matching 💘",
                fontSize = 16.sp,
                color = Color.DarkGray,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))


            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(2.dp, Color.Black, RoundedCornerShape(20.dp)) // Black border
                    .clickable { onSelectImage() },
                contentAlignment = Alignment.Center,

            ) {
                when {
                    uiState.bitmap != null -> {
                        Image(
                            bitmap = uiState.bitmap!!.asImageBitmap(),
                            contentDescription = "Profile image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    uiState.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = Color(0xFFB00020)
                        )
                    }

                    else -> {
                        Icon(
                            painter = painterResource(id = R.drawable.user),
                            contentDescription = "Profile placeholder",
                            modifier = Modifier.size(80.dp),
                            tint = Color.Gray,
                        )
                    }
                }
            }

            if (uiState.errorMessage.isNotEmpty()) {
                Text(
                    text = uiState.errorMessage,
                    color = Color.Red,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }

        // Bottom Button
        Button(
            onClick = { viewModel.uploadImage(onComplete) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(30.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White,
                disabledContentColor = Color.White,
                disabledContainerColor = Color.Black
            ),
            enabled = uiState.bitmap != null && !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Upload Photo", fontSize = 18.sp, fontWeight = FontWeight.Normal)
            }
        }
    }
}


class UserImageViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = Firebase.firestore
) : ViewModel() {
    private val _uiState = MutableStateFlow(UserImageUiState())
    val uiState: StateFlow<UserImageUiState> = _uiState.asStateFlow()

    private var base64Image: String? = null

    @RequiresApi(Build.VERSION_CODES.O)
    fun setImageUri(uri: Uri, contentResolver: android.content.ContentResolver) {
        _uiState.update { it.copy(isLoading = true, errorMessage = "") }

        try {
            val bitmap = if (Build.VERSION.SDK_INT < 28) {
                MediaStore.Images.Media.getBitmap(contentResolver, uri)
            } else {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri))
            }

            // Convert to Base64
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream)
            val imageBytes = byteArrayOutputStream.toByteArray()
            base64Image = Base64.getEncoder().encodeToString(imageBytes)

            _uiState.update {
                it.copy(
                    bitmap = bitmap,
                    isLoading = false,
                    errorMessage = ""
                )
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    errorMessage = "Failed to process image: ${e.localizedMessage}",
                    isLoading = false
                )
            }
        }
    }

    fun uploadImage(onComplete: () -> Unit) {
        val currentUser = auth.currentUser ?: return
        val imageBase64 = base64Image ?: return

        _uiState.update { it.copy(isLoading = true, errorMessage = "") }

        viewModelScope.launch {
            try {
                firestore.collection("users")
                    .document(currentUser.uid)
                    .update(mapOf("profileImage" to imageBase64))
                    .await()

                onComplete()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Upload failed: ${e.localizedMessage}",
                        isLoading = false
                    )
                }
            }
        }
    }
}

data class UserImageUiState(
    val bitmap: Bitmap? = null,
    val isLoading: Boolean = false,
    val errorMessage: String = ""
)
