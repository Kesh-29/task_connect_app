package com.example.taskconnectapp

data class Task(
    val taskId: String,  // ✅ Ensure UUID is stored as a String
    val title: String,
    val postedBy: String,
    val budget: String = "Open",
    val status: String = "Open",
    val acceptedBy: Int? = null,
    val createdAt: String = "Open",
    val completedAt: String = "Open"
)