package com.example.taskconnectapp

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class TaskDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_details) // Will design later

        val title = intent.getStringExtra("title")
        val postedBy = intent.getStringExtra("postedBy")
        val budget = intent.getStringExtra("budget")

        findViewById<TextView>(R.id.taskTitleDetail).text = title
        findViewById<TextView>(R.id.taskPostedByDetail).text = "Posted by: $postedBy"
        findViewById<TextView>(R.id.taskBudgetDetail).text = budget
    }
}
