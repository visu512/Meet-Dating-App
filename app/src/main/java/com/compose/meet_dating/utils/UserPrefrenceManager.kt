package com.compose.meet_dating.utils


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object UserPrefrenceManager {

    fun setUserOnline() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseDatabase.getInstance().reference
            .child("users").child(userId).child("online").setValue(true)
    }

    fun setUserOffline() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseDatabase.getInstance().reference
            .child("users").child(userId).child("online").setValue(false)
    }
}
