package com.compose.meet_dating.navbars

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.compose.meet_dating.main.model.ProfileData
import com.compose.meet_dating.main.model.ProfileViewModel

@Composable
fun LikesScreen(viewModel: ProfileViewModel = viewModel(), modifier: Modifier) {
//    val likedProfiles = remember { viewModel.likedProfiles } // FIXED collectAsState() issue

//    if (likedProfiles.isEmpty()) {
//        Box(
//            modifier = Modifier.fillMaxSize(),
//            contentAlignment = Alignment.Center
//        ) {
//            Text("No liked profiles yet", style = MaterialTheme.typography.bodyLarge)
//        }
//    } else {
//        LazyColumn(
//            modifier = Modifier.fillMaxSize()
//        ) {
//            items(likedProfiles) { profile ->
//                LikedProfileItem(profile = profile, viewModel)
//            }
//        }
//    }
}

@Composable
fun LikedProfileItem(profile: ProfileData, viewModel: ProfileViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .height(100.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                Text(
                    profile.name,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    profile.location,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

//            IconButton(
//                onClick = { viewModel.dislikeProfile(profile) },
//                modifier = Modifier.padding(end = 8.dp)
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Close,
//                    contentDescription = "Remove",
//                    tint = MaterialTheme.colorScheme.error
//                )
            }
        }

}
