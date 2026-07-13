package com.example.travira.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

data class BottomBarItem(
    val title: String,
    val icon: ImageVector
)

@Composable
fun TraviraBottomBar(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit
) {

    val items = listOf(
        BottomBarItem("Home", Icons.Default.Home),
        BottomBarItem("AI", Icons.Default.SmartToy),
        BottomBarItem("About", Icons.Default.Info)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {

        Surface(
            modifier = Modifier.shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(50.dp)
            ),
            shape = RoundedCornerShape(50.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp
        ) {

            Row(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 10.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {

                items.forEachIndexed { index, item ->

                    val selected = selectedIndex == index

                    val iconColor by animateColorAsState(
                        if (selected)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            Color.Gray,
                        label = ""
                    )

                    val backgroundColor by animateColorAsState(
                        if (selected)
                            MaterialTheme.colorScheme.primary
                        else
                            Color.Transparent,
                        label = ""
                    )

                    val iconSize by animateDpAsState(
                        if (selected) 28.dp else 24.dp,
                        label = ""
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            onItemSelected(index)
                        }
                    ) {

                        Box(
                            modifier = Modifier
                                .background(
                                    backgroundColor,
                                    CircleShape
                                )
                                .padding(7.dp),
                            contentAlignment = Alignment.Center
                        ) {

                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                modifier = Modifier.size(iconSize),
                                tint = iconColor
                            )

                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selected)
                                MaterialTheme.colorScheme.primary
                            else
                                Color.Gray,
                            fontWeight = if (selected)
                                FontWeight.Bold
                            else
                                FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TraviraBottomBarPreview() {

    MaterialTheme {

        TraviraBottomBar(
            selectedIndex = 0,
            onItemSelected = {}
        )

    }
}