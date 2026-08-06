package com.example.travira.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travira.components.AppCard
import com.example.travira.model.Place

@Composable
fun HomeScreen(
    places: List<Place>,
    isLoading: Boolean,
    errorMessage: String? = null,
    onPlaceClick: (Place) -> Unit,
    onRetry: () -> Unit = {},
    onAddPlaceClick: () -> Unit = {},
    wishlistIds: Set<String> = emptySet(),
    onToggleWishlist: ((Place) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {

        when {
            isLoading && places.isEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Loading places...")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "If server is starting, this may take a moment.\nTap Home again to retry.",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }

            !isLoading && places.isEmpty() && errorMessage != null -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "Could not load places", fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        fontSize = 13.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(onClick = onRetry) { Text("Retry") }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Or tap the Home icon again", fontSize = 12.sp, color = Color.Gray)
                }
            }

            !isLoading && places.isEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "No places found")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onRetry) { Text("Refresh") }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    items(
                        items = places,
                        key = { it._id.ifBlank { it.name } }
                    ) { place ->
                        AppCard(
                            place = place,
                            onClick = { onPlaceClick(place) },
                            showWishlistHeart = onToggleWishlist != null,
                            isWishlisted = place._id in wishlistIds,
                            onWishlistClick = { onToggleWishlist?.invoke(place) }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(100.dp)) }
                }

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 12.dp)
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = onAddPlaceClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 100.dp),
            containerColor = Color(0xFF1565C0),
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add place")
        }
    }
}
