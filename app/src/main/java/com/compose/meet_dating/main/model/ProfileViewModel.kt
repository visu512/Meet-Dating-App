package com.compose.meet_dating.main.model

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayInputStream

data class ProfileData(
    val id: String,
    val name: String,
    val age: Int,
    val location: String,
    val base64Image: String?
)

class ProfileViewModel : ViewModel() {
    private val _profiles = MutableStateFlow<List<ProfileData>>(emptyList())
    val profiles: StateFlow<List<ProfileData>> = _profiles.asStateFlow()

    private val firestore = FirebaseFirestore.getInstance()

    init {
        fetchProfiles()
    }

    private fun fetchProfiles() {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("users").get().await()
                val profilesList = snapshot.documents.mapNotNull { doc ->
                    try {
                        ProfileData(
                            id = doc.id,
                            name = doc.getString("username") ?: "Anonymous",
                            age = doc.getLong("age")?.toInt() ?: 0,
                            location = doc.getString("location") ?: "Unknown",
                            base64Image = doc.getString("profileImage")
                        )
                    } catch (e: Exception) {
                        Log.e("ProfileViewModel", "Error parsing profile", e)
                        null
                    }
                }
                _profiles.value = profilesList
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error fetching profiles", e)
            }
        }
    }

    fun decodeBase64ToBitmap(base64: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64, Base64.DEFAULT)
            BitmapFactory.decodeStream(ByteArrayInputStream(decodedBytes))
        } catch (e: Exception) {
            Log.e("ImageUtils", "Error decoding Base64 image", e)
            null
        }
    }
}