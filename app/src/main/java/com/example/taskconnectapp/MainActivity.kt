package com.example.taskconnectapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Hide the action bar
        supportActionBar?.hide()

        // Use Handler to delay navigation to the login page
        Handler(Looper.getMainLooper()).postDelayed(
            {
                val intent = Intent(this@MainActivity, LoginPage::class.java)
                startActivity(intent)
                finish() // Close the MainActivity
            },
            1200 // 3000 milliseconds = 3 seconds
        )
    }
}
