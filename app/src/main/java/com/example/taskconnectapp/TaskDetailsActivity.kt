package com.example.taskconnectapp

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class TaskDetailsActivity : AppCompatActivity() {

    private lateinit var btnTaskDetails: Button
    private var taskId: String? = null
    private var taskOwnerId: Int = -1
    private var acceptedBy: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_details)

        val backButton = findViewById<ImageView>(R.id.backButton)
        btnTaskDetails = findViewById(R.id.btnTaskDetails)

        backButton.setOnClickListener { finish() }

        taskId = intent.getStringExtra("task_id")
        if (taskId == null) {
            Toast.makeText(this, "Invalid task ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        fetchTaskDetails(taskId!!)
    }

    private fun fetchTaskDetails(taskId: String) {
        val url = "http://10.0.2.2/taskconnect/get_task_details.php?task_id=$taskId"

        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(
                        this@TaskDetailsActivity,
                        "Failed to load task: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let { responseBody ->
                    val json = responseBody.string()

                    runOnUiThread {
                        try {
                            val jsonObject = JSONObject(json)

                            if (jsonObject.has("error")) {
                                Toast.makeText(
                                    this@TaskDetailsActivity,
                                    "Error: ${jsonObject.getString("error")}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@runOnUiThread
                            }

                            findViewById<TextView>(R.id.taskTitleDetail).text =
                                jsonObject.optString("title", "N/A")
                            findViewById<TextView>(R.id.userName).text =
                                jsonObject.optString("postedBy", "Unknown")
                            findViewById<TextView>(R.id.UserContactNumber).text =
                                jsonObject.optString("contact_number", "N/A")
                            findViewById<TextView>(R.id.taskBudgetDetail).text =
                                jsonObject.optString("budget", "N/A")

                            val location =
                                if (jsonObject.isNull("location")) "N/A" else jsonObject.getString("location")
                            findViewById<TextView>(R.id.UserLocation).text = location

                            findViewById<TextView>(R.id.JobDescription).text =
                                jsonObject.optString("description", "N/A")

                            // Get important values
                            taskOwnerId = jsonObject.optInt("users_id", -1)
                            acceptedBy = if (jsonObject.isNull("accepted_by")) null else jsonObject.getInt("accepted_by")

                            // Update button
                            updateTaskButton()

                        } catch (e: Exception) {
                            Toast.makeText(
                                this@TaskDetailsActivity,
                                "Error parsing task details",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        })
    }

    private fun updateTaskButton() {
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val currentUserId = sharedPreferences.getInt("users_id", -1)
        val isTasker = sharedPreferences.getInt("is_tasker", 0) == 1

        println("DEBUG: Logged-in User ID -> $currentUserId")
        println("DEBUG: Task Poster ID -> $taskOwnerId")
        println("DEBUG: Task Accepted By -> $acceptedBy")
        println("DEBUG: is_tasker -> $isTasker")

        when {
            // If the logged-in user is the poster (taskOwnerId), show "Cancel Post"
            currentUserId == taskOwnerId -> {
                btnTaskDetails.text = "Cancel Post"
                btnTaskDetails.setOnClickListener {
                    cancelPost()
                }
            }
            // If the user is not a tasker, show "Register"
            !isTasker -> {
                btnTaskDetails.text = "Register"
                btnTaskDetails.setOnClickListener {
                    navigateToVerification()
                }
            }
            // If the task is not accepted and the user is a tasker, show "Accept Task"
            acceptedBy == null && isTasker -> {
                btnTaskDetails.text = "Accept Task"
                btnTaskDetails.setOnClickListener {
                    acceptTask()
                }
            }
            // If the user has already accepted the task, show "Cancel Task"
            acceptedBy == currentUserId -> {
                btnTaskDetails.text = "Cancel Task"
                btnTaskDetails.setOnClickListener {
                    cancelAcceptedTask()
                }
            }
        }
    }

    private fun cancelPost() {
        Toast.makeText(this, "Cancel Post functionality here", Toast.LENGTH_SHORT).show()
        // Implement API call to cancel post
    }

    private fun navigateToVerification() {
        Toast.makeText(this, "Navigate to verification screen", Toast.LENGTH_SHORT).show()
        // Implement navigation to verification
    }

    private fun acceptTask() {
        Toast.makeText(this, "Accept Task functionality here", Toast.LENGTH_SHORT).show()
        // Implement API call to accept task
    }

    private fun cancelAcceptedTask() {
        Toast.makeText(this, "Cancel Task functionality here", Toast.LENGTH_SHORT).show()
        // Implement API call to cancel task
    }
}
