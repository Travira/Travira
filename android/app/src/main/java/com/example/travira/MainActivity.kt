package com.example.travira

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import com.example.travira.auth.TokenManager
import com.example.travira.components.TraviraBottomBar
import com.example.travira.model.Place
import com.example.travira.model.User
import com.example.travira.remote.RefreshRequest
import com.example.travira.remote.RetrofitInstance
import com.example.travira.screens.admin.AdminDashboardScreen
import com.example.travira.screens.auth.LoginScreen
import com.example.travira.screens.home.HomeScreen
import com.example.travira.screens.places.AddPlaceScreen
import com.example.travira.screens.places.EditPlaceScreen
import com.example.travira.screens.places.PlaceScreen
import com.example.travira.screens.profile.ContributionScreen
import com.example.travira.screens.profile.EditProfileScreen
import com.example.travira.screens.profile.NotificationsScreen
import com.example.travira.screens.profile.ProfileScreen
import com.example.travira.screens.profile.ProfileSection
import com.example.travira.screens.profile.VisitedPlacesScreen
import com.example.travira.screens.profile.WishlistScreen
import com.example.travira.screens.splash.SplashScreen
import com.example.travira.ui.theme.TraviraTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        window.decorView.systemUiVisibility =
            (
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                    )

        setContent {
            TraviraTheme {
                TraviraRoot()
            }
        }
    }
}

/** What the user was trying to do before being sent to login */
enum class PendingAction {
    NONE, ADD_PLACE, AI_CHAT, WISHLIST
}

@Composable
fun TraviraRoot() {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }

    var showSplash by remember { mutableStateOf(true) }
    var prefetchedPlaces by remember { mutableStateOf<List<Place>>(emptyList()) }
    var prefetchError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val response = RetrofitInstance.placeApi.getPlaces()
            prefetchedPlaces = response.data
            Log.d("TRAVIRA_API", "PREFETCH PLACES: ${response.data.size}")
        } catch (e: Exception) {
            Log.e("TRAVIRA_API", "Prefetch error: ${e.message}")
            prefetchError = e.message
        }
    }

    if (showSplash) {
        SplashScreen(onFinish = { showSplash = false })
    } else {
        TraviraApp(
            tokenManager = tokenManager,
            initialPlaces = prefetchedPlaces,
            initialError = prefetchError
        )
    }
}

