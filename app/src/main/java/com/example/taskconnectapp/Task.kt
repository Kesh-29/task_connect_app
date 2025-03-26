package com.example.taskconnectapp

data class Task(
    val taskId: String,  // ✅ Ensure UUID is stored as a String
    val title: String,
    val postedBy: String,
    val budget: String,
    val acceptedBy: Int? = null
)

