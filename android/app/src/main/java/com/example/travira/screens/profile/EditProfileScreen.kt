package com.example.travira.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.travira.auth.TokenManager
import com.example.travira.model.User
import com.example.travira.remote.CloudinaryUploader
import com.example.travira.remote.RetrofitInstance
import com.example.travira.remote.UpdateProfileRequest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    tokenManager: TokenManager,
    user: User?,
    onBack: () -> Unit,
    onSaved: (User) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf(user?.name ?: "") }
    var phone by remember { mutableStateOf(user?.phone ?: "") }
    var location by remember { mutableStateOf(user?.location ?: "") }
    var profileUrl by remember { mutableStateOf(user?.profileImage) }
    var coverUrl by remember { mutableStateOf(user?.coverImage) }
    var profileUri by remember { mutableStateOf<Uri?>(null) }
    var coverUri by remember { mutableStateOf<Uri?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var success by remember { mutableStateOf<String?>(null) }

    val profilePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) profileUri = uri
    }
    val coverPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) coverUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1565C0),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .background(Color(0xFFF5F5F7))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                val coverModel: Any? = coverUri ?: coverUrl
                if (coverModel != null) {
                    AsyncImage(
                        model = coverModel,
                        contentDescription = "Cover",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF90CAF9))
                    )
                }
                IconButton(
                    onClick = { coverPicker.launch("image/*") },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Change cover", tint = Color.White)
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 40.dp)
                ) {
                    val profileModel: Any? = profileUri ?: profileUrl
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(3.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE3F2FD))
                            .clickable { profilePicker.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (profileModel != null) {
                            AsyncImage(
                                model = profileModel,
                                contentDescription = "Profile",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Default.AddAPhoto,
                                contentDescription = "Add photo",
                                tint = Color(0xFF1565C0),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(56.dp))

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = user?.email ?: "",
                    onValueChange = {},
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (error != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(error!!, color = Color(0xFFC62828), fontSize = 13.sp)
                }
                if (success != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(success!!, color = Color(0xFF2E7D32), fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        scope.launch {
                            loading = true
                            error = null
                            success = null
                            try {
                                val token = tokenManager.accessToken
                                    ?: throw Exception("Not logged in")
                                var newProfile = profileUrl
                                var newCover = coverUrl
                                if (profileUri != null) {
                                    newProfile = CloudinaryUploader.uploadImage(context, profileUri!!)
                                }
                                if (coverUri != null) {
                                    newCover = CloudinaryUploader.uploadImage(context, coverUri!!)
                                }
                                val res = RetrofitInstance.authApi.updateProfile(
                                    "Bearer $token",
                                    UpdateProfileRequest(
                                        name = name.trim().ifBlank { null },
                                        phone = phone.trim().ifBlank { null },
                                        location = location.trim().ifBlank { null },
                                        profileImage = newProfile,
                                        coverImage = newCover
                                    )
                                )
                                val updated = res.user
                                if (updated != null) {
                                    profileUrl = updated.profileImage
                                    coverUrl = updated.coverImage
                                    profileUri = null
                                    coverUri = null
                                    success = "Profile saved"
                                    onSaved(updated)
                                } else {
                                    error = res.message ?: "Failed to save"
                                }
                            } catch (e: Exception) {
                                error = e.message ?: "Failed to save profile"
                            } finally {
                                loading = false
                            }
                        }
                    },
                    enabled = !loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Save Profile", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
