package com.example.taskconnectapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class EditProfile : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etLocation: EditText
    private lateinit var etMobileNo: EditText
    private lateinit var btnUpdate: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var sharedPreferences: SharedPreferences

    // Store original values separately
    private var originalUsername: String = ""
    private var originalLocation: String = ""
    private var originalMobileNo: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val usersId = sharedPreferences.getInt("users_id", -1)

        if (usersId == -1) {
            showToast("User ID not found!")
            return
        }

        // Initialize views
        etUsername = findViewById(R.id.usernameField)
        etLocation = findViewById(R.id.locationField)
        etMobileNo = findViewById(R.id.phoneNumberField)
        btnUpdate = findViewById(R.id.btnUpdate)
        progressBar = findViewById(R.id.progressBar)

        // Set up click listeners
        findViewById<Button>(R.id.UserChangePassword).setOnClickListener {
            startActivity(Intent(this, ChangePasswordActivity::class.java).apply {
                putExtra("source", "edit_profile")
            })
        }

        findViewById<Button>(R.id.btnCancel).setOnClickListener {
            finish()
        }

        btnUpdate.setOnClickListener {
            updateProfile()
        }

        // Load profile data
        fetchUserProfile(usersId)
    }

    private fun fetchUserProfile(usersId: Int) {
        val request = Request.Builder()
            .url("http://10.0.2.2/taskconnect/get_user_profile.php?users_id=$usersId")
            .get()
            .build()

        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
            .newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        showToast("Error fetching profile: ${e.message}")
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    response.body?.string()?.let { responseBody ->
                        runOnUiThread {
                            try {
                                val jsonResponse = JSONObject(responseBody)
                                if (jsonResponse.getBoolean("success")) {
                                    val userData = jsonResponse.getJSONObject("user")

                                    // Store original values
                                    originalUsername = userData.optString("username", "")
                                    originalLocation = userData.optString("location", "")
                                    originalMobileNo = userData.optString("mobile_no", "")

                                    // Clear all fields (as requested)
                                    etUsername.setText("")
                                    etLocation.setText("")
                                    etMobileNo.setText("")

                                    // Store email in SharedPreferences
                                    userData.optString("email", "").takeIf { it.isNotEmpty() }?.let { email ->
                                        sharedPreferences.edit().putString("email", email).apply()
                                    }
                                } else {
                                    showToast("Failed to fetch profile data")
                                }
                            } catch (e: Exception) {
                                Log.e("EditProfile", "Error parsing profile data", e)
                                showToast("Error loading profile")
                            }
                        }
                    }
                }
            })
    }

    private fun updateProfile() {
        val usersId = sharedPreferences.getInt("users_id", -1).takeIf { it != -1 } ?: run {
            showToast("User ID not found!")
            return
        }

        val formBodyBuilder = FormBody.Builder()
            .add("users_id", usersId.toString())

        var changesMade = false

        // Only update fields that have non-empty new values
        etUsername.text.toString().trim().takeIf { it.isNotEmpty() }?.let {
            formBodyBuilder.add("username", it)
            changesMade = true
        }

        etLocation.text.toString().trim().takeIf { it.isNotEmpty() }?.let {
            formBodyBuilder.add("location", it)
            changesMade = true
        }

        etMobileNo.text.toString().trim().takeIf { it.isNotEmpty() }?.let {
            formBodyBuilder.add("mobile_no", it)
            changesMade = true
        }

        if (!changesMade) {
            showToast("No valid changes to update")
            return
        }

        showProgress(true)

        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
            .newCall(
                Request.Builder()
                    .url("http://10.0.2.2/taskconnect/update_profile.php")
                    .post(formBodyBuilder.build())
                    .build()
            ).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        showProgress(false)
                        showToast("Network error: ${e.message}")
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    runOnUiThread {
                        showProgress(false)
                        handleUpdateResponse(response)
                    }
                }
            })
    }

    private fun handleUpdateResponse(response: Response) {
        try {
            // Check if response is JSON
            if (!response.header("Content-Type").orEmpty().contains("application/json")) {
                val body = response.body?.string()
                Log.e("EditProfile", "Non-JSON response: $body")
                showToast("Server error occurred")
                return
            }

            val jsonResponse = JSONObject(response.body?.string() ?: "{}")
            if (jsonResponse.getBoolean("success")) {
                showToast("Profile updated successfully!")
                finish()
            } else {
                showToast(jsonResponse.optString("message", "Update failed"))
            }
        } catch (e: Exception) {
            Log.e("EditProfile", "Error parsing update response", e)
            showToast("Error processing response")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showProgress(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnUpdate.isEnabled = !show
    }
}