package com.compose.meet_dating.main.model

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LikedProfilesViewModel : ViewModel() {
    private val _likedProfiles = MutableStateFlow<List<ProfileData>>(emptyList())
    val likedProfiles: StateFlow<List<ProfileData>> = _likedProfiles

    fun addLikedProfile(profile: ProfileData) {
        _likedProfiles.value = _likedProfiles.value + profile
    }

    companion object
}
