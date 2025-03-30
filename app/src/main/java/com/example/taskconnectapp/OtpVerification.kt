package com.example.taskconnectapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class OtpVerification : AppCompatActivity() {

    private lateinit var otp1: EditText
    private lateinit var otp2: EditText
    private lateinit var otp3: EditText
    private lateinit var otp4: EditText
    private lateinit var verifyCodeButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp_verification)

        otp1 = findViewById(R.id.otp1)
        otp2 = findViewById(R.id.otp2)
        otp3 = findViewById(R.id.otp3)
        otp4 = findViewById(R.id.otp4)
        verifyCodeButton = findViewById(R.id.verifyCodeButton)

        val backButton = findViewById<ImageView>(R.id.backButton)

        backButton.setOnClickListener {
            val intent = Intent(this, LoginPage::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()  // Close the current activity
        }

        val otpFields = listOf(otp1, otp2, otp3, otp4)

        // Handle focus movement for OTP input
        for (i in otpFields.indices) {
            otpFields[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 1) {
                        if (i < otpFields.size - 1) {
                            otpFields[i + 1].requestFocus() // Move to next field
                        }
                    } else if (s?.isEmpty() == true && i > 0) {
                        otpFields[i - 1].requestFocus() // Move back on delete
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }

        // Verify button click
        verifyCodeButton.setOnClickListener {
            val enteredOtp = otp1.text.toString() + otp2.text.toString() + otp3.text.toString() + otp4.text.toString()

            if (enteredOtp.length == 4) {
                // Send OTP to server for verification
                verifyOtp(enteredOtp)
            } else {
                Toast.makeText(this, "Enter a 4-digit OTP", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun verifyOtp(otp: String) {
        val email = intent.getStringExtra("email") ?: return

        val requestBody = FormBody.Builder()
            .add("email", email)
            .add("otp", otp)
            .build()

        val request = Request.Builder()
            .url("http://10.0.2.2/taskconnect/verify_otp.php") // Use your actual server URL
            .post(requestBody)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@OtpVerification, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                val json = JSONObject(responseData)
                runOnUiThread {
                    if (json.getBoolean("success")) {
                        Toast.makeText(this@OtpVerification, "OTP Verified!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@OtpVerification, ChangePasswordActivity::class.java)
                        intent.putExtra("email", email) // Pass email to next screen
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@OtpVerification, json.getString("message"), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
