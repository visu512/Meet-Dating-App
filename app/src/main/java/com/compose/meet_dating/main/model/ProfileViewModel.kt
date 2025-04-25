package com.compose.meet_dating.main.model

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.ByteArrayInputStream

data class ProfileData(
    var userId: String = "",
    val username: String = "",
    val name: String = "",
    val age: Int = 0,
    val email: String = "",
    val location: String = "",
    val base64Image: String? = null,
    val gender: String = "",
    val id: String = ""
)

class ProfileViewModel : ViewModel() {
    private val _profiles = MutableStateFlow<List<ProfileData>>(emptyList())
    val profiles: StateFlow<List<ProfileData>> = _profiles.asStateFlow()

    private val _currentUserProfile = MutableStateFlow<ProfileData?>(null)
    val currentUserProfile: StateFlow<ProfileData?> = _currentUserProfile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    init {
        fetchCurrentUserProfile()
        fetchProfilesBasedOnPreference()
    }

    fun fetchCurrentUserProfile() {
        val userId = auth.currentUser?.uid ?: return
        _isLoading.value = true
        _errorMessage.value = null

        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { doc ->
                _isLoading.value = false
                if (doc.exists()) {
                    val profile = ProfileData(
                        userId = userId,
                        username = doc.getString("username") ?: doc.getString("username") ?: "Anonymous",
                        name = doc.getString("username") ?: "Anonymous",
                        age = doc.getLong("age")?.toInt() ?: 0,
                        email = doc.getString("email") ?: "Unknown",
                        location = doc.getString("location") ?: "Unknown",
                        base64Image = doc.getString("profileImage"),
                        gender = doc.getString("gender") ?: "",
                        id = userId
                    )
                    _currentUserProfile.value = profile
                } else {
                    _errorMessage.value = "User profile not found"
                }
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _errorMessage.value = "Failed to fetch profile: ${e.message}"
                Log.e("ProfileViewModel", "Failed to fetch current user profile", e)
            }
    }

    fun fetchProfilesBasedOnPreference() {
        val userId = auth.currentUser?.uid ?: return
        _isLoading.value = true
        _errorMessage.value = null

        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                val preference = document.getString("genderPreference") ?: "Everyone"
                val usersRef = firestore.collection("users")

                val query = when (preference) {
                    "Women" -> usersRef.whereEqualTo("gender", "I'm a Woman")
                    "Men" -> usersRef.whereEqualTo("gender", "I'm a Man")
                    else -> usersRef
                }

                query.get()
                    .addOnSuccessListener { result ->
                        _isLoading.value = false
                        val profilesList = result.documents
                            .filter { it.id != userId }
                            .mapNotNull { doc ->
                                try {
                                    ProfileData(
                                        userId = doc.id,
                                        username = doc.getString("username") ?: "Anonymous",
                                        name = doc.getString("username") ?: "Anonymous",
                                        age = doc.getLong("age")?.toInt() ?: 0,
                                        email = doc.getString("email") ?: "Unknown",
                                        location = doc.getString("location") ?: "Unknown",
                                        base64Image = doc.getString("profileImage"),
                                        gender = doc.getString("gender") ?: "",
                                        id = doc.id
                                    )
                                } catch (e: Exception) {
                                    Log.e("ProfileViewModel", "Error parsing profile", e)
                                    null
                                }
                            }
                        _profiles.value = profilesList
                    }
                    .addOnFailureListener { e ->
                        _isLoading.value = false
                        _errorMessage.value = "Failed to fetch profiles: ${e.message}"
                        Log.e("ProfileViewModel", "Error fetching filtered profiles", e)
                    }
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _errorMessage.value = "Failed to fetch preferences: ${e.message}"
                Log.e("ProfileViewModel", "Error fetching user preferences", e)
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

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}