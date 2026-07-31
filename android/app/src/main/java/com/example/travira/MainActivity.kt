package com.example.travira

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.travira.components.TraviraBottomBar
import com.example.travira.data.Place
import com.example.travira.data.places
import com.example.travira.screens.home.HomeScreen
import com.example.travira.screens.places.PlaceScreen
import com.example.travira.ui.theme.TraviraTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            TraviraTheme {
                TraviraApp()
            }
        }
    }
}

@Composable
fun TraviraApp() {

    // Bottom navigation selected item
    var selectedIndex by remember {
        mutableStateOf(0)
    }

    // Currently selected place
    var selectedPlace by remember {
        mutableStateOf<Place?>(null)
    }

    TraviraTheme {

        // ─────────────────────────────────
        // PLACE DETAIL SCREEN
        // ─────────────────────────────────
        if (selectedPlace != null) {

            PlaceScreen(
                place = selectedPlace!!,
                onBackClick = {
                    selectedPlace = null
                }
            )

        } else {

            // ─────────────────────────────────
            // MAIN APP SCREENS
            // ─────────────────────────────────
            Scaffold(
                bottomBar = {

                    TraviraBottomBar(
                        selectedIndex = selectedIndex,
                        onItemSelected = {
                            selectedIndex = it
                        }
                    )
                }
            ) { paddingValues ->

                when (selectedIndex) {

                    // HOME
                    0 -> {
                        HomeScreen(
                            places = places,
                            modifier = Modifier.padding(paddingValues),
                            onPlaceClick = { place ->
                                selectedPlace = place
                            }
                        )
                    }

                    // AI CHAT
                    1 -> {
                       print("Ai")
                    }

                    // ABOUT
                    2 -> {
                        print("about")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TraviraAppPreview() {
    TraviraTheme {
        TraviraApp()
    }
}