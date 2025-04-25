package com.compose.meet_dating.main

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
