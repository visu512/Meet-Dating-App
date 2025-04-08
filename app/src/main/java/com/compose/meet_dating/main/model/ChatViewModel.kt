//package com.compose.meet_dating.main.viewmodel
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.compose.meet_dating.main.model.ProfileData
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.tasks.await
//
//class ChatViewModel : ViewModel() {
//    private val _chatProfiles = MutableStateFlow<List<ProfileData>>(emptyList())
//    val chatProfiles: StateFlow<List<ProfileData>> = _chatProfiles.asStateFlow()
//
//    private val firestore = FirebaseFirestore.getInstance()
//    private val auth = FirebaseAuth.getInstance()
//
//    init {
//        fetchChatProfiles()
//    }
//
//    private fun fetchChatProfiles() {
//        viewModelScope.launch {
//            try {
//                val currentUserUid = auth.currentUser?.uid ?: return@launch
//
//                // Get all unique chat partners
//                val messages = firestore.collection("messages")
//                    .whereEqualTo("senderId", currentUserUid)
//                    .get()
//                    .await()
//
//                val uniqueReceiverIds = messages.documents
//                    .mapNotNull { it.getString("receiverId") }
//                    .toSet()
//
//                // Fetch profile data for each chat partner
//                val profiles = uniqueReceiverIds.mapNotNull { uid ->
//                    try {
//                        val doc = firestore.collection("users").document(uid).get().await()
//                        ProfileData(
//                            name = doc.getString("name") ?: "Unknown",
//                            location = doc.getString("location") ?: "",
//                            base64Image = doc.getString("base64Image")
//                        )
//                    } catch (e: Exception) {
//                        null
//                    }
//                }
//
//                _chatProfiles.value = profiles
//            } catch (e: Exception) {
//                // Handle error (you might want to update UI accordingly)
//                _chatProfiles.value = emptyList()
//            }
//        }
//    }
//}
//
//
////
////
////import androidx.lifecycle.ViewModel
////import androidx.lifecycle.viewModelScope
////import com.compose.meet_dating.chats.ChatMessage
////import com.compose.meet_dating.main.model.ProfileData
////import com.google.firebase.auth.FirebaseAuth
////import com.google.firebase.database.*
////import kotlinx.coroutines.flow.MutableStateFlow
////import kotlinx.coroutines.flow.StateFlow
////import kotlinx.coroutines.flow.asStateFlow
////import kotlinx.coroutines.launch
////
////class ChatViewModel : ViewModel() {
////    private val _chatProfiles = MutableStateFlow<List<ProfileData>>(emptyList())
////    val chatProfiles: StateFlow<List<ProfileData>> = _chatProfiles.asStateFlow()
////
////    private val _lastMessages = MutableStateFlow<Map<String, ChatMessage>>(emptyMap())
////    val lastMessages: StateFlow<Map<String, ChatMessage>> = _lastMessages.asStateFlow()
////
////    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
////    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
////
////    init {
////        fetchChatProfiles()
////    }
////
////    private fun fetchChatProfiles() {
////        viewModelScope.launch {
////            val profilesRef = database.getReference("users")
////            profilesRef.addValueEventListener(object : ValueEventListener {
////                override fun onDataChange(snapshot: DataSnapshot) {
////                    val profiles = mutableListOf<ProfileData>()
////                    snapshot.children.forEach { userSnapshot ->
////                        if (userSnapshot.key != currentUserId) {
////                            val profile = userSnapshot.getValue(ProfileData::class.java)
////                            profile?.let {
////                                it.userId = userSnapshot.key ?: ""
////                                profiles.add(it)
////                                fetchLastMessage(it.userId)
////                            }
////                        }
////                    }
////                    _chatProfiles.value = profiles
////                }
////
////                override fun onCancelled(error: DatabaseError) {
////                    // Handle error
////                }
////            })
////        }
////    }
////
////    private fun fetchLastMessage(receiverId: String) {
////        val chatId = listOf(currentUserId, receiverId).sorted().joinToString("_")
////        val messagesRef = database.getReference("chats/$chatId/messages")
////            .orderByChild("timestamp")
////            .limitToLast(1)
////
////        messagesRef.addValueEventListener(object : ValueEventListener {
////            override fun onDataChange(snapshot: DataSnapshot) {
////                snapshot.children.firstOrNull()?.let { messageSnapshot ->
////                    val message = messageSnapshot.getValue(ChatMessage::class.java)
////                    message?.let {
////                        _lastMessages.value = _lastMessages.value + (receiverId to it)
////                    }
////                }
////            }
////
////            override fun onCancelled(error: DatabaseError) {
////                // Handle error
////            }
////        })
////    }
////}







import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compose.meet_dating.chats.ChatMessage
import com.compose.meet_dating.main.model.ProfileData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val _chatProfiles = MutableStateFlow<List<ProfileData>>(emptyList())
    val chatProfiles: StateFlow<List<ProfileData>> = _chatProfiles.asStateFlow()

    private val _lastMessages = MutableStateFlow<Map<String, ChatMessage>>(emptyMap())
    val lastMessages: StateFlow<Map<String, ChatMessage>> = _lastMessages.asStateFlow()

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    init {
        fetchChatProfiles()
    }

    private fun fetchChatProfiles() {
        viewModelScope.launch {
            val profilesRef = database.getReference("users")
            profilesRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val profiles = mutableListOf<ProfileData>()
                    snapshot.children.forEach { userSnapshot ->
                        if (userSnapshot.key != currentUserId) {
                            val profile = userSnapshot.getValue(ProfileData::class.java)
                            profile?.let {
                                it.userId = userSnapshot.key ?: ""
                                profiles.add(it)
                                fetchLastMessage(it.userId)
                            }
                        }
                    }
                    _chatProfiles.value = profiles
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }
    }

    private fun fetchLastMessage(receiverId: String) {
        val chatId = listOf(currentUserId, receiverId).sorted().joinToString("_")
        val messagesRef = database.getReference("chats/$chatId/messages")
            .orderByChild("timestamp")
            .limitToLast(1)

        messagesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.firstOrNull()?.let { messageSnapshot ->
                    val message = messageSnapshot.getValue(ChatMessage::class.java)
                    message?.let {
                        _lastMessages.value = _lastMessages.value + (receiverId to it)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
}
