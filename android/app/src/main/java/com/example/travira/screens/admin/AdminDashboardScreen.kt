package com.example.travira.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.travira.auth.TokenManager
import com.example.travira.model.Place
import com.example.travira.model.User
import com.example.travira.remote.PlaceCounts
import com.example.travira.remote.RetrofitInstance
import com.example.travira.remote.StatusBody
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    tokenManager: TokenManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var tab by remember { mutableIntStateOf(0) }
    val isSuper = tokenManager.isSuperAdmin
    val tabs = if (isSuper) {
        listOf("Places", "Users", "Approvals", "Admins")
    } else {
        listOf("Places", "Users", "Approvals")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0D47A1),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F7FA))
        ) {
            TabRow(
                selectedTabIndex = tab,
                containerColor = Color.White
            ) {
                tabs.forEachIndexed { i, title ->
                    Tab(
                        selected = tab == i,
                        onClick = { tab = i },
                        text = { Text(title) },
                        icon = {
                            Icon(
                                when (i) {
                                    0 -> Icons.Default.Place
                                    1 -> Icons.Default.People
                                    2 -> Icons.Default.Check
                                    else -> Icons.Default.AdminPanelSettings
                                },
                                contentDescription = null
                            )
                        }
                    )
                }
            }

            when (tabs.getOrNull(tab)) {
                "Places" -> AdminPlacesTab(tokenManager)
                "Users" -> AdminUsersTab(tokenManager)
                "Approvals" -> AdminApprovalsTab(tokenManager)
                "Admins" -> AdminAdminsTab(tokenManager)
            }
        }
    }
}

@Composable
private fun AdminPlacesTab(tokenManager: TokenManager) {
    val scope = rememberCoroutineScope()
    var places by remember { mutableStateOf<List<Place>>(emptyList()) }
    var counts by remember { mutableStateOf<PlaceCounts?>(null) }
    var filter by remember { mutableStateOf("all") }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var selected by remember { mutableStateOf<Place?>(null) }

    fun load() {
        scope.launch {
            loading = true
            error = null
            try {
                val token = tokenManager.accessToken ?: return@launch
                val res = RetrofitInstance.adminApi.getAllPlaces("Bearer $token", filter)
                places = res.places
                counts = res.counts
            } catch (e: Exception) {
                error = e.message
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(filter) { load() }

    if (selected != null) {
        AdminPlaceDetail(
            tokenManager = tokenManager,
            place = selected!!,
            onBack = { selected = null; load() }
        )
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        counts?.let { c ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CountChip("All ${c.total}", filter == "all") { filter = "all" }
                CountChip("Pending ${c.pending}", filter == "pending") { filter = "pending" }
                CountChip("Approved ${c.approved}", filter == "approved") { filter = "approved" }
                CountChip("Rejected ${c.rejected}", filter == "rejected") { filter = "rejected" }
            }
        }

        when {
            loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            error != null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(error ?: "", color = Color.Red)
            }
            else -> LazyColumn(
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(places, key = { it._id }) { place ->
                    AdminPlaceCard(place) { selected = place }
                }
            }
        }
    }
}

@Composable
private fun CountChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontSize = 12.sp) }
    )
}

