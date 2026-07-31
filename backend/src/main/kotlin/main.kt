package com.example

fun main(args: Array<String>) {
    try {
        val cls = Class.forName("io.ktor.server.netty.EngineMain")
        val method = cls.getMethod("main", Array<String>::class.java)
        method.invoke(null, args)
    } catch (e: Throwable) {
        System.err.println("Failed to start EngineMain via reflection: ${e.message}")
        e.printStackTrace()
    }
}
