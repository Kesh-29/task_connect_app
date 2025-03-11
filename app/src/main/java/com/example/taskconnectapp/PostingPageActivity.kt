package com.example.taskconnectapp

import android.app.ProgressDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class PostingPageActivity : AppCompatActivity() {
    private lateinit var titleEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var contactEditText: EditText
    private lateinit var locationEditText: EditText
    private lateinit var requirementEditText: EditText
    private lateinit var budgetEditText: EditText
    private lateinit var postTaskButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_posting_page)

        // Initialize UI elements
        titleEditText = findViewById(R.id.question_txt)
        descriptionEditText = findViewById(R.id.descriptionText)
        contactEditText = findViewById(R.id.contact_number)
        locationEditText = findViewById(R.id.location)
        requirementEditText = findViewById(R.id.requirementText)
        budgetEditText = findViewById(R.id.budgetText)
        postTaskButton = findViewById(R.id.postTaskButton)
        val backButton = findViewById<ImageView>(R.id.backButton)

        backButton.setOnClickListener { finish() } // Go back to the previous activity

        // Submit task
        postTaskButton.setOnClickListener {
            postTaskToServer()
        }
    }

    private fun postTaskToServer() {
        val title = titleEditText.text.toString().trim()
        val description = descriptionEditText.text.toString().trim()
        val contact = contactEditText.text.toString().trim()
        val location = locationEditText.text.toString().trim()
        val requirement = requirementEditText.text.toString().trim()
        val budgetText = budgetEditText.text.toString().trim()
        val budget = if (budgetText.isEmpty()) "0.00" else budgetText // Ensure valid format

        if (title.isEmpty() || description.isEmpty() || contact.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Posting task...")
        progressDialog.show()

        val client = OkHttpClient()
        val requestBody = FormBody.Builder()
            .add("title", title)
            .add("description", description)
            .add("contact_number", contact)
            .add("location", location)
            .add("requirements", requirement)
            .add("budget", budget)
            .build()

        val request = Request.Builder()
            .url("http://10.0.2.2/post_task.php")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    progressDialog.dismiss()
                    Toast.makeText(applicationContext, "Failed to post task: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    progressDialog.dismiss()
                    val responseData = response.body?.string()
                    val jsonObject = JSONObject(responseData ?: "{}")

                    if (jsonObject.optBoolean("success", false)) {
                        Toast.makeText(applicationContext, "Task posted successfully!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        val errorMsg = jsonObject.optString("message", "Unknown error")
                        Toast.makeText(applicationContext, "Failed to post task: $errorMsg", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}