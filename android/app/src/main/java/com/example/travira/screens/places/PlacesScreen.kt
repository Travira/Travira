package com.example.travira.screens.places

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.travira.data.Place

@Composable
fun PlaceScreen(
    place: Place,
    onBackClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {

        // ─────────────────────────────
        // IMAGE
        // ─────────────────────────────
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

            // Back button
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .padding(16.dp)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        Color.White.copy(alpha = 0.92f)
                    )
                    .align(Alignment.TopStart)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }
        }

        // ─────────────────────────────
        // DETAILS
        // ─────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {

            // Place name
            Text(
                text = place.name,
                fontSize = 30.sp,
                color = Color.Black
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            // Location
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFF1976D2),
                    modifier = Modifier.size(20.dp)
                )

                Spacer(
                    modifier = Modifier.size(6.dp)
                )

                Text(
                    text = "${place.city}, ${place.state}, ${place.country}",
                    fontSize = 15.sp,
                    color = Color.Gray
                )
            }

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            // Rating
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFB300),
                    modifier = Modifier.size(22.dp)
                )

                Spacer(
                    modifier = Modifier.size(5.dp)
                )

                Text(
                    text = place.rating.toString(),
                    fontSize = 16.sp,
                    color = Color.Black
                )
            }

            Spacer(
                modifier = Modifier.height(28.dp)
            )

            // About
            Text(
                text = "About this place",
                fontSize = 21.sp,
                color = Color.Black
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = place.shortDescription,
                fontSize = 15.sp,
                lineHeight = 24.sp,
                color = Color.DarkGray
            )

            Spacer(
                modifier = Modifier.height(28.dp)
            )

            // Location section
            Text(
                text = "Location",
                fontSize = 21.sp,
                color = Color.Black
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF5F7FA)
                )
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

                    Spacer(
                        modifier = Modifier.size(10.dp)
                    )

                    Text(
                        text = place.location,
                        fontSize = 15.sp,
                        color = Color.DarkGray
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            // Open Maps
            Button(
                onClick = {
                    // Maps logic can be added here
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1976D2)
                )
            ) {

                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = null
                )

                Spacer(
                    modifier = Modifier.size(8.dp)
                )

                Text(
                    text = "Open in Maps",
                    fontSize = 16.sp
                )
            }

            Spacer(
                modifier = Modifier.height(20.dp)
            )
        }
    }
}