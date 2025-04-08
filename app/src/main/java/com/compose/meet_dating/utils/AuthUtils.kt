package com.compose.meet_dating.utils

import android.content.Context
import androidx.core.content.edit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

object AuthUtils {
    private const val PREFS_NAME = "meet_dating_prefs"
    private const val KEY_IS_LOGGED_IN = "isLoggedIn"
    private val auth: FirebaseAuth = Firebase.auth

    fun isUserLoggedIn(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val fromPrefs = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        val firebaseUser = auth.currentUser
        return fromPrefs && firebaseUser != null
    }

    fun setUserLoggedIn(context: Context, loggedIn: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putBoolean(KEY_IS_LOGGED_IN, loggedIn)
            apply()
        }
    }

    fun logoutUser(context: Context) {
        auth.signOut()
        setUserLoggedIn(context, false)
    }
}