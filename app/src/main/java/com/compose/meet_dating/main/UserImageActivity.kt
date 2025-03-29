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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compose.meet_dating.MainActivity
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
//                    finish()
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
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile image display
        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
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
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                else -> {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile placeholder",
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Select Image Button
        Button(
            onClick = onSelectImage,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        ) {
            Text("Select Image")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Continue Button
        Button(
            onClick = { viewModel.uploadImage(onComplete) },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.bitmap != null && !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Continue")
            }
        }

        // Error message
        if (uiState.errorMessage.isNotEmpty()) {
            Text(
                text = uiState.errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp)
            )
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
                // Save to Firestore
                val userData = hashMapOf(
                    "profileImage" to imageBase64,
                    "lastUpdated" to System.currentTimeMillis()
                )

                firestore.collection("users")
                    .document(currentUser.uid)
                    .update(userData.toMap())
                    .await()

                onComplete()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Upload failed: ${e.localizedMessage ?: "Unknown error"}",
                        isLoading = false
                    )
                }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}

data class UserImageUiState(
    val bitmap: Bitmap? = null,
    val isLoading: Boolean = false,
    val errorMessage: String = ""
)