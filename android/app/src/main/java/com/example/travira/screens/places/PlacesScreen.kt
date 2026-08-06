package com.example.travira.screens.places

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.travira.auth.TokenManager
import com.example.travira.model.Place
import com.example.travira.remote.RetrofitInstance
import kotlinx.coroutines.launch

@Composable
fun PlaceScreen(
    place: Place,
    onBackClick: () -> Unit,
    tokenManager: TokenManager? = null,
    isWishlistedInitially: Boolean = false,
    isVisitedInitially: Boolean = false,
    onRequireLogin: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var isWishlisted by remember { mutableStateOf(isWishlistedInitially) }
    var isVisited by remember { mutableStateOf(isVisitedInitially) }
    var actionMsg by remember { mutableStateOf<String?>(null) }

    fun authOr(action: suspend (String) -> Unit) {
        val token = tokenManager?.accessToken
        if (token.isNullOrBlank() || tokenManager?.isLoggedIn != true) {
            onRequireLogin()
            return
        }
        scope.launch {
            try {
                action(token)
            } catch (e: Exception) {
                actionMsg = e.message
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
        ) {
            AsyncImage(
                model = place.imageUrl,
                contentDescription = place.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .padding(16.dp)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.92f))
                    .align(Alignment.TopStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }

            // Wishlist heart – top right
            IconButton(
                onClick = {
                    authOr { token ->
                        if (isWishlisted) {
                            RetrofitInstance.placeApi.removeWishlist("Bearer $token", place._id)
                            isWishlisted = false
                            actionMsg = "Removed from wishlist"
                        } else {
                            RetrofitInstance.placeApi.addWishlist("Bearer $token", place._id)
                            isWishlisted = true
                            actionMsg = "Added to wishlist"
                        }
                    }
                },
                modifier = Modifier
                    .padding(16.dp)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.92f))
                    .align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = if (isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Wishlist",
                    tint = if (isWishlisted) Color(0xFFE91E63) else Color.Black
                )
            }

            // Visited tick – below heart
            IconButton(
                onClick = {
                    authOr { token ->
                        if (isVisited) {
                            RetrofitInstance.authApi.removeVisitedPlace("Bearer $token", place._id)
                            isVisited = false
                            actionMsg = "Removed from visited"
                        } else {
                            RetrofitInstance.authApi.addVisitedPlace("Bearer $token", place._id)
                            isVisited = true
                            actionMsg = "Marked as visited"
                        }
                    }
                },
                modifier = Modifier
                    .padding(end = 16.dp, top = 72.dp)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.92f))
                    .align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = if (isVisited) Icons.Default.CheckCircle else Icons.Outlined.CheckCircle,
                    contentDescription = "Visited",
                    tint = if (isVisited) Color(0xFF2E7D32) else Color.Black
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(text = place.name, fontSize = 30.sp, color = Color.Black)

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFF1976D2),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text = listOfNotNull(place.city, place.state, place.country).joinToString(", "),
                    fontSize = 15.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFB300),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.size(5.dp))
                Text(
                    text = place.displayRating.toString(),
                    fontSize = 16.sp,
                    color = Color.Black
                )
            }

            if (actionMsg != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(actionMsg!!, fontSize = 13.sp, color = Color(0xFF1565C0))
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(text = "About this place", fontSize = 21.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = place.shortDescription ?: place.description ?: "",
                fontSize = 15.sp,
                lineHeight = 24.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(text = "Location", fontSize = 21.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F7FA))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFF1976D2),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(
                        text = place.location ?: listOfNotNull(place.city, place.state, place.country).joinToString(", "),
                        fontSize = 15.sp,
                        color = Color.DarkGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { /* Maps integration later */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
            ) {
                Icon(imageVector = Icons.Default.Map, contentDescription = null)
                Spacer(modifier = Modifier.size(8.dp))
                Text(text = "Open in Maps", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
