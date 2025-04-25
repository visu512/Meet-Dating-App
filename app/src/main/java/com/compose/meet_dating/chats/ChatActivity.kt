package com.compose.meet_dating.chats

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.compose.meet_dating.R
import com.compose.meet_dating.utils.UserPrefrenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val name = intent.getStringExtra("name") ?: "Unknown"
        val senderId = intent.getStringExtra("senderId") ?: ""
        val receiverId = intent.getStringExtra("receiverId") ?: ""
        val base64Image = intent.getStringExtra("base64Image")
        var bitmap: Bitmap? = null

        if (base64Image != null) {
            val bytes = android.util.Base64.decode(base64Image, android.util.Base64.DEFAULT)
            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } else {
            val imageUriString = intent.getStringExtra("profileImageUri")
            val imageUri = imageUriString?.let { Uri.parse(it) }
            bitmap = imageUri?.let {
                val inputStream = contentResolver.openInputStream(it)
                BitmapFactory.decodeStream(inputStream)
            }
        }

        setContent {
            ChatScreen(
                contactName = name,
                profileBitmap = bitmap,
                senderId = senderId,
                receiverId = receiverId
            )
        }
    }

    override fun onResume() {
        super.onResume()
        UserPrefrenceManager.setUserOnline()
    }

    override fun onPause() {
        super.onPause()
        UserPrefrenceManager.setUserOffline()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(contactName: String, profileBitmap: Bitmap?, senderId: String, receiverId: String) {
    val context = LocalContext.current
    val messages = remember { mutableStateListOf<ChatMessage>() }
    var inputText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedMessageId by remember { mutableStateOf<String?>(null) }
    var isReceiverOnline by remember { mutableStateOf(false) }

    val chatId = listOf(senderId, receiverId).sorted().joinToString("_")
    val database = FirebaseDatabase.getInstance().reference
    val messagesRef = database.child("chats").child(chatId).child("messages")
    val userStatusRef = database.child("users").child(receiverId).child("online")

    DisposableEffect(chatId) {
        val messageListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newMessages = snapshot.children.mapNotNull {
                    it.getValue(ChatMessage::class.java)
                }.sortedBy { it.timestamp }
                messages.clear()
                messages.addAll(newMessages)
            }

            override fun onCancelled(error: DatabaseError) {
                errorMessage = "Failed to load messages: ${error.message}"
            }
        }

        val onlineListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                isReceiverOnline = snapshot.getValue(Boolean::class.java) ?: false
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        messagesRef.addValueEventListener(messageListener)
        userStatusRef.addValueEventListener(onlineListener)

        onDispose {
            messagesRef.removeEventListener(messageListener)
            userStatusRef.removeEventListener(onlineListener)
        }
    }

    if (showDeleteDialog && selectedMessageId != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    messagesRef.child(selectedMessageId!!).removeValue()
                    showDeleteDialog = false
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("Delete Message?") },
            text = { Text("Are you sure you want to delete this message?") }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    profileBitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "Profile",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .border(1.dp, Color.LightGray, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } ?: Icon(
                        painter = painterResource(id = R.drawable.user),
                        contentDescription = "Profile",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .border(0.5.dp, Color.LightGray, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = contactName, fontSize = 17.sp, color = Color.Black)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isReceiverOnline) Color(0xFF006400) else Color.Gray)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isReceiverOnline) "Online" else "Offline",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isReceiverOnline) Color(0xFF006400) else Color.Gray
                            )
                        }
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = { (context as Activity).finish() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                        contentDescription = "Back"
                    )
                }
            }
        )

        Divider(color = Color.LightGray, modifier = Modifier.height(1.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp),
            reverseLayout = true
        ) {
            items(messages.reversed()) { message ->
                MessageBubble(
                    message = message,
                    isMe = message.senderId == senderId,
                    onLongPress = {
                        selectedMessageId = message.id
                        showDeleteDialog = true
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier
                    .weight(1f)
                    .background(Color.LightGray, RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                textStyle = TextStyle(fontSize = 16.sp),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (inputText.isEmpty()) {
                            Text("Type a message...", color = Color.Gray)
                        }
                        innerTextField()
                    }
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        val messageId = messagesRef.push().key ?: ""
                        val message = ChatMessage(
                            id = messageId,
                            text = inputText,
                            senderId = senderId,
                            receiverId = receiverId,
                            timestamp = System.currentTimeMillis()
                        )
                        messagesRef.child(messageId).setValue(message)
                            .addOnSuccessListener { inputText = "" }
                            .addOnFailureListener { e ->
                                errorMessage = "Failed to send message: ${e.message}"
                            }
                    }
                },
                enabled = inputText.isNotBlank()
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send")
            }
        }

        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            errorMessage = null
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage, isMe: Boolean, onLongPress: () -> Unit) {
    val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(message.timestamp))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Column(
            modifier = Modifier
                .wrapContentSize()
                .clip(RoundedCornerShape(12.dp))
                .background(if (isMe) Color(0xFF03A9F4) else Color(0xFF03A9F4))
                .padding(10.dp)
                .pointerInput(Unit) {
                    detectTapGestures(onLongPress = { onLongPress() })
                }
        ) {
            Text(text = message.text, fontSize = 16.sp)
        }
        Text(
            text = time,
            fontSize = 10.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 2.dp, start = 8.dp, end = 8.dp)
        )
    }
}

data class ChatMessage(
    val id: String = "",
    val text: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val timestamp: Long = 0L
)
