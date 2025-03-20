package com.example.taskconnectapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val bottomNavView = findViewById<BottomNavigationView>(R.id.btmNavView)

        // Set default fragment (HomeFragment) when app starts
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
        }

        bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home_btn -> replaceFragment(HomeFragment())
                R.id.activity_btn -> replaceFragment(UserActivityFragment())
                R.id.post_btn -> replaceFragment(PostFragment())
                R.id.profile_btn -> replaceFragment(ProfileFragment())
                else -> return@setOnItemSelectedListener false
            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
