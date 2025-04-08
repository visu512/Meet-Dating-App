package com.compose.meet_dating.main.model

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.common.base.Verify
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.ByteArrayInputStream
import java.lang.Boolean.getBoolean

data class ProfileData(
    var userId: String = "",
    val name: String = "",
    val age: Int = 0,
    val email: String = "",
    val location: String = "",
    val base64Image: String? = null,
    val gender: String = ""

)

class ProfileViewModel : ViewModel() {
    private val _profiles = MutableStateFlow<List<ProfileData>>(emptyList())
    val profiles: StateFlow<List<ProfileData>> = _profiles.asStateFlow()

    private val firestore = FirebaseFirestore.getInstance()

    init {
        fetchProfilesBasedOnPreference() // Automatically loads filtered profiles
    }

    fun fetchProfilesBasedOnPreference() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                val preference = document.getString("genderPreference") ?: "Everyone"
                val usersRef = firestore.collection("users")

                val query = when (preference) {
                    "Women" -> usersRef.whereEqualTo("gender", "I’m a Woman")
                    "Men" -> usersRef.whereEqualTo("gender", "I’m a Man")
                    else -> usersRef // "Everyone" show
                }

                query.get()
                    .addOnSuccessListener { result ->
                        val profilesList = result.documents
                            .filter { it.id != userId } // Exclude current user
                            .mapNotNull { doc ->
                                try {
                                    ProfileData(
                                        userId = doc.id,
                                        name = doc.getString("username") ?: "Anonymous",
                                        age = doc.getLong("age")?.toInt() ?: 0,
                                        email = doc.getString("email") ?: "Unknown",
                                        location = doc.getString("location") ?: "Unknown",
                                        base64Image = doc.getString("profileImage"),
                                        gender = doc.getString("gender") ?: "",
                                    )
                                } catch (e: Exception) {
                                    Log.e("ProfileViewModel", "Error parsing profile", e)
                                    null
                                }
                            }

                        _profiles.value = profilesList
                        Log.d("ProfileViewModel", "Filtered Profiles: ${_profiles.value}")
                    }
                    .addOnFailureListener {
                        Log.e("ProfileViewModel", "Error fetching filtered profiles", it)
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

    fun fetchUserProfile(userEmail: String) {
        if (userEmail.isEmpty()) return

        firestore.collection("users")
            .whereEqualTo("email", userEmail)
            .get()
            .addOnSuccessListener { snapshot ->
                val userProfile = snapshot.documents.firstOrNull()?.let { doc ->
                    ProfileData(
                        userId = doc.id,
                        name = doc.getString("username") ?: "Anonymous",
                        age = doc.getLong("age")?.toInt() ?: 0,
                        email = doc.getString("email") ?: "Unknown",
                        location = doc.getString("location") ?: "Unknown",
                        base64Image = doc.getString("profileImage"),
                        gender = doc.getString("gender") ?: "",

                        )
                }

                userProfile?.let {
                    _profiles.value = listOf(it)
                }
            }
            .addOnFailureListener { e ->
                Log.e("ProfileViewModel", "Failed to fetch user profile", e)
            }
    }

//    fun fetchCurrentUserProfile() {
//        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
//
//        firestore.collection("users").document(userId)
//            .get()
//            .addOnSuccessListener { doc ->
//                val profile = ProfileData(
//                    id = doc.id,
//                    name = doc.getString("username") ?: "Anonymous",
//                    age = doc.getLong("age")?.toInt() ?: 0,
//                    email = doc.getString("email") ?: "Unknown",
//                    location = doc.getString("location") ?: "Unknown",
//                    base64Image = doc.getString("profileImage"),
//                    gender = doc.getString("gender") ?: ""
//                )
//                _profiles.value = listOf(profile)
//            }
//            .addOnFailureListener {
//                Log.e("ProfileViewModel", "Failed to fetch current user profile", it)
//            }
//    }

}
