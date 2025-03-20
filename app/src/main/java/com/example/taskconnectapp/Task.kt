package com.example.taskconnectapp

data class Task(
    val title: String,
    val postedBy: String,  // ✅ Use a single field for full name
    val budget: String
)