@Composable
fun TraviraApp(
    tokenManager: TokenManager,
    initialPlaces: List<Place> = emptyList(),
    initialError: String? = null
) {
    val scope = rememberCoroutineScope()

    var selectedIndex by remember { mutableIntStateOf(0) }
    var selectedPlace by remember { mutableStateOf<Place?>(null) }
    var placesList by remember { mutableStateOf(initialPlaces) }
    var isLoading by remember { mutableStateOf(initialPlaces.isEmpty()) }
    var errorMessage by remember { mutableStateOf(initialError) }
    var refreshTrigger by remember { mutableIntStateOf(0) }

    var isLoggedIn by remember { mutableStateOf(tokenManager.isLoggedIn) }
    var currentUser by remember { mutableStateOf<User?>(null) }

    var showLogin by remember { mutableStateOf(false) }
    var showAdmin by remember { mutableStateOf(false) }
    var showAddPlace by remember { mutableStateOf(false) }
    var editingPlace by remember { mutableStateOf<Place?>(null) }
    var pendingAction by remember { mutableStateOf(PendingAction.NONE) }
    var profileSection by remember { mutableStateOf<ProfileSection?>(null) }

    fun refreshUser() {
        scope.launch {
            if (!tokenManager.isLoggedIn) {
                currentUser = null
                return@launch
            }
            val token = tokenManager.accessToken
            if (token.isNullOrBlank()) return@launch
            try {
                val res = RetrofitInstance.authApi.getCurrentUser("Bearer $token")
                currentUser = res.user
            } catch (e: Exception) {
                val rt = tokenManager.refreshToken
                if (!rt.isNullOrBlank()) {
                    try {
                        val refreshed = RetrofitInstance.authApi.refreshToken(RefreshRequest(rt))
                        tokenManager.accessToken = refreshed.accessToken
                        val res = RetrofitInstance.authApi.getCurrentUser("Bearer ${refreshed.accessToken}")
                        currentUser = res.user
                    } catch (_: Exception) {
                        tokenManager.clear()
                        isLoggedIn = false
                        currentUser = null
                    }
                }
            }
        }
    }

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) refreshUser() else currentUser = null
    }

    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger == 0 && placesList.isNotEmpty()) {
            isLoading = false
            return@LaunchedEffect
        }
        isLoading = true
        errorMessage = null
        try {
            val response = RetrofitInstance.placeApi.getPlaces()
            placesList = response.data
            Log.d("TRAVIRA_API", "TOTAL PLACES: ${response.data.size}")
        } catch (e: Exception) {
            Log.e("TRAVIRA_API", e.message ?: "API ERROR")
            errorMessage = e.message ?: "Failed to load places"
        } finally {
            isLoading = false
        }
    }

    fun requireAuth(action: PendingAction) {
        if (tokenManager.isLoggedIn) {
            when (action) {
                PendingAction.ADD_PLACE -> showAddPlace = true
                PendingAction.AI_CHAT -> selectedIndex = 1
                else -> {}
            }
        } else {
            pendingAction = action
            showLogin = true
        }
    }

    fun onLoginSuccess() {
        isLoggedIn = true
        showLogin = false
        // Admins stay on the main app; they open dashboard via bottom Admin tab
        when (pendingAction) {
            PendingAction.ADD_PLACE -> showAddPlace = true
            PendingAction.AI_CHAT -> selectedIndex = 1
            else -> {}
        }
        pendingAction = PendingAction.NONE
        refreshUser()
    }

    val wishlistIds = remember(currentUser) {
        currentUser?.wishlist?.map { it._id }?.toSet() ?: emptySet()
    }
    val visitedIds = remember(currentUser) {
        currentUser?.visitedPlaces?.mapNotNull { it.place?._id }?.toSet() ?: emptySet()
    }

    when {
        showAdmin -> {
            BackHandler { showAdmin = false }
            AdminDashboardScreen(
                tokenManager = tokenManager,
                onBack = { showAdmin = false }
            )
        }

        showLogin -> {
            BackHandler {
                showLogin = false
                pendingAction = PendingAction.NONE
            }
            LoginScreen(
                tokenManager = tokenManager,
                onLoginSuccess = { onLoginSuccess() },
                onBack = {
                    showLogin = false
                    pendingAction = PendingAction.NONE
                }
            )
        }

        showAddPlace -> {
            BackHandler { showAddPlace = false }
            AddPlaceScreen(
                tokenManager = tokenManager,
                onBack = { showAddPlace = false },
                onSubmitted = {
                    showAddPlace = false
                    selectedIndex = 0
                    refreshTrigger++
                    refreshUser()
                }
            )
        }

        editingPlace != null -> {
            val placeBeingEdited = editingPlace!!
            val ownerId = placeBeingEdited.addedById
            val isOwner =
                !ownerId.isNullOrBlank() && ownerId == (currentUser?.userId ?: tokenManager.userId)
            val isAdminEdit = tokenManager.isAdmin && !isOwner
            BackHandler { editingPlace = null }
            EditPlaceScreen(
                place = placeBeingEdited,
                tokenManager = tokenManager,
                isAdminEdit = isAdminEdit,
                onBack = { editingPlace = null },
                onSaved = { updated ->
                    editingPlace = null
                    selectedPlace = updated
                    refreshTrigger++
                    refreshUser()
                }
            )
        }

        selectedPlace != null -> {
            BackHandler { selectedPlace = null }
            PlaceScreen(
                place = selectedPlace!!,
                onBackClick = { selectedPlace = null },
                tokenManager = tokenManager,
                isWishlistedInitially = selectedPlace!!._id in wishlistIds,
                isVisitedInitially = selectedPlace!!._id in visitedIds,
                onRequireLogin = {
                    pendingAction = PendingAction.NONE
                    showLogin = true
                },
                onEditClick = { place ->
                    editingPlace = place
                },
                onDeleted = {
                    selectedPlace = null
                    refreshTrigger++
                    refreshUser()
                },
                currentUserId = currentUser?.userId ?: tokenManager.userId
            )
        }

        profileSection == ProfileSection.EDIT_PROFILE -> {
            BackHandler { profileSection = null }
            EditProfileScreen(
                tokenManager = tokenManager,
                user = currentUser,
                onBack = { profileSection = null },
                onSaved = { updated ->
                    currentUser = updated
                    profileSection = null
                    selectedIndex = 2
                    refreshUser()
                }
            )
        }

        profileSection == ProfileSection.WISHLIST -> {
            BackHandler { profileSection = null }
            WishlistScreen(
                tokenManager = tokenManager,
                onBack = {
                    profileSection = null
                    refreshUser()
                },
                onPlaceClick = { selectedPlace = it }
            )
        }

        profileSection == ProfileSection.CONTRIBUTION -> {
            BackHandler { profileSection = null }
            ContributionScreen(
                tokenManager = tokenManager,
                onBack = { profileSection = null },
                onPlaceClick = { selectedPlace = it }
            )
        }

        profileSection == ProfileSection.VISITED -> {
            BackHandler { profileSection = null }
            VisitedPlacesScreen(
                tokenManager = tokenManager,
                onBack = {
                    profileSection = null
                    refreshUser()
                },
                onPlaceClick = { selectedPlace = it }
            )
        }

        profileSection == ProfileSection.NOTIFICATIONS -> {
            BackHandler { profileSection = null }
            NotificationsScreen(
                tokenManager = tokenManager,
                onBack = {
                    profileSection = null
                    refreshUser()
                }
            )
        }

        else -> {
            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedIndex) {
                    0 -> {
                        HomeScreen(
                            places = placesList,
                            isLoading = isLoading,
                            errorMessage = errorMessage,
                            onPlaceClick = { selectedPlace = it },
                            onRetry = { refreshTrigger++ },
                            onAddPlaceClick = { requireAuth(PendingAction.ADD_PLACE) },
                            userName = currentUser?.name
                                ?: tokenManager.userName
                                ?: if (isLoggedIn) "Traveler" else "Guest",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    1 -> {
                        LaunchedEffect(Unit) {
                            if (!tokenManager.isLoggedIn) {
                                requireAuth(PendingAction.AI_CHAT)
                                selectedIndex = 0
                            }
                        }
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.material3.Text("AI Chatbot coming soon")
                        }
                    }
                    2 -> {
                        ProfileScreen(
                            isLoggedIn = isLoggedIn,
                            user = currentUser ?: if (isLoggedIn) User(
                                name = tokenManager.userName ?: "Traveler",
                                email = tokenManager.userEmail ?: ""
                            ) else null,
                            onLoginClick = {
                                pendingAction = PendingAction.NONE
                                showLogin = true
                            },
                            onLogoutClick = {
                                scope.launch {
                                    try {
                                        val token = tokenManager.accessToken
                                        val rt = tokenManager.refreshToken
                                        if (!token.isNullOrBlank() && !rt.isNullOrBlank()) {
                                            RetrofitInstance.authApi.logout(
                                                "Bearer $token",
                                                RefreshRequest(rt)
                                            )
                                        }
                                    } catch (_: Exception) { }
                                    tokenManager.clear()
                                    isLoggedIn = false
                                    currentUser = null
                                }
                            },
                            onSectionClick = { section -> profileSection = section },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                TraviraBottomBar(
                    selectedIndex = selectedIndex.coerceIn(0, if (tokenManager.isAdmin) 3 else 2),
                    showAdminTab = tokenManager.isAdmin,
                    onItemSelected = { index ->
                        if (index == 0 && selectedIndex == 0) {
                            refreshTrigger++
                        }
                        if (index == 1 && !tokenManager.isLoggedIn) {
                            requireAuth(PendingAction.AI_CHAT)
                            return@TraviraBottomBar
                        }
                        if (index == 3 && tokenManager.isAdmin) {
                            showAdmin = true
                            return@TraviraBottomBar
                        }
                        selectedIndex = index
                    },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TraviraAppPreview() {
    TraviraTheme { }
}
