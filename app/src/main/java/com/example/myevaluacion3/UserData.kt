package com.example.myevaluacion3

import java.util.concurrent.ConcurrentHashMap

// Simple in-memory data store for users
object UserData {
    private val users = ConcurrentHashMap<String, String>()

    // Function to add a new user
    fun registerUser(username: String, password: String): Boolean {
        if (users.containsKey(username)) {
            return false // User already exists
        }
        users[username] = password
        return true
    }

    // Function to check user credentials
    fun loginUser(username: String, password: String): Boolean {
        return users[username] == password
    }
}