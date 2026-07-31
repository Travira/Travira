package com.example.database

import com.mongodb.ConnectionString
import com.mongodb.kotlin.client.MongoClient
import com.mongodb.kotlin.client.MongoDatabase
import io.github.cdimascio.dotenv.dotenv

object MongoDatabase {

    private val dotenv = dotenv()

    private val uri = dotenv["MONGODB_URI"]
        ?.takeIf { it.isNotBlank() }
        ?: error("MONGODB_URI is not set in .env")

    private val client = MongoClient.create(
        ConnectionString(uri)
    )

    val database: MongoDatabase =
        client.getDatabase("travira")

    fun testConnection() {
        val collections = database.listCollectionNames().toList()

        println("=================================")
        println("MongoDB Atlas connected successfully!")
        println("Database: travira")
        println("Collections: $collections")
        println("=================================")
    }
}