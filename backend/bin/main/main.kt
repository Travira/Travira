package com.example

import com.example.database.MongoDatabase
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>) {
    MongoDatabase.testConnection()
    EngineMain.main(args)
}