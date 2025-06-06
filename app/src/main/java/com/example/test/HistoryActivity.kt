package com.example.yourapp

import android.os.Bundle
import com.example.test.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class HistoryActivity: BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomMenu)
        bottomNav.selectedItemId = R.id.navigation_history
        setupBottomNav(bottomNav)
    }
}
