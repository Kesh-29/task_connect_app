package com.example.taskconnectapp

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody


class SignUpPageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up_page)

        val firstName = findViewById<TextInputEditText>(R.id.FirstNameField)
        val lastName = findViewById<TextInputEditText>(R.id.LastNameField)
        val username = findViewById<TextInputEditText>(R.id.usernameField)
        val email = findViewById<TextInputEditText>(R.id.emailField)
        val phone = findViewById<TextInputEditText>(R.id.phoneNumberField)
        val password = findViewById<TextInputEditText>(R.id.passwordField)
        val confirmPassword = findViewById<TextInputEditText>(R.id.confirmPasswordField)
        val btnRegister = findViewById<Button>(R.id.btnProceed)
        val backButton = findViewById<ImageView>(R.id.backButton)

        val passwordLayout = findViewById<TextInputLayout>(R.id.etPassword)
        val confirmPasswordLayout = findViewById<TextInputLayout>(R.id.etConfirmPassword)

        val logInButton = findViewById<Button>(R.id.btnLogIn)

        logInButton.setOnClickListener {
            val intent = Intent(this, LoginPage::class.java)
            startActivity(intent)
        }

        // Toggle password visibility
        passwordLayout.setEndIconOnClickListener {
            togglePasswordVisibility(password)
        }
        confirmPasswordLayout.setEndIconOnClickListener {
            togglePasswordVisibility(confirmPassword)
        }

        backButton.setOnClickListener {
            finish() // Go back to the previous activity
        }

        btnRegister.setOnClickListener {
            val firstNameText = firstName.text.toString()
            val lastNameText = lastName.text.toString()
            val usernameText = username.text.toString()
            val emailText = email.text.toString()
            val phoneText = phone.text.toString()
            val passwordText = password.text.toString()
            val confirmPasswordText = confirmPassword.text.toString()

            if (firstNameText.isNotEmpty() && lastNameText.isNotEmpty() && usernameText.isNotEmpty() &&
                emailText.isNotEmpty() && phoneText.isNotEmpty() && passwordText.isNotEmpty() && confirmPasswordText.isNotEmpty()) {

                if (passwordText == confirmPasswordText) {
                    registerUser(firstNameText, lastNameText, usernameText, emailText, phoneText, passwordText)
                } else {
                    Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun togglePasswordVisibility(editText: TextInputEditText) {
        if (editText.inputType == InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD) {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        editText.setSelection(editText.text?.length ?: 0) // Keep cursor at the end
    }

    private fun registerUser(firstName: String, lastName: String, username: String, email: String, phone: String, password: String) {
        val url = "http://10.0.2.2/taskconnect/register.php"
        val client = OkHttpClient()

        val jsonObject = JSONObject()
        jsonObject.put("first_name", firstName)  // ✅ Added first_name
        jsonObject.put("last_name", lastName)    // ✅ Added last_name
        jsonObject.put("username", username)
        jsonObject.put("email", email)
        jsonObject.put("mobile_no", phone)
        jsonObject.put("password", password)

        val requestBody = jsonObject.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(applicationContext, "Registration Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()?.trim() ?: ""

                if (responseBody.isEmpty()) {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Username Already Taken!", Toast.LENGTH_SHORT).show()
                    }
                    return
                }

                try {
                    val jsonResponse = JSONObject(responseBody)
                    runOnUiThread {
                        when {
                            jsonResponse.has("success") && jsonResponse.getBoolean("success") -> {
                                Toast.makeText(applicationContext, "Registration Successful!", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            jsonResponse.has("message") -> {
                                Toast.makeText(applicationContext, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                Toast.makeText(applicationContext, "Unknown error occurred!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Error parsing server response!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
