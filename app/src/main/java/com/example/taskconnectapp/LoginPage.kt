package com.example.taskconnectapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class LoginPage : AppCompatActivity() {
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_page)

        val emailOrUsernameInputEditText = findViewById<TextInputEditText>(R.id.email) // Now accepts username too
        val passwordInputEditText = findViewById<TextInputEditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val signUpButton = findViewById<Button>(R.id.btnSignUp)

        signUpButton.setOnClickListener {
            val intent = Intent(this, SignUpPageActivity::class.java)
            startActivity(intent)
        }

        loginButton.setOnClickListener {
            val emailOrUsername = emailOrUsernameInputEditText?.text.toString().trim()
            val password = passwordInputEditText?.text.toString().trim()

            if (emailOrUsername.isNotEmpty() && password.isNotEmpty()) {
                loginUser(emailOrUsername, password)
            } else {
                Toast.makeText(this, "Please enter your email/username and password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginUser(emailOrUsername: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val url = "http://10.0.2.2/taskconnect/login.php"

            val formBody = FormBody.Builder()
                .add("email", emailOrUsername)  // Accepts both email and username
                .add("password", password)
                .build()

            val request = Request.Builder()
                .url(url)
                .post(formBody)
                .build()

            try {
                val response = client.newCall(request).execute()
                val responseData = response.body?.string()

                println("Server Response: $responseData") // 🔴 Debugging

                if (!response.isSuccessful || responseData == null) {
                    runOnUiThread {
                        Toast.makeText(this@LoginPage, "Login Failed. Try again.", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                try {
                    val jsonResponse = JSONObject(responseData)

                    if (jsonResponse.getBoolean("success")) {
                        val usersId = jsonResponse.optInt("users_id", -1)
                        if (usersId == -1) {
                            runOnUiThread {
                                Toast.makeText(this@LoginPage, "Error retrieving user ID", Toast.LENGTH_SHORT).show()
                            }
                            return@launch
                        }

                        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                        sharedPref.edit().putInt("users_id", usersId).apply() // Ensure it's "users_id"

                        runOnUiThread {
                            Toast.makeText(this@LoginPage, "Login Successful!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@LoginPage, HomeActivity::class.java))
                            finish()
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@LoginPage, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@LoginPage, "JSON Error: ${e.message}\nResponse: $responseData", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@LoginPage, "Network Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
