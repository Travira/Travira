package com.example.travira.model


data class Place(
    val _id: String,
    val id: Int,
    val name: String,
    val shortDescription: String,
    val city: String,
    val state: String,
    val country: String,
    val location: String,
    val imageUrl: String,
    val rating: Double
)