package com.example.yourapp

import android.content.Intent
import android.os.Bundle
import com.example.test.R
import android.widget.ImageView
import com.google.android.material.bottomnavigation.BottomNavigationView

class SettingsActivity: BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_menu)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomMenu)
        bottomNav.selectedItemId = R.id.navigation_settings
        setupBottomNav(bottomNav)

        // 通知
        val buttonImage1 = findViewById<ImageView>(R.id.buttonImage1)
        buttonImage1.setOnClickListener {
            startActivity(Intent(this, NotifySettingsActivity::class.java))
        }

        // 言語
        val buttonImage2 = findViewById<ImageView>(R.id.buttonImage2)
        buttonImage2.setOnClickListener {
            startActivity(Intent(this, LanguageSettingsActivity::class.java))
        }

        // 所在地
//        val buttonImage3 = findViewById<ImageView>(R.id.buttonImage3)
//        buttonImage3.setOnClickListener {
//            startActivity(Intent(this, LocationSettingsActivity::class.java))
//        }
    }
}
