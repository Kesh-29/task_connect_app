package com.example.taskconnectapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var oldPassword: EditText
    private lateinit var newPassword: EditText
    private lateinit var confirmPassword: EditText
    private lateinit var changePasswordButton: Button
    private lateinit var sharedPreferences: SharedPreferences
    private val serverUrl = "http://10.0.2.2/taskconnect/change_password.php"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        val backButton = findViewById<ImageView>(R.id.backButton)
        val oldPasswordLayout = findViewById<TextInputLayout>(R.id.old_password)
        oldPassword = findViewById(R.id.etOldPassword)

        val source = intent.getStringExtra("source")

        // Determine the email source
        val email: String = if (source == "edit_profile") {
            sharedPreferences.getString("email", "") ?: ""
        } else {
            intent.getStringExtra("email") ?: ""
        }

        println("DEBUG: Retrieved Email in ChangePasswordActivity -> $email")

        // ✅ If email is missing, show error only in Edit Profile mode
        if (email.isEmpty() && source == "edit_profile") {
            Toast.makeText(this, "User email not found. Please log in again.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Show old password field only when coming from Edit Profile
        if (source == "edit_profile") {
            oldPasswordLayout.visibility = View.VISIBLE
        }

        backButton.setOnClickListener {
            if (source == "edit_profile") {
                finish()
            } else {
                val intent = Intent(this, LoginPage::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
        }

        newPassword = findViewById(R.id.password_input)
        confirmPassword = findViewById(R.id.etConfirmPassword)
        changePasswordButton = findViewById(R.id.Update_button)

        changePasswordButton.setOnClickListener {
            val oldPass = oldPassword.text.toString().trim()
            val password = newPassword.text.toString().trim()
            val confirmPass = confirmPassword.text.toString().trim()

            if (password.isEmpty() || confirmPass.isEmpty() || (source == "edit_profile" && oldPass.isEmpty())) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPass) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            changePassword(email, oldPass, password, source)
        }
    }

    private fun changePassword(email: String, oldPassword: String, newPassword: String, source: String?) {
        val client = OkHttpClient()
        val requestBody = FormBody.Builder()
            .add("email", email)
            .add("new_password", newPassword)

        // 🔹 Only add old password if coming from Edit Profile and it's not empty
        if (source == "edit_profile" && oldPassword.isNotEmpty()) {
            requestBody.add("old_password", oldPassword)
        }

        val request = Request.Builder()
            .url(serverUrl)
            .post(requestBody.build())
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(applicationContext, "Failed to connect", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                val jsonResponse = JSONObject(responseData ?: "{}")

                runOnUiThread {
                    if (jsonResponse.getString("status") == "success") {
                        Toast.makeText(applicationContext, "Password changed successfully", Toast.LENGTH_SHORT).show()

                        if (source == "edit_profile") {
                            finish()
                        } else {
                            startActivity(Intent(applicationContext, LoginPage::class.java))
                            finish()
                        }
                    } else {
                        Toast.makeText(applicationContext, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
