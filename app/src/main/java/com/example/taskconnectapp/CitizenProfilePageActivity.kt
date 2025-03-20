package com.example.taskconnectapp

import android.content.Intent // Added import for navigation
import android.os.Bundle
import android.widget.Button // Added import for Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class CitizenProfilePageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.fragment_citizen_profile_page)

        // Handle system bars padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Find the logout button by its ID
        val logoutButton = findViewById<Button>(R.id.logoutbtn) // Added this line
        val backButton = findViewById<ImageView>(R.id.backButton)

        backButton.setOnClickListener {
            finish() // Go back to the previous activity
        }

        // Set a click listener for the logout button
        logoutButton.setOnClickListener {
            // Navigate back to the login screen
            val intent = Intent(this, LoginPage::class.java) // Updated to go to LoginActivity
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Clears the back stack
            startActivity(intent) // Starts the login activity
        }
    }
}
