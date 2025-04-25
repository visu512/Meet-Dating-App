package com.compose.meet_dating.utils

import android.content.Context
import androidx.core.content.edit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

object AuthUtils {
    private const val PREFS_NAME = "meet_dating_prefs"
    private const val KEY_IS_LOGGED_IN = "isLoggedIn"
    private const val KEY_USER_ID = "currentUserId"
    private val auth: FirebaseAuth = Firebase.auth

    private var googleLoginInProgress = false

    fun setGoogleLoginInProgress(inProgress: Boolean) {
        googleLoginInProgress = inProgress
    }

    fun isGoogleLoginInProgress(): Boolean {
        return googleLoginInProgress
    }

    fun isUserLoggedIn(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val fromPrefs = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        val firebaseUser = auth.currentUser

        return if (fromPrefs && firebaseUser != null) {
            val savedUid = prefs.getString(KEY_USER_ID, null)
            savedUid == firebaseUser.uid
        } else {
            false
        }
    }

    fun setUserLoggedIn(context: Context, loggedIn: Boolean, userId: String? = null) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putBoolean(KEY_IS_LOGGED_IN, loggedIn)
            userId?.let { putString(KEY_USER_ID, it) }
            apply()
        }
    }

    fun getCurrentUserId(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_USER_ID, null) ?: auth.currentUser?.uid
    }

    fun logoutUser(context: Context) {
        auth.signOut()
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            clear()
            apply()
        }
    }
}