@Composable
private fun AdminPlaceCard(place: Place, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = place.imageUrl,
                contentDescription = place.name,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                Text(place.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    listOfNotNull(place.city, place.state).joinToString(", "),
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                StatusBadge(place.approvalStatus ?: "—")
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val color = when (status) {
        "approved" -> Color(0xFF2E7D32)
        "rejected" -> Color(0xFFC62828)
        "pending" -> Color(0xFFF9A825)
        else -> Color.Gray
    }
    Text(
        text = status.uppercase(),
        color = color,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun AdminPlaceDetail(
    tokenManager: TokenManager,
    place: Place,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var feedback by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }

    fun act(status: String) {
        scope.launch {
            busy = true
            message = null
            try {
                val token = tokenManager.accessToken ?: return@launch
                RetrofitInstance.adminApi.setPlaceStatus(
                    "Bearer $token",
                    place._id,
                    StatusBody(status = status, feedback = feedback.ifBlank { null }, message = feedback.ifBlank { null })
                )
                message = "Status set to $status. User notified."
            } catch (e: Exception) {
                message = e.message
            } finally {
                busy = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        AsyncImage(
            model = place.imageUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(place.name, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(listOfNotNull(place.city, place.state, place.country).joinToString(", "), color = Color.Gray)
        StatusBadge(place.approvalStatus ?: "—")
        Spacer(modifier = Modifier.height(8.dp))
        Text(place.shortDescription ?: place.description ?: "", fontSize = 14.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text("Rating: ${place.displayRating}  •  Visitors: ${place.visitorsCount}", fontSize = 13.sp)

        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = feedback,
            onValueChange = { feedback = it },
            label = { Text("Feedback (for user notification)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { act("approved") },
                enabled = !busy,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) { Text("Approve") }
            Button(
                onClick = { act("rejected") },
                enabled = !busy,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
            ) { Text("Reject") }
            Button(
                onClick = { act("pending") },
                enabled = !busy,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9A825))
            ) { Text("Pending") }
        }
        message?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = Color(0xFF1565C0))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                scope.launch {
                    try {
                        val token = tokenManager.accessToken ?: return@launch
                        RetrofitInstance.adminApi.deletePlace("Bearer $token", place._id)
                        onBack()
                    } catch (e: Exception) {
                        message = e.message
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C))
        ) {
            Text("Delete place")
        }
    }
}

@Composable
private fun AdminUsersTab(tokenManager: TokenManager) {
    val scope = rememberCoroutineScope()
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var selected by remember { mutableStateOf<User?>(null) }
    var detailNote by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val token = tokenManager.accessToken ?: return@LaunchedEffect
            val res = RetrofitInstance.adminApi.getUsers("Bearer $token")
            users = res.users
        } catch (_: Exception) {
        } finally {
            loading = false
        }
    }

    if (selected != null) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
            IconButton(onClick = { selected = null }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
            }
            Text(selected!!.name, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(selected!!.email, color = Color.Gray)
            Text("Role: ${selected!!.role}", fontSize = 14.sp)
            detailNote?.let { Text(it, fontSize = 12.sp, color = Color.Gray) }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Wishlist: ${selected!!.wishlist.size}", fontWeight = FontWeight.Medium)
            selected!!.wishlist.forEach {
                Text("• ${it.name}", fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Contributions: ${selected!!.addedPlaces.size}", fontWeight = FontWeight.Medium)
            selected!!.addedPlaces.forEach {
                Text("• ${it.name} (${it.approvalStatus ?: "—"})", fontSize = 13.sp)
            }
        }
        return
    }

    if (loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    } else {
        LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(users, key = { it.userId }) { user ->
                Card(
                    onClick = {
                        selected = user
                        scope.launch {
                            try {
                                val token = tokenManager.accessToken ?: return@launch
                                val res = RetrofitInstance.adminApi.getUserDetail("Bearer $token", user.userId)
                                selected = res.user ?: user
                                detailNote = res.passwordNote
                            } catch (_: Exception) { }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(user.name, fontWeight = FontWeight.Bold)
                        Text(user.email, fontSize = 13.sp, color = Color.Gray)
                        Text("Role: ${user.role}", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminApprovalsTab(tokenManager: TokenManager) {
    // Same as Places with pending filter
    var filter by remember { mutableStateOf("pending") }
    // Reuse places tab logic via embedding filter default
    val scope = rememberCoroutineScope()
    var places by remember { mutableStateOf<List<Place>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    fun load() {
        scope.launch {
            loading = true
            try {
                val token = tokenManager.accessToken ?: return@launch
                val res = RetrofitInstance.adminApi.getAllPlaces("Bearer $token", filter)
                places = res.places
            } catch (_: Exception) {
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(filter) { load() }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CountChip("Pending", filter == "pending") { filter = "pending" }
            CountChip("Rejected", filter == "rejected") { filter = "rejected" }
            CountChip("Approved", filter == "approved") { filter = "approved" }
        }
        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(places, key = { it._id }) { place ->
                    AdminApprovalCard(tokenManager, place) { load() }
                }
            }
        }
    }
}

@Composable
private fun AdminApprovalCard(tokenManager: TokenManager, place: Place, onDone: () -> Unit) {
    val scope = rememberCoroutineScope()
    var feedback by remember { mutableStateOf("") }

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(place.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(listOfNotNull(place.city, place.state).joinToString(", "), color = Color.Gray, fontSize = 13.sp)
            StatusBadge(place.approvalStatus ?: "pending")
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = feedback,
                onValueChange = { feedback = it },
                label = { Text("Feedback") },
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                val token = tokenManager.accessToken ?: return@launch
                                RetrofitInstance.adminApi.approvePlace(
                                    "Bearer $token",
                                    place._id,
                                    mapOf("message" to (feedback.ifBlank { "Approved" }))
                                )
                                onDone()
                            } catch (_: Exception) { }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                    Text(" Approve")
                }
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                val token = tokenManager.accessToken ?: return@launch
                                RetrofitInstance.adminApi.rejectPlace(
                                    "Bearer $token",
                                    place._id,
                                    mapOf("feedback" to (feedback.ifBlank { "Rejected by admin" }))
                                )
                                onDone()
                            } catch (_: Exception) { }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                ) {
                    Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                    Text(" Reject")
                }
            }
        }
    }
}

@Composable
private fun AdminAdminsTab(tokenManager: TokenManager) {
    val scope = rememberCoroutineScope()
    var admins by remember { mutableStateOf<List<User>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    fun load() {
        scope.launch {
            loading = true
            error = null
            try {
                val token = tokenManager.accessToken ?: return@launch
                val res = RetrofitInstance.adminApi.getAdmins("Bearer $token")
                admins = res.admins
            } catch (e: Exception) {
                error = e.message
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) { load() }

    when {
        loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        error != null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(error ?: "", color = Color.Red, modifier = Modifier.padding(16.dp))
        }
        else -> LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(admins, key = { it.userId }) { admin ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(admin.name, fontWeight = FontWeight.Bold)
                        Text(admin.email, fontSize = 13.sp, color = Color.Gray)
                        Text("Role: ${admin.role}  •  Status: ${admin.role}", fontSize = 12.sp)
                        if (admin.role != "superadmin") {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            try {
                                                val token = tokenManager.accessToken ?: return@launch
                                                RetrofitInstance.adminApi.setAdminStatus(
                                                    "Bearer $token",
                                                    admin.userId,
                                                    StatusBody("approved")
                                                )
                                                load()
                                            } catch (_: Exception) { }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                                ) { Text("Approve") }
                                Button(
                                    onClick = {
                                        scope.launch {
                                            try {
                                                val token = tokenManager.accessToken ?: return@launch
                                                RetrofitInstance.adminApi.setAdminStatus(
                                                    "Bearer $token",
                                                    admin.userId,
                                                    StatusBody("rejected")
                                                )
                                                load()
                                            } catch (_: Exception) { }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                                ) { Text("Reject") }
                                Button(
                                    onClick = {
                                        scope.launch {
                                            try {
                                                val token = tokenManager.accessToken ?: return@launch
                                                RetrofitInstance.adminApi.setAdminStatus(
                                                    "Bearer $token",
                                                    admin.userId,
                                                    StatusBody("pending")
                                                )
                                                load()
                                            } catch (_: Exception) { }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9A825))
                                ) { Text("Pending") }
                            }
                        }
                    }
                }
            }
        }
    }
}