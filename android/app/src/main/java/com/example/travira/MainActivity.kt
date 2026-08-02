package com.example.travira

import android.os.Bundle
import android.view.View
import androidx.core.view.WindowCompat
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.travira.components.TraviraBottomBar
import com.example.travira.model.Place
import com.example.travira.remote.RetrofitInstance
import com.example.travira.screens.home.HomeScreen
import com.example.travira.screens.places.PlaceScreen
import com.example.travira.ui.theme.TraviraTheme


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

                TraviraApp()

            }

        }

    }

}



@Composable
fun TraviraApp() {


    var selectedIndex by remember {

        mutableStateOf(0)

    }



    var selectedPlace by remember {

        mutableStateOf<Place?>(null)

    }



    var placesList by remember {

        mutableStateOf<List<Place>>(emptyList())

    }




    LaunchedEffect(Unit) {


        try {


            val response =
                RetrofitInstance.api.getPlaces()


            placesList = response


            Log.d(
                "TRAVIRA_API",
                "TOTAL PLACES: ${response.size}"
            )


        } catch (e: Exception) {


            Log.e(
                "TRAVIRA_API",
                e.message ?: "API ERROR"
            )


        }


    }




    if (selectedPlace != null) {


        BackHandler {

            selectedPlace = null

        }



        PlaceScreen(

            place = selectedPlace!!,

            onBackClick = {

                selectedPlace = null

            }

        )



    } else {



        Box(

            modifier = Modifier
                .fillMaxSize()

        ) {



            when(selectedIndex) {



                0 -> {


                    HomeScreen(

                        places = placesList,


                        modifier = Modifier
                            .fillMaxSize(),


                        onPlaceClick = {

                            selectedPlace = it

                        }

                    )


                }



                1 -> {


                    // AI Screen


                }




                2 -> {


                    // About Screen


                }



            }




            // Floating bottom pill

            TraviraBottomBar(

                selectedIndex = selectedIndex,


                onItemSelected = {

                    selectedIndex = it

                },


                modifier = Modifier
                    .align(
                        Alignment.BottomCenter
                    )


            )



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