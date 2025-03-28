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

class ResetPasswordActivity : AppCompatActivity() {
    private lateinit var etEmail: EditText
    private lateinit var btnSendOTP: Button
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        etEmail = findViewById(R.id.email)
        btnSendOTP = findViewById(R.id.reset_btn)

        btnSendOTP.setOnClickListener {
            val email = etEmail.text.toString()
            if (email.isNotEmpty()) {
                sendOTP(email)
            } else {
                Toast.makeText(this, "Enter your email", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendOTP(email: String) {
        val requestBody = FormBody.Builder().add("email", email).build()
        val request = Request.Builder().url("http://10.0.2.2/taskconnect/send_otp.php").post(requestBody).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { Toast.makeText(this@ResetPasswordActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show() }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                val json = JSONObject(responseData)
                runOnUiThread {
                    if (json.getBoolean("success")) {
                        val intent = Intent(this@ResetPasswordActivity, OtpVerification::class.java)
                        intent.putExtra("email", email)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@ResetPasswordActivity, json.getString("message"), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
