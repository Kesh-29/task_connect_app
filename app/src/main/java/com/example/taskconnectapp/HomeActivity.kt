package com.example.taskconnectapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val bottomNavView = findViewById<BottomNavigationView>(R.id.btmNavView)

        bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home_btn -> {
                    // Stay in HomeActivity
                    true
                }
                R.id.activity_btn -> {
                    startActivity(Intent(this, UserActivityActivity::class.java))
                    true
                }
                R.id.post_btn -> {
                    startActivity(Intent(this, PostingPageActivity::class.java))
                    true
                }
                R.id.profile_btn -> {
                    startActivity(Intent(this, CitizenProfilePageActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}