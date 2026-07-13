package com.example.travira

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.travira.components.TraviraBottomBar
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

    var selectedIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),

        bottomBar = {
            TraviraBottomBar(
                selectedIndex = selectedIndex,
                onItemSelected = { selectedIndex = it }
            )
        }

    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {

            ScreenContent(selectedIndex)

        }

    }
}

@Composable
private fun ScreenContent(selectedIndex: Int) {

    when (selectedIndex) {

        0 -> Text("🏠 Home Screen")

        1 -> Text("🤖 AI Screen")

        2 -> Text("ℹ️ About Screen")

    }
}

@Preview(showBackground = true)
@Composable
fun TraviraAppPreview() {
    TraviraTheme {
        TraviraApp()
    }
}