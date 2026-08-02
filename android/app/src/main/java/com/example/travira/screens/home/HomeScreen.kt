package com.example.travira.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.travira.components.AppCard
import com.example.travira.model.Place


@Composable
fun HomeScreen(
    places: List<Place>,
    onPlaceClick: (Place) -> Unit,
    modifier: Modifier = Modifier
) {


    if (places.isEmpty()) {


        androidx.compose.foundation.layout.Column(

            modifier = modifier
                .fillMaxSize(),

            horizontalAlignment = Alignment.CenterHorizontally,

            verticalArrangement = Arrangement.Center

        ) {


            CircularProgressIndicator()


            Spacer(
                modifier = Modifier.height(16.dp)
            )


            Text(
                text = "Loading places..."
            )


        }


    } else {



        LazyColumn(

            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),

            verticalArrangement = Arrangement.spacedBy(16.dp)

        ) {



            item {

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

            }



            items(

                items = places

            ) { place ->



                AppCard(

                    place = place,

                    onClick = {

                        onPlaceClick(place)

                    }

                )

            }



            item {

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

            }


        }


    }

}