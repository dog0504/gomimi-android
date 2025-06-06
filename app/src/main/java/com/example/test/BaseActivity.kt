package com.example.yourapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.test.R
import com.example.yourapp.CalendarActivity
import com.example.yourapp.MainActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

open class BaseActivity : AppCompatActivity() {

    protected fun setupBottomNav(bottomNav: BottomNavigationView) {
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_manual -> {
                    if (this !is ManualActivity) {
                        startActivity(Intent(this, ManualActivity::class.java))
                        overridePendingTransition(0, 0)
                    }
                    true
                }
                R.id.navigation_calendar -> {
                    if (this !is CalendarActivity) {
                        startActivity(Intent(this, CalendarActivity::class.java))
                        overridePendingTransition(0, 0)
                    }
                    true
                }
                R.id.navigation_camera -> {
                    if (this !is MainActivity) {
                        startActivity(Intent(this, MainActivity::class.java))
                        overridePendingTransition(0, 0)
                    }
                    true
                }
                R.id.navigation_history -> {
                    if (this !is HistoryActivity) {
                        startActivity(Intent(this, HistoryActivity::class.java))
                        overridePendingTransition(0, 0)
                    }
                    true
                }
                R.id.navigation_settings -> {
                    if (this !is SettingsActivity) {
                        startActivity(Intent(this, SettingsActivity::class.java))
                        overridePendingTransition(0, 0)
                    }
                    true
                }
                else -> false
            }
        }
    }
}
