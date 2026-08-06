package com.example.travira.screens.profile

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.travira.R
import com.example.travira.model.User

enum class ProfileSection {
    EDIT_PROFILE, WISHLIST, CONTRIBUTION, VISITED, NOTIFICATIONS
}

@Composable
fun ProfileScreen(
    isLoggedIn: Boolean,
    user: User?,
    onLoginClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onSectionClick: (ProfileSection) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val displayName = if (isLoggedIn) (user?.name?.ifBlank { "Traveler" } ?: "Traveler") else "Guest"
    val bioFromUser = user?.bio
    val bio = when {
        !isLoggedIn -> "Browsing as guest. Login to save wishlist, add places & use AI."
        !bioFromUser.isNullOrBlank() -> bioFromUser
        else -> "Explore smarter. Discover deeper. Travel with confidence."
    }
    val email = if (isLoggedIn) (user?.email ?: "") else "guest@travira.app"
    val wishlistCount = user?.wishlist?.size ?: 0
    val contributionCount = user?.addedPlaces?.size ?: 0
    val visitedCount = user?.visitedPlaces?.size ?: 0
    val notificationCount = user?.notifications?.count { !it.read } ?: 0

    val scroll = rememberScrollState()
    val context = LocalContext.current

    fun requireLoginThen(section: ProfileSection) {
        if (!isLoggedIn) onLoginClick() else onSectionClick(section)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F7))
            .verticalScroll(scroll)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        ) {
            if (!user?.coverImage.isNullOrBlank()) {
                AsyncImage(
                    model = user?.coverImage,
                    contentDescription = "Cover",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.taj),
                    contentDescription = "Cover",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f))
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-8).dp)
            ) {
                if (!user?.profileImage.isNullOrBlank()) {
                    AsyncImage(
                        model = user?.profileImage,
                        contentDescription = "Profile photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(3.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "Profile photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(3.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE3F2FD))
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-4).dp)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = displayName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = bio,
                fontSize = 13.sp,
                color = Color(0xFF6B6B6B),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
            if (!isLoggedIn) {
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onLoginClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Login / Sign up")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        ProfileMenuCard {
            ProfileMenuItem(
                icon = Icons.Default.Person,
                title = "Profile",
                subtitle = email,
                onClick = { requireLoginThen(ProfileSection.EDIT_PROFILE) }
            )
            MenuDivider()
            ProfileMenuItem(
                icon = Icons.Default.Favorite,
                title = "Wishlist",
                trailing = if (wishlistCount > 0) wishlistCount.toString() else null,
                onClick = { requireLoginThen(ProfileSection.WISHLIST) }
            )
            MenuDivider()
            ProfileMenuItem(
                icon = Icons.Default.Place,
                title = "Contribution",
                subtitle = "Places you added",
                trailing = if (contributionCount > 0) contributionCount.toString() else null,
                onClick = { requireLoginThen(ProfileSection.CONTRIBUTION) }
            )
            MenuDivider()
            ProfileMenuItem(
                icon = Icons.Default.TravelExplore,
                title = "Visited Places",
                trailing = if (visitedCount > 0) visitedCount.toString() else null,
                onClick = { requireLoginThen(ProfileSection.VISITED) }
            )
            MenuDivider()
            ProfileMenuItem(
                icon = Icons.Default.Notifications,
                title = "Notifications",
                subtitle = "Admin feedback & place updates",
                trailing = if (notificationCount > 0) notificationCount.toString() else null,
                onClick = { requireLoginThen(ProfileSection.NOTIFICATIONS) }
            )
            if (isLoggedIn) {
                MenuDivider()
                ProfileMenuItem(
                    icon = Icons.Default.Logout,
                    title = "Logout",
                    onClick = onLogoutClick
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        ProfileMenuCard {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Travira",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1565C0)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "© 2026 Travira. All rights reserved.",
                    fontSize = 12.sp,
                    color = Color(0xFF9E9E9E)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Explore smarter. Discover deeper.",
                    fontSize = 12.sp,
                    color = Color(0xFF757575)
                )
                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SocialIcon("IG", Color(0xFFE1306C)) { openUrl(context, "https://instagram.com/") }
                    SocialIcon("Pin", Color(0xFFE60023)) { openUrl(context, "https://pinterest.com/") }
                    SocialIcon("WA", Color(0xFF25D366)) { openUrl(context, "https://wa.me/") }
                    SocialIcon("Git", Color(0xFF333333)) { openUrl(context, "https://github.com/") }
                    SocialIcon("Call", Color(0xFF1565C0)) { openUrl(context, "tel:") }
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun ProfileMenuCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) { content() }
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    trailing: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFEEF5FF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, title, tint = Color(0xFF1565C0), modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF212121))
            if (subtitle != null) {
                Text(subtitle, fontSize = 12.sp, color = Color(0xFF9E9E9E))
            }
        }
        if (trailing != null) {
            Box(
                modifier = Modifier
                    .background(Color(0xFF1565C0), RoundedCornerShape(10.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(trailing, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(6.dp))
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            null,
            tint = Color(0xFFBDBDBD),
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun MenuDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 0.6.dp,
        color = Color(0xFFEEEEEE)
    )
}

@Composable
private fun SocialIcon(label: String, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.12f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

private fun openUrl(context: android.content.Context, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (_: Exception) { }
}
