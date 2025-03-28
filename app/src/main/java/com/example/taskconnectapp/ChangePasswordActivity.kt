package com.example.taskconnectapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var newPassword: EditText
    private lateinit var confirmPassword: EditText
    private lateinit var changePasswordButton: Button
    private val serverUrl = "http://10.0.2.2/taskconnect/change_password.php" // Update for real server

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        newPassword = findViewById(R.id.password_input)
        confirmPassword = findViewById(R.id.etConfirmPassword)
        changePasswordButton = findViewById(R.id.Update_button)

        val email = intent.getStringExtra("email") ?: ""

        changePasswordButton.setOnClickListener {
            val password = newPassword.text.toString()
            val confirmPass = confirmPassword.text.toString()

            if (password.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPass) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            changePassword(email, password)
        }
    }

    private fun changePassword(email: String, password: String) {
        val client = OkHttpClient()
        val requestBody = FormBody.Builder()
            .add("email", email)
            .add("password", password)
            .build()

        val request = Request.Builder()
            .url(serverUrl)
            .post(requestBody)
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
                        startActivity(Intent(applicationContext, LoginPage::class.java))
                        finish()
                    } else {
                        Toast.makeText(applicationContext, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
